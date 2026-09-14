package com.eia.felinegraph;

import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.Tokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    @Test
    void leeNumerosSeparadosPorEspaciosNormales() throws Exception {
        Tokenizer tokenizer = new Tokenizer("3 20 100");
        assertEquals(3, tokenizer.nextInt());
        assertEquals(20, tokenizer.nextInt());
        assertEquals(100, tokenizer.nextInt());
        assertFalse(tokenizer.hasNext());
    }

    @Test
    void toleraLineasEnBlancoIntercaladas() throws Exception {
        Tokenizer tokenizer = new Tokenizer("3\n\n\n2 1\n\n\n0\n");
        assertEquals(3, tokenizer.nextInt());
        assertEquals(2, tokenizer.nextInt());
        assertEquals(1, tokenizer.nextInt());
        assertEquals(0, tokenizer.nextInt());
        assertFalse(tokenizer.hasNext());
    }

    @Test
    void toleraEspaciosSobrantesAlInicioYAlFinal() throws Exception {
        Tokenizer tokenizer = new Tokenizer("   5    6   \n   ");
        assertEquals(5, tokenizer.nextInt());
        assertEquals(6, tokenizer.nextInt());
        assertFalse(tokenizer.hasNext());
    }

    @Test
    void toleraTabulacionesYFinDeLineaDeWindows() throws Exception {
        Tokenizer tokenizer = new Tokenizer("7\t8\t\t9\r\n10\r\n");
        assertEquals(7, tokenizer.nextInt());
        assertEquals(8, tokenizer.nextInt());
        assertEquals(9, tokenizer.nextInt());
        assertEquals(10, tokenizer.nextInt());
        assertFalse(tokenizer.hasNext());
    }

    @Test
    void toleraEspacioNoSeparableCopiadoDeUnPdf() throws Exception {
        Tokenizer tokenizer = new Tokenizer("1\u00A02 \u00A0 3");
        assertEquals(1, tokenizer.nextInt());
        assertEquals(2, tokenizer.nextInt());
        assertEquals(3, tokenizer.nextInt());
    }

    @Test
    void nextLongLeeValoresQueNoCabenEnInt() throws Exception {
        Tokenizer tokenizer = new Tokenizer("9999000000 -5");
        assertEquals(9999000000L, tokenizer.nextLong());
        assertEquals(-5L, tokenizer.nextLong());
    }

    @Test
    void positionDiceElNumeroDelUltimoTokenLeido() throws Exception {
        Tokenizer tokenizer = new Tokenizer("10 20 30");
        assertEquals(0, tokenizer.position());
        tokenizer.nextInt();
        tokenizer.nextInt();
        assertEquals(2, tokenizer.position());
    }

    @Test
    void tokenNoNumericoLanzaErrorLegible() throws Exception {
        Tokenizer tokenizer = new Tokenizer("1 abc");
        tokenizer.nextInt();
        InputFormatException error = assertThrows(InputFormatException.class, () -> tokenizer.nextInt());
        assertEquals("Se esperaba un numero entero en el token 2, se encontro 'abc'", error.getMessage());
    }

    @Test
    void decimalNoEsUnEntero() {
        Tokenizer tokenizer = new Tokenizer("3.5");
        assertThrows(InputFormatException.class, () -> tokenizer.nextLong());
    }

    @Test
    void quedarseSinTokensLanzaErrorLegible() throws Exception {
        Tokenizer tokenizer = new Tokenizer("1");
        tokenizer.nextInt();
        InputFormatException error = assertThrows(InputFormatException.class, () -> tokenizer.nextInt());
        assertTrue(error.getMessage().contains("token 2"));
        assertTrue(error.getMessage().contains("se termino"));
    }

    @Test
    void entradaVaciaNoTieneTokens() {
        Tokenizer tokenizer = new Tokenizer("  \n\n\t ");
        assertFalse(tokenizer.hasNext());
        assertThrows(InputFormatException.class, () -> tokenizer.nextInt());
    }
}
