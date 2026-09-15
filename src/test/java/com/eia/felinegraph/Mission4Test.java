package com.eia.felinegraph;

import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.draw.GraphDrawing;
import com.eia.felinegraph.missions.Mission4Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Mission4Test {

    private String resolver(
            String input
    ) throws InputFormatException {

        return new Mission4Network()
                .solve(input)
                .consoleOutput();
    }

    @Test
    void sampleDelEnunciado()
            throws Exception {

        Mission4Network mission =
                new Mission4Network();

        assertEquals(
                "Case #1: 55",
                mission.solve(
                        mission.sampleInput()
                ).consoleOutput()
        );
    }

    @Test
    void unNodoSinCablesCuestaCero()
            throws Exception {

        assertEquals(
                "Case #1: 0",
                resolver(
                        "1\n"
                                + "1\n"
                                + "0\n"
                )
        );
    }

    @Test
    void redDesconectadaEsImposible()
            throws Exception {

        String input =
                "1\n"
                        + "4\n"
                        + "2\n"
                        + "1 2 10\n"
                        + "3 4 20\n";

        assertEquals(
                "Case #1: Limon cut too many cables",
                resolver(input)
        );
    }

    @Test
    void duplicadosYLazosSeManejan()
            throws Exception {

        String input =
                "1\n"
                        + "3\n"
                        + "5\n"
                        + "1 1 0\n"
                        + "1 2 100\n"
                        + "1 2 10\n"
                        + "2 3 5\n"
                        + "1 3 50\n";

        assertEquals(
                "Case #1: 15",
                resolver(input)
        );
    }

    @Test
    void usaLongParaElCostoAcumulado()
            throws Exception {

        StringBuilder input =
                new StringBuilder();

        input.append("1\n");
        input.append("10000\n");
        input.append("9999\n");

        for (int i = 1;
             i < 10000;
             i++) {

            input.append(i)
                    .append(" ")
                    .append(i + 1)
                    .append(" 1000000\n");
        }

        assertEquals(
                "Case #1: 9999000000",
                resolver(
                        input.toString()
                )
        );
    }

    @Test
    void dibujoDelSampleResaltaElMst()
            throws Exception {

        Mission4Network mission =
                new Mission4Network();

        MissionResult result =
                mission.solve(
                        mission.sampleInput()
                );

        CaseResult caseResult =
                result.cases().get(0);

        assertNotNull(
                caseResult.drawing()
        );

        GraphDrawing drawing =
                (GraphDrawing) caseResult.drawing();

        assertFalse(
                drawing.directed
        );

        assertEquals(
                GraphDrawing.Highlight.MST,
                drawing.kind
        );

        assertArrayEquals(
                new boolean[]{
                        true,
                        false,
                        true,
                        false,
                        true
                },
                drawing.highlighted
        );
    }

    @Test
    void masDeCienInterseccionesOmiteDibujo()
            throws Exception {

        CaseResult caseResult =
                new Mission4Network()
                        .solve(
                                "1\n"
                                        + "101\n"
                                        + "0\n"
                        )
                        .cases()
                        .get(0);

        assertEquals(
                "Case #1: Limon cut too many cables",
                caseResult.line()
        );

        assertNull(
                caseResult.drawing()
        );

        assertNotNull(
                caseResult.drawingSkippedReason()
        );
    }

    @Test
    void variosCasosSeNumeranCorrectamente()
            throws Exception {

        String input =
                "2\n"
                        + "1\n"
                        + "0\n"
                        + "2\n"
                        + "1\n"
                        + "1 2 7\n";

        assertEquals(
                "Case #1: 0\nCase #2: 7",
                resolver(input)
        );
    }

    @Test
    void nodoFueraDeRangoSeRechaza() {

        assertThrows(
                InputFormatException.class,
                () -> resolver(
                        "1\n"
                                + "2\n"
                                + "1\n"
                                + "1 3 5\n"
                )
        );
    }

    @Test
    void costoNegativoSeRechaza() {

        assertThrows(
                InputFormatException.class,
                () -> resolver(
                        "1\n"
                                + "2\n"
                                + "1\n"
                                + "1 2 -5\n"
                )
        );
    }

    @Test
    void entradaVaciaSeRechaza() {

        assertThrows(
                InputFormatException.class,
                () -> resolver(" \n\t ")
        );
    }

    @Test
    void datosSobrantesGeneranWarning()
            throws Exception {

        MissionResult result =
                new Mission4Network()
                        .solve(
                                "1\n"
                                        + "1\n"
                                        + "0\n"
                                        + "55 66\n"
                        );

        assertEquals(
                "Case #1: 0",
                result.consoleOutput()
        );

        assertEquals(
                1,
                result.warnings().size()
        );
    }
}