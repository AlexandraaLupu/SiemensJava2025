package com.siemens.internship;

import com.siemens.internship.model.Item;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory(); // i need this to test @Pattern
        validator = factory.getValidator();
    }

    @Test
    void testValidItem() {
        Item item = new Item(1L, "Item", "Item", "-", "test@email.com");

        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        assertTrue(violations.isEmpty(), "Item should be valid");
    }

    @Test
    void testInvalidEmail() {
        Item item = new Item(1L, "Item", "Item", "-", "invalid-email");

        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        assertFalse(violations.isEmpty(), "Email should be invalid"); // if there are no violations

        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") // checks if the field that triggered the violation is email
                        && v.getMessage().contains("Invalid email"));
        assertTrue(hasEmailError, "Expected email validation error");
    }

    @Test
    void testSettersAndGetters() {
        Item item = new Item();
        item.setId(10L);
        item.setName("Test1");
        item.setDescription("Test");
        item.setStatus("PROCESSED");
        item.setEmail("valid@email.com");

        assertEquals(10L, item.getId());
        assertEquals("Test1", item.getName());
        assertEquals("Test", item.getDescription());
        assertEquals("PROCESSED", item.getStatus());
        assertEquals("valid@email.com", item.getEmail());
    }
}
