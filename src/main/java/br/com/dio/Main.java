package br.com.dio;

import br.com.dio.persistence.migration.MigrationStrategy;
import br.com.dio.ui.MainMenu;

import java.sql.SQLException;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public final class Main {

    private Main() {
    }

    public static void main(final String[] args) {
        try {
            executeDatabaseMigration();
            new MainMenu().execute();
        } catch (SQLException exception) {
            System.err.println(
                    "Não foi possível iniciar a aplicação. Verifique a conexão com o banco de dados."
            );
            System.err.println("Detalhes: " + exception.getMessage());
        }
    }

    private static void executeDatabaseMigration() throws SQLException {
        try (var connection = getConnection()) {
            new MigrationStrategy(connection).executeMigration();
        }
    }

}