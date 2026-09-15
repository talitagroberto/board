package br.com.dio.ui.util;

import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsoleInputTest {

    @Test
    void shouldReadAndTrimRequiredText() {
        var input = new ConsoleInput(
                new Scanner("   \n  Board profissional  \n")
        );

        var result = input.readRequiredText("Informe um texto");

        assertEquals("Board profissional", result);
    }

    @Test
    void shouldIgnoreInvalidValueAndReadInteger() {
        var input = new ConsoleInput(
                new Scanner("valor inválido\n42\n")
        );

        var result = input.readInt("Informe um número");

        assertEquals(42, result);
    }

    @Test
    void shouldRejectNegativeValue() {
        var input = new ConsoleInput(
                new Scanner("-5\n3\n")
        );

        var result = input.readNonNegativeInt("Informe uma quantidade");

        assertEquals(3, result);
    }

    @Test
    void shouldReadLongValue() {
        var input = new ConsoleInput(
                new Scanner("identificador inválido\n123456789\n")
        );

        var result = input.readLong("Informe um ID");

        assertEquals(123456789L, result);
    }

    @Test
    void shouldRejectNullScanner() {
        assertThrows(
                NullPointerException.class,
                () -> new ConsoleInput(null)
        );
    }

}