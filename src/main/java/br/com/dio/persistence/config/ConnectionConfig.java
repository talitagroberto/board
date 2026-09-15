package br.com.dio.persistence.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionConfig {

    private static final String DATABASE_URL_VARIABLE = "BOARD_DB_URL";
    private static final String DATABASE_USER_VARIABLE = "BOARD_DB_USER";
    private static final String DATABASE_PASSWORD_VARIABLE = "BOARD_DB_PASSWORD";

    private ConnectionConfig() {
    }

    public static Connection getConnection() throws SQLException {
        var url = getRequiredEnvironmentVariable(DATABASE_URL_VARIABLE);
        var user = getRequiredEnvironmentVariable(DATABASE_USER_VARIABLE);
        var password = getRequiredEnvironmentVariable(DATABASE_PASSWORD_VARIABLE);

        var connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        return connection;
    }

    private static String getRequiredEnvironmentVariable(final String variableName) {
        var value = System.getenv(variableName);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente " + variableName + " não foi configurada."
            );
        }

        return value;
    }

}