package com.austinbordeaux.irrigation.web.form;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneFormValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private ZoneForm validForm() {
        ZoneForm form = new ZoneForm();
        form.setName("Front Lawn");
        form.setPlantTypeId(1L);
        form.setFlowRateGpm(8.0);
        form.setMoistureThreshold(20.0);
        form.setTargetMoisture(32.0);
        return form;
    }

    private boolean hasErrorOn(Set<ConstraintViolation<ZoneForm>> errors, String field) {
        return errors.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    @DisplayName("A complete, sensible zone has no errors")
    void validFormPasses() {
        assertEquals(0, validator.validate(validForm()).size());
    }

    @Test
    @DisplayName("Featured 2: threshold equal to target is rejected")
    void thresholdEqualToTargetIsRejected() {
        ZoneForm form = validForm();
        form.setMoistureThreshold(32.0);
        form.setTargetMoisture(32.0);
        Set<ConstraintViolation<ZoneForm>> errors = validator.validate(form);
        assertTrue(hasErrorOn(errors, "thresholdBelowTarget"));
    }

    @Test
    @DisplayName("HTML in the zone name is rejected")
    void htmlInNameIsRejected() {
        ZoneForm form = validForm();
        form.setName("<script>x</script>");
        assertTrue(hasErrorOn(validator.validate(form), "name"));
    }

    @Test
    @DisplayName("A flow rate of 0 GPM is rejected")
    void zeroFlowRateIsRejected() {
        ZoneForm form = validForm();
        form.setFlowRateGpm(0.0);
        assertTrue(hasErrorOn(validator.validate(form), "flowRateGpm"));
    }
}