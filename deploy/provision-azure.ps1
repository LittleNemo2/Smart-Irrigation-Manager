
param(
    [string]$ResourceGroup = "rg-irrigation",
    [string]$Location      = "westus",
    [string]$DbServer      = "sim-pg-bordeaux",
    [string]$DbName        = "irrigation",
    [string]$DbAdmin       = "simadmin",
    [string]$Registry      = "simregbordeaux",
    [string]$Environment   = "sim-env",
    [string]$AppName       = "smart-irrigation",
    [string]$ImageName     = "smart-irrigation-manager",
    [string]$Tag           = "v1"
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param([string]$Description, [scriptblock]$Command)
    Write-Host ""
    Write-Host "==> $Description" -ForegroundColor Cyan
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Step failed: $Description (exit code $LASTEXITCODE)"
    }
}

$secure = Read-Host "Choose a PostgreSQL admin password" -AsSecureString
$DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))

$loginServer = "$Registry.azurecr.io"
$image       = "${loginServer}/${ImageName}:$Tag"
$jdbcUrl     = "jdbc:postgresql://$DbServer.postgres.database.azure.com:5432/${DbName}?sslmode=require"

Invoke-Step "Registering resource providers" {
    az provider register --namespace Microsoft.App --wait
    az provider register --namespace Microsoft.OperationalInsights --wait
    az provider register --namespace Microsoft.DBforPostgreSQL --wait
    az provider register --namespace Microsoft.ContainerRegistry --wait
    az extension add --name containerapp --upgrade --only-show-errors
}

Invoke-Step "Creating resource group $ResourceGroup" {
    az group create --name $ResourceGroup --location $Location --output none
}

Invoke-Step "Creating PostgreSQL Flexible Server $DbServer" {
    az postgres flexible-server create --resource-group $ResourceGroup --name $DbServer `
        --location $Location --tier Burstable --sku-name Standard_B1ms --storage-size 32 `
        --version 16 --admin-user $DbAdmin --admin-password $DbPassword `
        --public-access 0.0.0.0 --yes --output none
}
Invoke-Step "Creating database $DbName" {
    az postgres flexible-server db create --resource-group $ResourceGroup `
        --server-name $DbServer --name $DbName --output none
}

Invoke-Step "Creating container registry $Registry" {
    az acr create --resource-group $ResourceGroup --name $Registry --sku Basic `
        --location $Location --admin-enabled true --output none
}
Invoke-Step "Building Docker image $image" { docker build -t $image . }
Invoke-Step "Signing in to the registry" { az acr login --name $Registry }
Invoke-Step "Pushing $image" { docker push $image }

Invoke-Step "Creating Container Apps environment $Environment" {
    az containerapp env create --name $Environment --resource-group $ResourceGroup `
        --location $Location --output none
}

$acrUser = az acr credential show --name $Registry --query username --output tsv
$acrPass = az acr credential show --name $Registry --query "passwords[0].value" --output tsv

Invoke-Step "Creating Container App $AppName" {
    az containerapp create --name $AppName --resource-group $ResourceGroup `
        --environment $Environment --image $image `
        --registry-server $loginServer --registry-username $acrUser --registry-password $acrPass `
        --target-port 8080 --ingress external --min-replicas 1 --max-replicas 1 `
        --cpu 0.5 --memory 1.0Gi `
        --secrets "db-password=$DbPassword" `
        --env-vars SPRING_PROFILES_ACTIVE=prod "DATABASE_URL=$jdbcUrl" `
            "DATABASE_USERNAME=$DbAdmin" DATABASE_PASSWORD=secretref:db-password `
        --output none
}

$fqdn = az containerapp show --name $AppName --resource-group $ResourceGroup `
    --query "properties.configuration.ingress.fqdn" --output tsv
Write-Host ""
Write-Host "Provisioning complete. The app will be ready in about a minute at https://$fqdn" -ForegroundColor Green
