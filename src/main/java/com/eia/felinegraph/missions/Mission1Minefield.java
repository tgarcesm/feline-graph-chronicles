package com.eia.felinegraph.missions;

import com.eia.felinegraph.algorithms.Bfs;
import com.eia.felinegraph.algorithms.Dfs;
import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.Mission;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.Tokenizer;
import com.eia.felinegraph.core.draw.GridDrawing;

import java.util.ArrayList;
import java.util.List;

/**
 * Mision 1 - Rescatando a Nina del campo minado.
 *
 * Esta clase parsea la entrada, valida los rangos, ejecuta BFS y DFS,
 * genera la linea exacta de salida y prepara el modelo de dibujo.
 *
 * Los algoritmos permanecen independientes de JavaFX.
 */
public final class Mission1Minefield implements Mission {

    private static final int MAX_ROWS = 1000;
    private static final int MAX_COLS = 1000;

    private static final int MAX_DRAW = 50;

    private static final String SAMPLE =
            "10 10\n"
                    + "9\n"
                    + "0 1 2\n"
                    + "1 1 2\n"
                    + "2 2 2 9\n"
                    + "3 2 1 7\n"
                    + "5 3 3 6 9\n"
                    + "6 4 0 1 2 7\n"
                    + "7 3 0 3 8\n"
                    + "8 2 7 9\n"
                    + "9 3 2 3 4\n"
                    + "0 0\n"
                    + "9 9\n"
                    + "0 0\n";

    @Override
    public String title() {
        return "Mision 1 - Rescatando a Nina";
    }

    @Override
    public String sampleInput() {
        return SAMPLE;
    }

    @Override
    public MissionResult solve(
            String rawInput
    ) throws InputFormatException {

        Tokenizer tokenizer =
                new Tokenizer(rawInput);

        if (!tokenizer.hasNext()) {

            throw new InputFormatException(
                    "La entrada esta vacia: pegue los datos de la mision "
                            + "o use el boton de cargar ejemplo"
            );
        }

        List<CaseResult> cases =
                new ArrayList<>();

        List<String> warnings =
                new ArrayList<>();

        int caseNumber = 1;
        boolean terminatorFound = false;

        while (tokenizer.hasNext()) {

            try {

                int rows =
                        tokenizer.nextInt();

                int cols =
                        tokenizer.nextInt();

                /*
                 * 0 0 termina la entrada y no es un caso.
                 */
                if (rows == 0
                        && cols == 0) {

                    terminatorFound = true;
                    break;
                }

                validateDimensions(
                        rows,
                        cols,
                        tokenizer
                );

                cases.add(
                        solveCase(
                                tokenizer,
                                caseNumber,
                                rows,
                                cols
                        )
                );

                caseNumber++;

            } catch (InputFormatException error) {

                throw new InputFormatException(
                        "Error en el caso #"
                                + caseNumber
                                + ": "
                                + error.getMessage()
                );
            }
        }

        if (!terminatorFound) {

            throw new InputFormatException(
                    "La entrada de la Mision 1 debe terminar con 0 0"
            );
        }

        if (tokenizer.hasNext()) {

            warnings.add(
                    "Aviso: la entrada tiene datos de sobra despues "
                            + "del terminador 0 0, se ignoraron"
            );
        }

        return new MissionResult(
                cases,
                warnings
        );
    }

    private CaseResult solveCase(
            Tokenizer tokenizer,
            int caseNumber,
            int rows,
            int cols
    ) throws InputFormatException {

        boolean[] mines =
                new boolean[rows * cols];

        int rowsWithBombs =
                readInt(
                        tokenizer,
                        "numero de filas con bombas",
                        0,
                        rows
                );

        for (int i = 0;
             i < rowsWithBombs;
             i++) {

            int bombRow =
                    readInt(
                            tokenizer,
                            "fila con bombas",
                            0,
                            rows - 1
                    );

            int bombCount =
                    readInt(
                            tokenizer,
                            "numero de bombas de la fila "
                                    + bombRow,
                            0,
                            cols
                    );

            for (int j = 0;
                 j < bombCount;
                 j++) {

                int bombCol =
                        readInt(
                                tokenizer,
                                "columna de bomba",
                                0,
                                cols - 1
                        );

                mines[
                        bombRow * cols
                                + bombCol
                        ] = true;
            }
        }

        int startRow =
                readInt(
                        tokenizer,
                        "fila de origen",
                        0,
                        rows - 1
                );

        int startCol =
                readInt(
                        tokenizer,
                        "columna de origen",
                        0,
                        cols - 1
                );

        int goalRow =
                readInt(
                        tokenizer,
                        "fila de destino",
                        0,
                        rows - 1
                );

        int goalCol =
                readInt(
                        tokenizer,
                        "columna de destino",
                        0,
                        cols - 1
                );

        Bfs.Result bfs =
                Bfs.search(
                        rows,
                        cols,
                        mines,
                        startRow,
                        startCol,
                        goalRow,
                        goalCol
                );

        Dfs.Result dfs =
                Dfs.search(
                        rows,
                        cols,
                        mines,
                        startRow,
                        startCol,
                        goalRow,
                        goalCol
                );

        String line;

        if (!bfs.reachable()
                || !dfs.reachable()) {

            line =
                    "Case #"
                            + caseNumber
                            + ": Nina is unreachable";

        } else {

            line =
                    "Case #"
                            + caseNumber
                            + ": BFS "
                            + bfs.distance()
                            + " DFS "
                            + dfs.distance();
        }

        /*
         * Hasta 50 x 50 el dibujo es obligatorio.
         */
        if (rows <= MAX_DRAW
                && cols <= MAX_DRAW) {

            GridDrawing drawing =
                    new GridDrawing(
                            rows,
                            cols,
                            mines.clone(),
                            startRow,
                            startCol,
                            goalRow,
                            goalCol,
                            bfs.path(),
                            dfs.path()
                    );

            return new CaseResult(
                    caseNumber,
                    line,
                    drawing,
                    null,
                    null
            );
        }

        String reason =
                "Dibujo omitido: la grilla es de "
                        + rows
                        + " x "
                        + cols
                        + " y el limite de visualizacion es 50 x 50";

        return new CaseResult(
                caseNumber,
                line,
                null,
                null,
                reason
        );
    }

    private void validateDimensions(
            int rows,
            int cols,
            Tokenizer tokenizer
    ) throws InputFormatException {

        if (rows < 1
                || rows > MAX_ROWS) {

            throw new InputFormatException(
                    "R debe estar entre 1 y 1000, se encontro "
                            + rows
                            + " (token "
                            + (tokenizer.position() - 1)
                            + ")"
            );
        }

        if (cols < 1
                || cols > MAX_COLS) {

            throw new InputFormatException(
                    "C debe estar entre 1 y 1000, se encontro "
                            + cols
                            + " (token "
                            + tokenizer.position()
                            + ")"
            );
        }
    }

    private int readInt(
            Tokenizer tokenizer,
            String name,
            int minimum,
            int maximum
    ) throws InputFormatException {

        int value =
                tokenizer.nextInt();

        if (value < minimum
                || value > maximum) {

            throw new InputFormatException(
                    name
                            + " debe estar entre "
                            + minimum
                            + " y "
                            + maximum
                            + ", se encontro "
                            + value
                            + " (token "
                            + tokenizer.position()
                            + ")"
            );
        }

        return value;
    }
}