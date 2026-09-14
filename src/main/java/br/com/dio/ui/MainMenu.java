package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;
import br.com.dio.ui.util.ConsoleInput;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.FINAL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.INITIAL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.PENDING;

public class MainMenu {

    private static final int CREATE_BOARD_OPTION = 1;
    private static final int SELECT_BOARD_OPTION = 2;
    private static final int DELETE_BOARD_OPTION = 3;
    private static final int EXIT_OPTION = 4;

    private final ConsoleInput input = ConsoleInput.getInstance();

    public void execute() throws SQLException {
        System.out.println("\n=== GERENCIADOR DE BOARDS ===");

        while (true) {
            showMenu();
            var option = input.readInt("Escolha uma opção");

            switch (option) {
                case CREATE_BOARD_OPTION -> createBoard();
                case SELECT_BOARD_OPTION -> selectBoard();
                case DELETE_BOARD_OPTION -> deleteBoard();
                case EXIT_OPTION -> {
                    System.out.println("Aplicação encerrada. Até logo!");
                    return;
                }
                default -> System.out.println("Opção inválida. Escolha uma opção entre 1 e 4.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n1 - Criar um novo board");
        System.out.println("2 - Selecionar um board existente");
        System.out.println("3 - Excluir um board");
        System.out.println("4 - Sair");
    }

    private void createBoard() throws SQLException {
        var board = new BoardEntity();
        board.setName(input.readRequiredText("Informe o nome do board"));

        var additionalColumns = input.readNonNegativeInt(
                "Informe a quantidade de colunas pendentes adicionais"
        );

        List<BoardColumnEntity> columns = new ArrayList<>();

        var initialColumnName = input.readRequiredText("Informe o nome da coluna inicial");
        columns.add(createColumn(initialColumnName, INITIAL, 0));

        for (int index = 0; index < additionalColumns; index++) {
            var pendingColumnName = input.readRequiredText(
                    "Informe o nome da coluna pendente " + (index + 1)
            );
            columns.add(createColumn(pendingColumnName, PENDING, index + 1));
        }

        var finalColumnName = input.readRequiredText("Informe o nome da coluna final");
        columns.add(createColumn(finalColumnName, FINAL, additionalColumns + 1));

        var cancelColumnName = input.readRequiredText("Informe o nome da coluna de cancelamento");
        columns.add(createColumn(cancelColumnName, CANCEL, additionalColumns + 2));

        board.setBoardColumns(columns);

        try (var connection = getConnection()) {
            new BoardService(connection).insert(board);
        }

        System.out.printf("Board \"%s\" criado com sucesso.%n", board.getName());
    }

    private void selectBoard() throws SQLException {
        var id = input.readLong("Informe o ID do board que deseja selecionar");

        final Optional<BoardEntity> board;

        try (var connection = getConnection()) {
            board = new BoardQueryService(connection).findById(id);
        }

        board.ifPresentOrElse(
                selectedBoard -> new BoardMenu(selectedBoard).execute(),
                () -> System.out.printf("Não foi encontrado um board com o ID %s.%n", id)
        );
    }

    private void deleteBoard() throws SQLException {
        var id = input.readLong("Informe o ID do board que deseja excluir");
        var confirmation = input.readRequiredText(
                "Esta operação é permanente. Digite S para confirmar"
        );

        if (!confirmation.equalsIgnoreCase("S")) {
            System.out.println("Exclusão cancelada.");
            return;
        }

        try (var connection = getConnection()) {
            var deleted = new BoardService(connection).delete(id);

            if (deleted) {
                System.out.printf("Board com ID %s excluído com sucesso.%n", id);
            } else {
                System.out.printf("Não foi encontrado um board com o ID %s.%n", id);
            }
        }
    }

    private static BoardColumnEntity createColumn(
            final String name,
            final BoardColumnKindEnum kind,
            final int order
    ) {
        var column = new BoardColumnEntity();
        column.setName(name);
        column.setKind(kind);
        column.setOrder(order);
        return column;
    }

}