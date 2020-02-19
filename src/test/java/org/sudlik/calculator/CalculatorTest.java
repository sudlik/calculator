package org.sudlik.calculator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    @Test
    public void handle_empty_string_as_zero() {
        String emptyString = "";

        int result = Calculator.Add(emptyString);

        assertEquals(0, result);
    }

    @Test
    public void handle_integer() {
        String integer = "1";

        int result = Calculator.Add(integer);

        assertEquals(1, result);
    }

    @Test
    public void handle_empty_string_and_integer_together() {
        String numbers = ",1";

        int result = Calculator.Add(numbers);

        assertEquals(1, result);
    }

    @Test
    public void handle_two_integers() {
        String numbers = "1,2";

        int result = Calculator.Add(numbers);

        assertEquals(3, result);
    }

    @Test
    public void handle_newline_as_delimiter() {
        String numbers = "1\n2\n3";

        int result = Calculator.Add(numbers);

        assertEquals(6, result);
    }

    @Test
    public void allow_change_delimiter() {
        String numbers = "//;\n1;2;3";

        int result = Calculator.Add(numbers);

        assertEquals(6, result);
    }

    @Test
    public void do_not_allow_negative_numbers() {
        String numbers = "//;\n1;2;-3";

        Executable executable = () -> Calculator.Add(numbers);

        Calculator.NegativesNotAllowedException exception = assertThrows(Calculator.NegativesNotAllowedException.class, executable);
        assertEquals("Negatives not allowed: -3", exception.getMessage());
    }

    @Test
    public void show_multiple_negative_numbers_in_message() {
        String numbers = "//;\n1;-2;-3";

        Executable executable = () -> Calculator.Add(numbers);

        Calculator.NegativesNotAllowedException exception = assertThrows(Calculator.NegativesNotAllowedException.class, executable);
        assertEquals("Negatives not allowed: -2, -3", exception.getMessage());
    }

    @Test
    public void allow_1000() {
        String numbers = "1,2,1000";

        int result = Calculator.Add(numbers);

        assertEquals(1003, result);
    }

    @Test
    public void ignore_number_greater_than_1000() {
        String numbers = "1,2,1001";

        int result = Calculator.Add(numbers);

        assertEquals(3, result);
    }

    @Test
    public void allow_multi_character_custom_delimiter() {
        String numbers = "//[***]\n1***2***3";

        int result = Calculator.Add(numbers);

        assertEquals(6, result);
    }

    @Test
    public void allow_multiple_custom_delimiters() {
        String numbers = "//[;][*]\n1*2;3";

        int result = Calculator.Add(numbers);

        assertEquals(6, result);
    }

    @Test
    public void allow_multiple_custom_multi_character_delimiters() {
        String numbers = "//[;;][**]\n1**2;;3";

        int result = Calculator.Add(numbers);

        assertEquals(6, result);
    }
}