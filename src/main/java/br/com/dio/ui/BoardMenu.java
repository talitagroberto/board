package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.BoardColumnQueryService;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.CardQueryService;
import br.com.dio.service.CardService;
import br.com.dio.ui.util.ConsoleInput;

import java.sql.SQLException;
import java.util.List;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class BoardMenu {

    private static final int CREATE_CARD_OPTION = 1;
    private static final int MOVE_CARD_OPTION = 2;
    private static final int BLOCK_CARD_OPTION = 3;
    private static final int UNBLOCK_CARD_OPTION = 4;
    private static final int CANCEL_CARD_OPTION = 5;
    private static final int SHOW_BOARD_OPTION = 6;
    private static final int SHOW_COLUMN_OPTION = 7;
    private static final int SHOW_CARD_OPTION = 8;
    private static final int RETURN_OPTION = 9;

    private final ConsoleInput input = ConsoleInput.getInstance();
    private final BoardEntity board;

    public BoardMenu(final BoardEntity board) {
        this.board = board;
    }

    public void execute() {
        System.out.printf(
                "%n=== BOARD: %s (ID: %s) ===%n",
                board.getName(),
                board.getId()
        );

        while (true) {
            showMenu();
            var option = input.readInt("Escolha uma opção");

            try {
                switch (option) {
                    case CREATE_CARD_OPTION -> createCard();
                    case MOVE_CARD_OPTION -> moveCardToNextColumn();
                    case BLOCK_CARD_OPTION -> blockCard();
                    case UNBLOCK_CARD_OPTION -> unblockCard();
                    case CANCEL_CARD_OPTION -> cancelCard();
                    case SHOW_BOARD_OPTION -> showBoard();
                    case SHOW_COLUMN_OPTION -> showColumn();
                    case SHOW_CARD_OPTION -> showCard();
                    case RETURN_OPTION -> {
                        System.out.println("Voltando ao menu principal.");
                        return;
                    }
                    default -> System.out.println(
                            "Opção inválida. Escolha uma opção entre 1 e 9."
                    );
                }
            } catch (SQLException exception) {
                System.err.println(
                        "Não foi possível concluir a operação no banco de dados."
                );
                System.err.println("Detalhes: " + exception.getMessage());
            }
        }
    }

    private void showMenu() {
        System.out.println("\n1 - Criar um card");
        System.out.println("2 - Mover um card");
        System.out.println("3 - Bloquear um card");
        System.out.println("4 - Desbloquear um card");
        System.out.println("5 - Cancelar um card");
        System.out.println("6 - Visualizar o board");
        System.out.println("7 - Visualizar uma coluna e seus cards");
        System.out.println("8 - Visualizar um card");
        System.out.println("9 - Voltar ao menu principal");
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        card.setTitle(input.readRequiredText("Informe o título do card"));
        card.setDescription(input.readRequiredText("Informe a descrição do card"));
        card.setBoardColumn(board.getInitialColumn());

        try (var connection = getConnection()) {
            new CardService(connection).create(card);
        }

        System.out.printf("Card \"%s\" criado com sucesso.%n", card.getTitle());
    }

    private void moveCardToNextColumn() throws SQLException {
        var cardId = input.readLong(
                "Informe o ID do card que deseja mover para a próxima coluna"
        );

        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, getColumnsInfo());
            System.out.printf("Card com ID %s movido com sucesso.%n", cardId);
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        var cardId = input.readLong("Informe o ID do card que deseja bloquear");
        var reason = input.readRequiredText("Informe o motivo do bloqueio");

        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason, getColumnsInfo());
            System.out.printf("Card com ID %s bloqueado com sucesso.%n", cardId);
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        var cardId = input.readLong("Informe o ID do card que deseja desbloquear");
        var reason = input.readRequiredText("Informe o motivo do desbloqueio");

        try (var connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason);
            System.out.printf("Card com ID %s desbloqueado com sucesso.%n", cardId);
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        var cardId = input.readLong(
                "Informe o ID do card que deseja mover para a coluna de cancelamento"
        );
        var confirmation = input.readRequiredText(
                "Digite S para confirmar o cancelamento"
        );

        if (!confirmation.equalsIgnoreCase("S")) {
            System.out.println("Cancelamento não realizado.");
            return;
        }

        try (var connection = getConnection()) {
            new CardService(connection).cancel(
                    cardId,
                    board.getCancelColumn().getId(),
                    getColumnsInfo()
            );
            System.out.printf("Card com ID %s cancelado com sucesso.%n", cardId);
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try (var connection = getConnection()) {
            var result = new BoardQueryService(connection)
                    .showBoardDetails(board.getId());

            result.ifPresentOrElse(
                    selectedBoard -> {
                        System.out.printf(
                                "%nBoard: %s (ID: %s)%n",
                                selectedBoard.name(),
                                selectedBoard.id()
                        );

                        selectedBoard.columns().forEach(column ->
                                System.out.printf(
                                        "- %s | Tipo: %s | Cards: %s%n",
                                        column.name(),
                                        column.kind(),
                                        column.cardsAmount()
                                )
                        );
                    },
                    () -> System.out.println("O board não foi encontrado.")
            );
        }
    }

    private void showColumn() throws SQLException {
        var columnIds = board.getBoardColumns()
                .stream()
                .map(BoardColumnEntity::getId)
                .toList();

        long selectedColumnId;

        while (true) {
            System.out.printf(
                    "%nEscolha uma coluna do board \"%s\":%n",
                    board.getName()
            );

            board.getBoardColumns().forEach(column ->
                    System.out.printf(
                            "%s - %s [%s]%n",
                            column.getId(),
                            column.getName(),
                            column.getKind()
                    )
            );

            selectedColumnId = input.readLong("Informe o ID da coluna");

            if (columnIds.contains(selectedColumnId)) {
                break;
            }

            System.out.println("A coluna informada não pertence a este board.");
        }

        try (var connection = getConnection()) {
            var column = new BoardColumnQueryService(connection)
                    .findById(selectedColumnId);

            column.ifPresentOrElse(
                    selectedColumn -> {
                        System.out.printf(
                                "%nColuna: %s | Tipo: %s%n",
                                selectedColumn.getName(),
                                selectedColumn.getKind()
                        );

                        if (selectedColumn.getCards().isEmpty()) {
                            System.out.println("Esta coluna não possui cards.");
                            return;
                        }

                        selectedColumn.getCards().forEach(card ->
                                System.out.printf(
                                        "%nCard %s - %s%nDescrição: %s%n",
                                        card.getId(),
                                        card.getTitle(),
                                        card.getDescription()
                                )
                        );
                    },
                    () -> System.out.println("A coluna não foi encontrada.")
            );
        }
    }

    private void showCard() throws SQLException {
        var cardId = input.readLong(
                "Informe o ID do card que deseja visualizar"
        );

        try (var connection = getConnection()) {
            new CardQueryService(connection)
                    .findById(cardId)
                    .ifPresentOrElse(
                            card -> {
                                System.out.printf(
                                        "%nCard %s - %s%n",
                                        card.id(),
                                        card.title()
                                );
                                System.out.printf(
                                        "Descrição: %s%n",
                                        card.description()
                                );
                                System.out.println(
                                        card.blocked()
                                                ? "Bloqueado. Motivo: " + card.blockReason()
                                                : "O card não está bloqueado."
                                );
                                System.out.printf(
                                        "Quantidade de bloqueios: %s%n",
                                        card.blocksAmount()
                                );
                                System.out.printf(
                                        "Coluna atual: %s - %s%n",
                                        card.columnId(),
                                        card.columnName()
                                );
                            },
                            () -> System.out.printf(
                                    "Não existe um card com o ID %s.%n",
                                    cardId
                            )
                    );
        }
    }

    private List<BoardColumnInfoDTO> getColumnsInfo() {
        return board.getBoardColumns()
                .stream()
                .map(column -> new BoardColumnInfoDTO(
                        column.getId(),
                        column.getOrder(),
                        column.getKind()
                ))
                .toList();
    }

}