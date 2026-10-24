param(
    [Parameter(Mandatory = $true)]
    [string]$Tag,

    [string]$ResourceGroup = "rg-irrigation",
    [string]$Registry      = "simregbordeaux",
    [string]$AppName       = "smart-irrigation",
    [string]$ImageName     = "smart-irrigation-manager",
    [switch]$SkipTests
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

function Test-Site {
    param([string]$BaseUrl)
    for ($i = 1; $i -le 12; $i++) {
        try {
            $login = Invoke-WebRequest -Uri "$BaseUrl/login" -UseBasicParsing -TimeoutSec 20
            $css   = Invoke-WebRequest -Uri "$BaseUrl/css/app.css" -UseBasicParsing -TimeoutSec 20
            if ($login.StatusCode -eq 200 -and $login.Content -match "Smart Irrigation Manager" -and $css.StatusCode -eq 200) {
                Write-Host "Smoke test passed on attempt $i." -ForegroundColor Green
                return $true
            }
        } catch {
            Write-Host "Attempt ${i}: site not ready yet ($($_.Exception.Message))"
        }
        Start-Sleep -Seconds 15
    }
    return $false
}

$loginServer = "$Registry.azurecr.io"
$newImage    = "${loginServer}/${ImageName}:$Tag"

if (-not $SkipTests) {
    Invoke-Step "Running unit tests (mvnw test)" { .\mvnw.cmd -q test }
}

Invoke-Step "Building Docker image $newImage" { docker build -t $newImage . }

Invoke-Step "Signing in to Azure Container Registry" { az acr login --name $Registry }
Invoke-Step "Pushing $newImage" { docker push $newImage }

$previousImage = az containerapp show --name $AppName --resource-group $ResourceGroup `
    --query "properties.template.containers[0].image" --output tsv
Write-Host "Currently deployed image: $previousImage"

Invoke-Step "Updating Container App to $Tag" {
    az containerapp update --name $AppName --resource-group $ResourceGroup --image $newImage --output none
}

Write-Host ""
Write-Host "==> Waiting for the new revision to become ready" -ForegroundColor Cyan
$ready = $false
for ($i = 1; $i -le 20; $i++) {
    $latest      = az containerapp show --name $AppName --resource-group $ResourceGroup --query "properties.latestRevisionName" --output tsv
    $latestReady = az containerapp show --name $AppName --resource-group $ResourceGroup --query "properties.latestReadyRevisionName" --output tsv
    if ($latest -and $latest -eq $latestReady) {
        Write-Host "Revision $latest is ready."
        $ready = $true
        break
    }
    Write-Host "Revision $latest is still starting (check $i of 20)..."
    Start-Sleep -Seconds 15
}

$fqdn = az containerapp show --name $AppName --resource-group $ResourceGroup `
    --query "properties.configuration.ingress.fqdn" --output tsv
$url = "https://$fqdn"
Write-Host "Smoke testing $url"

if ($ready -and (Test-Site -BaseUrl $url)) {
    Write-Host ""
    Write-Host "Deployment of $Tag succeeded: $url" -ForegroundColor Green
    exit 0
}

Write-Host "Smoke test failed. Rolling back to $previousImage" -ForegroundColor Yellow
Invoke-Step "Rolling back" {
    az containerapp update --name $AppName --resource-group $ResourceGroup --image $previousImage --output none
}
if (Test-Site -BaseUrl $url) {
    Write-Host "Rollback complete. $previousImage is live again." -ForegroundColor Yellow
} else {
    Write-Host "Rollback finished, but the site is still not answering. Check the logs with:" -ForegroundColor Red
    Write-Host "az containerapp logs show --name $AppName --resource-group $ResourceGroup --tail 100"
}
exit 1
