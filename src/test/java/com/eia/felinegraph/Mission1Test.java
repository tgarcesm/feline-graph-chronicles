package com.eia.felinegraph;

import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.draw.GridDrawing;
import com.eia.felinegraph.missions.Mission1Minefield;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class Mission1Test {

    private String resolver(
            String input
    ) throws InputFormatException {

        return new Mission1Minefield()
                .solve(input)
                .consoleOutput();
    }

    @Test
    void sampleDelEnunciado()
            throws Exception {

        Mission1Minefield mission =
                new Mission1Minefield();

        assertEquals(
                "Case #1: BFS 18 DFS 32",
                mission.solve(
                        mission.sampleInput()
                ).consoleOutput()
        );
    }

    @Test
    void destinoRodeadoDeBombasEsInalcanzable()
            throws Exception {

        String input =
                "3 3\n"
                        + "2\n"
                        + "0 1 1\n"
                        + "1 2 0 1\n"
                        + "0 0\n"
                        + "0 2\n"
                        + "0 0\n";

        assertEquals(
                "Case #1: Nina is unreachable",
                resolver(input)
        );
    }

    @Test
    void origenIgualDestinoDaCero()
            throws Exception {

        String input =
                "3 3\n"
                        + "0\n"
                        + "1 1\n"
                        + "1 1\n"
                        + "0 0\n";

        assertEquals(
                "Case #1: BFS 0 DFS 0",
                resolver(input)
        );
    }

    @Test
    void origenConBombaEsInalcanzable()
            throws Exception {

        String input =
                "2 2\n"
                        + "1\n"
                        + "0 1 0\n"
                        + "0 0\n"
                        + "1 1\n"
                        + "0 0\n";

        assertEquals(
                "Case #1: Nina is unreachable",
                resolver(input)
        );
    }

    @Test
    void destinoConBombaEsInalcanzable()
            throws Exception {

        String input =
                "2 2\n"
                        + "1\n"
                        + "1 1 1\n"
                        + "0 0\n"
                        + "1 1\n"
                        + "0 0\n";

        assertEquals(
                "Case #1: Nina is unreachable",
                resolver(input)
        );
    }

    @Test
    void grillaUnoPorUnoDaCero()
            throws Exception {

        String input =
                "1 1\n"
                        + "0\n"
                        + "0 0\n"
                        + "0 0\n"
                        + "0 0\n";

        assertEquals(
                "Case #1: BFS 0 DFS 0",
                resolver(input)
        );
    }

    @Test
    void aceptaWhitespaceLibre()
            throws Exception {

        String input =
                "\n\n"
                        + " 2   2 \n"
                        + "\t0\n"
                        + " 0  0\n"
                        + "\t1 1\n"
                        + "\n 0 0 \n";

        assertEquals(
                "Case #1: BFS 2 DFS 2",
                resolver(input)
        );
    }

    @Test
    void dibujoDelSampleContieneLosDosCaminos()
            throws Exception {

        Mission1Minefield mission =
                new Mission1Minefield();

        MissionResult result =
                mission.solve(
                        mission.sampleInput()
                );

        CaseResult caseResult =
                result.cases().get(0);

        assertNotNull(
                caseResult.drawing()
        );

        assertNull(
                caseResult.drawingSkippedReason()
        );

        GridDrawing drawing =
                (GridDrawing) caseResult.drawing();

        assertEquals(10, drawing.rows);
        assertEquals(10, drawing.cols);

        assertEquals(19, drawing.bfsPath.length);
        assertEquals(33, drawing.dfsPath.length);

        assertEquals(0, drawing.bfsPath[0]);
        assertEquals(
                99,
                drawing.bfsPath[
                        drawing.bfsPath.length - 1
                        ]
        );

        assertEquals(0, drawing.dfsPath[0]);
        assertEquals(
                99,
                drawing.dfsPath[
                        drawing.dfsPath.length - 1
                        ]
        );
    }

    @Test
    void sobreElLimiteSeOmiteDibujoPeroSeCalcula()
            throws Exception {

        String input =
                "51 1\n"
                        + "0\n"
                        + "0 0\n"
                        + "50 0\n"
                        + "0 0\n";

        CaseResult caseResult =
                new Mission1Minefield()
                        .solve(input)
                        .cases()
                        .get(0);

        assertEquals(
                "Case #1: BFS 50 DFS 50",
                caseResult.line()
        );

        assertNull(
                caseResult.drawing()
        );

        assertEquals(
                "Dibujo omitido: la grilla es de 51 x 1 "
                        + "y el limite de visualizacion es 50 x 50",
                caseResult.drawingSkippedReason()
        );
    }

    @Test
    void grillaMilPorMilCumpleElRequisito()
            throws Exception {

        String input =
                "1000 1000\n"
                        + "0\n"
                        + "0 0\n"
                        + "999 0\n"
                        + "0 0\n";

        assertTimeout(
                Duration.ofSeconds(2),
                () -> assertEquals(
                        "Case #1: BFS 999 DFS 999",
                        resolver(input)
                )
        );
    }

    @Test
    void basuraLanzaErrorLegible() {

        assertThrows(
                InputFormatException.class,
                () -> resolver("hola mundo")
        );
    }

    @Test
    void entradaVaciaLanzaError() {

        assertThrows(
                InputFormatException.class,
                () -> resolver(" \n\t ")
        );
    }

    @Test
    void faltaTerminadorSeRechaza() {

        String input =
                "1 1\n"
                        + "0\n"
                        + "0 0\n"
                        + "0 0\n";

        assertThrows(
                InputFormatException.class,
                () -> resolver(input)
        );
    }

    @Test
    void coordenadaFueraDeRangoSeRechaza() {

        String input =
                "2 2\n"
                        + "0\n"
                        + "2 0\n"
                        + "1 1\n"
                        + "0 0\n";

        assertThrows(
                InputFormatException.class,
                () -> resolver(input)
        );
    }

    @Test
    void datosDespuesDelTerminadorGeneranWarning()
            throws Exception {

        MissionResult result =
                new Mission1Minefield()
                        .solve(
                                "1 1\n"
                                        + "0\n"
                                        + "0 0\n"
                                        + "0 0\n"
                                        + "0 0\n"
                                        + "5 5 5\n"
                        );

        assertEquals(
                "Case #1: BFS 0 DFS 0",
                result.consoleOutput()
        );

        assertEquals(
                1,
                result.warnings().size()
        );
    }
}