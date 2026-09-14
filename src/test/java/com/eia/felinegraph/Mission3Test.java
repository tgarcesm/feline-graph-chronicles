package com.eia.felinegraph;

import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.draw.GraphDrawing;
import com.eia.felinegraph.core.draw.MatrixDrawing;
import com.eia.felinegraph.missions.Mission3Churun;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la Mision 3 (Floyd-Warshall + Bellman-Ford). La salida se compara completa,
 * caracter por caracter, y se cubre cada rama de la precedencia: inalcanzable, no acotado y
 * finito (incluyendo negativo), ademas del cross-check entre los dos algoritmos.
 */
class Mission3Test {

    private String resolver(String entrada) throws InputFormatException {
        return new Mission3Churun().solve(entrada).consoleOutput();
    }

    @Test
    void sampleDelEnunciado() throws Exception {
        Mission3Churun mision = new Mission3Churun();
        String salida = mision.solve(mision.sampleInput()).consoleOutput();
        assertEquals("Case #1: 110\nCase #2: Infinite churun!\nCase #3: -65", salida);
    }

    @Test
    void destinoInalcanzableDesdeOrigen() throws Exception {
        assertEquals("Case #1: Limon blocked the way", resolver("1\n3 1 0 2\n0 1 5\n"));
    }

    @Test
    void cicloPositivoQueLlegaAlDestinoEsInfinito() throws Exception {
        String entrada = "1\n3 3 0 2\n0 1 5\n1 2 10\n2 1 -3\n";
        assertEquals("Case #1: Infinite churun!", resolver(entrada));
    }

    @Test
    void cicloPositivoQueNoLlegaAlDestinoDaRespuestaFinita() throws Exception {
        // El ciclo 1 <-> 2 gana +8 por vuelta, pero no hay ninguna arista desde 1 o 2 hacia el 3.
        String entrada = "1\n4 4 0 3\n0 1 5\n1 2 10\n2 1 -2\n0 3 7\n";
        assertEquals("Case #1: 7", resolver(entrada));
    }

    @Test
    void elMaximoPuedeSerNegativo() throws Exception {
        assertEquals("Case #1: -65", resolver("1\n3 3 0 2\n0 1 -40\n1 2 -25\n0 2 -80\n"));
    }

    @Test
    void origenIgualADestinoSinCicloDaCero() throws Exception {
        assertEquals("Case #1: 0", resolver("1\n2 1 0 0\n0 1 5\n"));
    }

    @Test
    void aristasParalelasSeQuedanConElMaximo() throws Exception {
        assertEquals("Case #1: 9", resolver("1\n2 3 0 1\n0 1 5\n0 1 9\n0 1 3\n"));
    }

    @Test
    void lasAristasSonDirigidas() throws Exception {
        // 0 -> 1 existe, pero no al reves: desde 1 no se puede llegar a 0.
        assertEquals("Case #1: Limon blocked the way", resolver("1\n2 1 1 0\n0 1 5\n"));
    }

    @Test
    void elNumeroDeCasoEmpiezaEnUnoYAvanza() throws Exception {
        String entrada = "2\n2 1 0 1\n0 1 5\n2 1 1 0\n0 1 5\n";
        assertEquals("Case #1: 5\nCase #2: Limon blocked the way", resolver(entrada));
    }

    @Test
    void entradaConLineasEnBlancoYEspaciosDeSobra() throws Exception {
        String entrada = "\n\n 1 \n\n  2 1  0 1 \n\n 0 1   5 \n\n";
        assertEquals("Case #1: 5", resolver(entrada));
    }

    @Test
    void floydWarshallYBellmanFordCoincidenEnTodosLosCasosDelSample() throws Exception {
        Mission3Churun mision = new Mission3Churun();
        MissionResult resultado = mision.solve(mision.sampleInput());
        assertTrue(resultado.warnings().isEmpty(),
                "No deberia haber discrepancia entre Floyd-Warshall y Bellman-Ford en el sample");
    }

    @Test
    void elAvisoDeDiscrepanciaSeDisparaCuandoLasCategoriasNoCoinciden() {
        String aviso = Mission3Churun.discrepancyWarning(5, "FINITE", 10L, "UNBOUNDED", 10L);
        assertNotNull(aviso);
        assertTrue(aviso.contains("#5"));
    }

    @Test
    void elAvisoDeDiscrepanciaSeDisparaCuandoElValorNoCoincide() {
        assertNotNull(Mission3Churun.discrepancyWarning(2, "FINITE", 10L, "FINITE", 11L));
    }

