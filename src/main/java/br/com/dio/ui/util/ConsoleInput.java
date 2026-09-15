package br.com.dio.ui.util;

import java.util.Objects;
import java.util.Scanner;

public final class ConsoleInput {

    private static final ConsoleInput INSTANCE = new ConsoleInput();

    private final Scanner scanner;

    private ConsoleInput() {
        this(new Scanner(System.in));
    }

    ConsoleInput(final Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner);
    }

    public static ConsoleInput getInstance() {
        return INSTANCE;
    }

    public String readRequiredText(final String message) {
        while (true) {
            System.out.print(message + ": ");
            var value = scanner.nextLine().trim();

            if (!value.isBlank()) {
                return value;
            }

            System.out.println("O valor informado não pode ficar vazio.");
        }
    }

    public int readInt(final String message) {
        while (true) {
            var value = readRequiredText(message);

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                System.out.println("Informe um número inteiro válido.");
            }
        }
    }

    public int readNonNegativeInt(final String message) {
        while (true) {
            var value = readInt(message);

            if (value >= 0) {
                return value;
            }

            System.out.println("Informe um número maior ou igual a zero.");
        }
    }

    public long readLong(final String message) {
        while (true) {
            var value = readRequiredText(message);

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                System.out.println("Informe um identificador numérico válido.");
            }
        }
    }

}
