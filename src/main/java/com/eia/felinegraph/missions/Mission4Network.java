package com.eia.felinegraph.missions;

import com.eia.felinegraph.algorithms.Kruskal;
import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.Mission;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.Tokenizer;
import com.eia.felinegraph.core.draw.GraphDrawing;

import java.util.ArrayList;
import java.util.List;

/**
 * Mision 4 - Reconectando la red.
 *
 * Lee los casos, valida el formato, convierte los nodos 1..N del
 * enunciado a indices internos 0..N-1, ejecuta Kruskal y prepara
 * la salida y el dibujo.
 */
public final class Mission4Network implements Mission {

    private static final int MAX_NODES =
            10_000;

    private static final int MAX_CABLES =
            100_000;

    private static final long MAX_COST =
            1_000_000L;

    private static final int MAX_DRAW_NODES =
            100;

    private static final int MAX_DRAW_CABLES =
            300;

    private static final String SAMPLE =
            "1\n"
                    + "4\n"
                    + "5\n"
                    + "1 2 10\n"
                    + "2 3 20\n"
                    + "3 4 30\n"
                    + "4 1 40\n"
                    + "1 3 15\n";

    @Override
    public String title() {
        return "Mision 4 - Reconectando la red";
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

        int totalCases =
                tokenizer.nextInt();

        if (totalCases < 1) {

            throw new InputFormatException(
                    "El numero de casos T debe ser al menos 1, se encontro "
                            + totalCases
            );
        }

        List<CaseResult> cases =
                new ArrayList<>();

        for (int caseNumber = 1;
             caseNumber <= totalCases;
             caseNumber++) {

            try {

                cases.add(
                        solveCase(
                                tokenizer,
                                caseNumber
                        )
                );

            } catch (InputFormatException error) {

                throw new InputFormatException(
                        "Error en el caso #"
                                + caseNumber
                                + ": "
                                + error.getMessage()
                );
            }
        }

        List<String> warnings =
                new ArrayList<>();

        if (tokenizer.hasNext()) {

            warnings.add(
                    "Aviso: la entrada tiene datos de sobra despues "
                            + "del caso #"
                            + totalCases
                            + ", se ignoraron"
            );
        }

        return new MissionResult(
                cases,
                warnings
        );
    }

    private CaseResult solveCase(
            Tokenizer tokenizer,
            int caseNumber
    ) throws InputFormatException {

        int nodes =
                readInt(
                        tokenizer,
                        "N",
                        1,
                        MAX_NODES
                );

        int cables =
                readInt(
                        tokenizer,
                        "C",
                        0,
                        MAX_CABLES
                );

        int[] from =
                new int[cables];

        int[] to =
                new int[cables];

        long[] cost =
                new long[cables];

        for (int i = 0;
             i < cables;
             i++) {

            /*
             * El enunciado usa nodos 1..N.
             * El algoritmo interno usa 0..N-1.
             */
            from[i] =
                    readInt(
                            tokenizer,
                            "inicio del cable",
                            1,
                            nodes
                    ) - 1;

            to[i] =
                    readInt(
                            tokenizer,
                            "fin del cable",
                            1,
                            nodes
                    ) - 1;

            cost[i] =
                    readCost(
                            tokenizer
                    );
        }

        Kruskal.Result result =
                Kruskal.minimumSpanningTree(
                        nodes,
                        from,
                        to,
                        cost
                );

        String line;

        if (result.connected()) {

            line =
                    "Case #"
                            + caseNumber
                            + ": "
                            + result.totalCost();

        } else {

            line =
                    "Case #"
                            + caseNumber
                            + ": Limon cut too many cables";
        }

        if (nodes <= MAX_DRAW_NODES
                && cables <= MAX_DRAW_CABLES) {

            boolean[] highlighted;

            GraphDrawing.Highlight kind;

            if (result.connected()) {

                highlighted =
                        result.selected();

                kind =
                        GraphDrawing.Highlight.MST;

            } else {

                highlighted =
                        new boolean[cables];

                kind =
                        GraphDrawing.Highlight.NONE;
            }

            GraphDrawing drawing =
                    new GraphDrawing(
                            nodes,
                            false,
                            from.clone(),
                            to.clone(),
                            cost.clone(),
                            -1,
                            -1,
                            highlighted,
                            kind
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
                "Dibujo omitido: la instancia tiene "
                        + nodes
                        + " intersecciones y "
                        + cables
                        + " cables; el limite es 100 intersecciones y 300 cables";

        return new CaseResult(
                caseNumber,
                line,
                null,
                null,
                reason
        );
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

    private long readCost(
            Tokenizer tokenizer
    ) throws InputFormatException {

        long value =
                tokenizer.nextLong();

        if (value < 0
                || value > MAX_COST) {

            throw new InputFormatException(
                    "cost debe estar entre 0 y "
                            + MAX_COST
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