    @Test
    void sinDiscrepanciaNoHayAviso() {
        assertNull(Mission3Churun.discrepancyWarning(1, "FINITE", 10L, "FINITE", 10L));
        assertNull(Mission3Churun.discrepancyWarning(1, "UNREACHABLE", 0L, "UNREACHABLE", 0L));
    }

    @Test
    void laMatrizSeConstruyeConGuionYConInf() throws Exception {
        Mission3Churun mision = new Mission3Churun();
        MissionResult resultado = mision.solve(mision.sampleInput());
        MatrixDrawing matriz = resultado.cases().get(1).matrix();
        assertNotNull(matriz);
        assertEquals(4, matriz.n);
        assertEquals("inf", matriz.cells[0][3]);
        assertEquals("0", matriz.cells[0][0]);
    }

    @Test
    void laMatrizSiempreEstaPresenteSinImportarElTamano() throws Exception {
        Mission3Churun mision = new Mission3Churun();
        MissionResult resultado = mision.solve(mision.sampleInput());
        for (CaseResult caso : resultado.cases()) {
            assertNotNull(caso.matrix());
        }
    }

    @Test
    void elDibujoDelGrafoResaltaElCaminoEnElCasoFinito() throws Exception {
        Mission3Churun mision = new Mission3Churun();
        MissionResult resultado = mision.solve(mision.sampleInput());
        CaseResult caso1 = resultado.cases().get(0);
        assertNull(caso1.drawingSkippedReason());
        GraphDrawing dibujo = (GraphDrawing) caso1.drawing();
        assertTrue(dibujo.directed);
        assertEquals(GraphDrawing.Highlight.PATH, dibujo.kind);
        assertTrue(hayAlgunaResaltada(dibujo));
    }

    @Test
    void elDibujoDelGrafoResaltaElCicloEnElCasoNoAcotado() throws Exception {
        Mission3Churun mision = new Mission3Churun();
        MissionResult resultado = mision.solve(mision.sampleInput());
        CaseResult caso2 = resultado.cases().get(1);
        GraphDrawing dibujo = (GraphDrawing) caso2.drawing();
        assertEquals(GraphDrawing.Highlight.CYCLE, dibujo.kind);
        assertTrue(hayAlgunaResaltada(dibujo));
    }

    private boolean hayAlgunaResaltada(GraphDrawing dibujo) {
        for (boolean resaltada : dibujo.highlighted) {
            if (resaltada) {
                return true;
            }
        }
        return false;
    }

    @Test
    void conMasDe60NodosSeOmiteElDibujoDelGrafoPeroLaMatrizSigue() throws Exception {
        MissionResult resultado = new Mission3Churun().solve("1\n61 1 0 60\n0 60 9\n");
        CaseResult caso = resultado.cases().get(0);
        assertEquals("Case #1: 9", caso.line());
        assertNull(caso.drawing());
        assertNotNull(caso.matrix());
        assertEquals("Dibujo omitido: la instancia tiene 61 nodos y el limite es 60", caso.drawingSkippedReason());
    }

    @Test
    void con60NodosSiSeDibujaElGrafo() throws Exception {
        CaseResult caso = new Mission3Churun().solve("1\n60 1 0 59\n0 59 9\n").cases().get(0);
        assertNotNull(caso.drawing());
        assertNull(caso.drawingSkippedReason());
    }

    @Test
    void entradaBasuraLanzaErrorLegible() {
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("hola mundo"));
    }

    @Test
    void entradaVaciaLanzaErrorLegible() {
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("   \n  "));
    }

    @Test
    void entradaIncompletaDiceEnQueCasoFallo() {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> new Mission3Churun().solve("2\n2 1 0 1\n0 1 5\n3 2 0 2\n0 1 5\n"));
        assertTrue(error.getMessage().startsWith("Error en el caso #2"));
    }

    @Test
    void pesoFueraDeRangoSeRechaza() {
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("1\n2 1 0 1\n0 1 1001\n"));
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("1\n2 1 0 1\n0 1 -1001\n"));
    }

    @Test
    void nodoFueraDeRangoLanzaErrorLegible() {
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("1\n2 1 0 5\n0 1 5\n"));
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("1\n2 1 0 1\n0 2 5\n"));
    }

    @Test
    void numeroDeCasosCeroONegativoSeRechaza() {
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("0\n"));
        assertThrows(InputFormatException.class, () -> new Mission3Churun().solve("-1\n"));
    }

    @Test
    void datosDeSobraGeneranUnAvisoPeroNoUnError() throws Exception {
        MissionResult resultado = new Mission3Churun().solve("1\n2 1 0 1\n0 1 5\n99 99\n");
        assertEquals("Case #1: 5", resultado.consoleOutput());
        assertTrue(resultado.warnings().stream().anyMatch(w -> w.startsWith("Aviso: la entrada")));
    }
}
