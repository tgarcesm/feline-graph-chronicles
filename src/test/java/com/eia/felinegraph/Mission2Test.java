package com.eia.felinegraph;

import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.draw.GraphDrawing;
import com.eia.felinegraph.missions.Mission2Accounts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la Mision 2 (Dijkstra). La salida se compara completa, caracter por caracter,
 * porque el enunciado dice que los mensajes se comparan automaticamente.
 */
class Mission2Test {

    private String resolver(String entrada) throws InputFormatException {
        return new Mission2Accounts().solve(entrada).consoleOutput();
    }

    @Test
    void sampleDelEnunciado() throws Exception {
        Mission2Accounts mision = new Mission2Accounts();
        String salida = mision.solve(mision.sampleInput()).consoleOutput();
        assertEquals("Case #1: 100\nCase #2: 150\nCase #3: Nina is very sad", salida);
    }

    @Test
    void origenIgualADestinoDaCero() throws Exception {
        assertEquals("Case #1: 0", resolver("1\n3 2 1 1\n0 1 5\n1 2 7\n"));
    }

    @Test
    void grafoSinAristasEsInalcanzable() throws Exception {
        assertEquals("Case #1: Nina is very sad", resolver("1\n4 0 0 3\n"));
    }

    @Test
    void grafoDeUnSoloNodo() throws Exception {
        assertEquals("Case #1: 0", resolver("1\n1 0 0 0\n"));
    }

    @Test
    void aristasRepetidasConDistintoPesoUsanLaMasBarata() throws Exception {
        assertEquals("Case #1: 20", resolver("1\n2 3 0 1\n0 1 50\n0 1 20\n1 0 70\n"));
    }

    @Test
    void unLazoNoAfectaElResultado() throws Exception {
        assertEquals("Case #1: 10", resolver("1\n3 4 0 2\n0 0 5\n0 1 4\n1 1 0\n1 2 6\n"));
    }

    @Test
    void pesoCeroEnVariasAristas() throws Exception {
        assertEquals("Case #1: 0", resolver("1\n4 4 0 3\n0 1 0\n1 2 0\n2 3 0\n0 3 5\n"));
    }

    @Test
    void lasConexionesSonBidireccionales() throws Exception {
        assertEquals("Case #1: 30", resolver("1\n3 2 0 2\n1 0 10\n2 1 20\n"));
    }

    @Test
    void costoAcumuladoQueNoCabeEnIntSeGuardaEnLong() throws Exception {
        // Cadena 0-1-2-...-9999 con peso maximo: 9999 * 1000000 = 9999000000 (mas que Integer.MAX_VALUE)
        StringBuilder entrada = new StringBuilder("1\n10000 9999 0 9999\n");
        for (int i = 0; i < 9999; i++) {
            entrada.append(i).append(" ").append(i + 1).append(" 1000000\n");
        }
        assertEquals("Case #1: 9999000000", resolver(entrada.toString()));
    }

    @Test
    void elNumeroDeCasoEmpiezaEnUnoYAvanza() throws Exception {
        String entrada = "3\n1 0 0 0\n2 0 0 1\n2 1 1 0\n0 1 7\n";
        assertEquals("Case #1: 0\nCase #2: Nina is very sad\nCase #3: 7", resolver(entrada));
    }

    @Test
    void entradaConLineasEnBlancoYEspaciosDeSobra() throws Exception {
        String entrada = "\n\n  2  \n\n 2 1   0 1 \n\n 0 1\n100 \n  1 0 0 0  \n\n";
        assertEquals("Case #1: 100\nCase #2: 0", resolver(entrada));
    }

    @Test
    void elDibujoResaltaLasConexionesDelCamino() throws Exception {
        Mission2Accounts mision = new Mission2Accounts();
        MissionResult resultado = mision.solve(mision.sampleInput());

        // Caso 2 del sample: la ruta 2 -> 1 -> 0 usa las conexiones 2 (1-2) y 0 (0-1), no la 1 (0-2)
        CaseResult caso2 = resultado.cases().get(1);
        assertNull(caso2.drawingSkippedReason());
        GraphDrawing dibujo = (GraphDrawing) caso2.drawing();
        assertEquals(GraphDrawing.Highlight.PATH, dibujo.kind);
        assertFalse(dibujo.directed);
        assertArrayEquals(new boolean[] {true, false, true}, dibujo.highlighted);
        assertEquals(2, dibujo.start);
        assertEquals(0, dibujo.goal);

        // Caso 3 del sample: no hay camino, no se resalta nada
        GraphDrawing dibujo3 = (GraphDrawing) resultado.cases().get(2).drawing();
        assertEquals(GraphDrawing.Highlight.NONE, dibujo3.kind);
    }

    @Test
    void conMasDe60NodosSeOmiteElDibujoPeroSeResponde() throws Exception {
        MissionResult resultado = new Mission2Accounts().solve("1\n61 1 0 60\n0 60 9\n");
        CaseResult caso = resultado.cases().get(0);
        assertEquals("Case #1: 9", caso.line());
        assertNull(caso.drawing());
        assertEquals("Dibujo omitido: la instancia tiene 61 nodos y el limite es 60", caso.drawingSkippedReason());
    }

    @Test
    void con60NodosSiSeDibuja() throws Exception {
        CaseResult caso = new Mission2Accounts().solve("1\n60 1 0 59\n0 59 9\n").cases().get(0);
        assertNotNull(caso.drawing());
        assertNull(caso.drawingSkippedReason());
    }

    @Test
    void entradaBasuraLanzaErrorLegible() {
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("hola mundo"));
    }

    @Test
    void entradaVaciaLanzaErrorLegible() {
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("   \n  "));
    }

    @Test
    void entradaIncompletaDiceEnQueCasoFallo() {
        InputFormatException error = assertThrows(InputFormatException.class,
                () -> new Mission2Accounts().solve("2\n2 1 0 1\n0 1 5\n3 2 0 2\n0 1 5\n"));
        assertTrue(error.getMessage().startsWith("Error en el caso #2"));
    }

    @Test
    void nodoFueraDeRangoLanzaErrorLegible() {
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("1\n2 1 0 5\n0 1 5\n"));
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("1\n2 1 0 1\n0 2 5\n"));
    }

    @Test
    void pesoNegativoSeRechaza() {
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("1\n2 1 0 1\n0 1 -5\n"));
    }

    @Test
    void numeroDeCasosCeroONegativoSeRechaza() {
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("0\n"));
        assertThrows(InputFormatException.class, () -> new Mission2Accounts().solve("-1\n"));
    }

    @Test
    void datosDeSobraGeneranUnAvisoPeroNoUnError() throws Exception {
        MissionResult resultado = new Mission2Accounts().solve("1\n1 0 0 0\n5 5 5\n");
        assertEquals("Case #1: 0", resultado.consoleOutput());
        assertEquals(1, resultado.warnings().size());
    }
}
