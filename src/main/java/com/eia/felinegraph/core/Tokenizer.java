package com.eia.felinegraph.core;

import java.util.ArrayList;
import java.util.List;

public final class Tokenizer {

    private final List<String> tokens;
    private int siguiente;

    public Tokenizer(String text) {
        tokens = new ArrayList<>();
        siguiente = 0;
        if (text == null) {
            return;
        }
        // split es por si el texto empieza con espacios
        String[] partes = text.split("[\\s\\x{00A0}]+"); // uno o mas espacios en blanco
        for (String parte : partes) {
            if (!parte.isEmpty()) {
                tokens.add(parte);
            }
        }
    }

    // por si faltan tokens por leer
    public boolean hasNext() {
        return siguiente < tokens.size();
    }

    // si no hay token o no es un int tiramos excepcion
    public int nextInt() throws InputFormatException {
        String token = siguienteToken();
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException error) {
            throw new InputFormatException(mensajeNoEsNumero(token));
        }
    }

    public long nextLong() throws InputFormatException {
        String token = siguienteToken();
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException error) {
            throw new InputFormatException(mensajeNoEsNumero(token));
        }
    }

    public int position() {
        return siguiente;
    }

    //token actual y avanza
    private String siguienteToken() throws InputFormatException {
        if (!hasNext()) {
            throw new InputFormatException("Se esperaba un numero en el token " + (siguiente + 1)
                    + ", pero la entrada se termino (tiene " + tokens.size() + " tokens en total)");
        }
        String token = tokens.get(siguiente);
        siguiente++;
        return token;
    }

    private String mensajeNoEsNumero(String token) {
        return "Se esperaba un numero entero en el token " + siguiente + ", se encontro '" + token + "'";
    }
}
