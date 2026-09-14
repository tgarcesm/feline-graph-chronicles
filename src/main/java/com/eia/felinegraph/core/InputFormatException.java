package com.eia.felinegraph.core;

/**
 * Error que se lanza cuando la entrada pegada en la GUI no tiene el formato esperado.
 * La lanzan las cuatro misiones: Mision 1 y 4 (Samuel), Mision 2 (Sebastian), Mision 3 (Tomas).
 * Ejemplo de mensaje: "Se esperaba un numero entero en el token 14, se encontro 'abc'"
 */
public class InputFormatException extends Exception {

    // Toda excepcion de Java es "serializable" y el compilador pide un numero de version.
    // Aqui no se serializa nada, asi que basta con 1.
    private static final long serialVersionUID = 1L;

    public InputFormatException(String mensaje) {
        super(mensaje);
    }
}
