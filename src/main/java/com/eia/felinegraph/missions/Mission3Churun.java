package com.eia.felinegraph.missions;

import com.eia.felinegraph.algorithms.BellmanFord;
import com.eia.felinegraph.algorithms.FloydWarshall;
import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.Mission;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.Tokenizer;
import com.eia.felinegraph.core.draw.GraphDrawing;
import com.eia.felinegraph.core.draw.MatrixDrawing;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta la Mision 3: parsea la entrada, corre Floyd-Warshall y Bellman-Ford sobre cada caso,
 * aplica la precedencia de los tres mensajes de salida, arma la matriz N x N y el dibujo del
 * grafo, y compara los dos algoritmos entre si (cross-check). Los algoritmos viven aparte en
 * algorithms/FloydWarshall.java y algorithms/BellmanFord.java; esta clase es la unica que sabe
 * como se lee y se muestra la Mision 3.
 */
public final class Mission3Churun implements Mission {

    private static final int MIN_N = 1;
    private static final int MAX_N = 100;
    private static final int MIN_M = 0;
    private static final int MAX_M = 5000;
    private static final long MIN_PESO = -1000L;
    private static final long MAX_PESO = 1000L;

    // hasta 60 nodos se dibuja el grafo; la matriz siempre cabe porque N nunca pasa de 100
    private static final int MAX_NODOS_DIBUJO_GRAFO = 60;

    private static final String CATEGORIA_INALCANZABLE = "UNREACHABLE";
    private static final String CATEGORIA_NO_ACOTADO = "UNBOUNDED";
    private static final String CATEGORIA_FINITA = "FINITE";

    private static final String MENSAJE_INALCANZABLE = "Limon blocked the way";
    private static final String MENSAJE_NO_ACOTADO = "Infinite churun!";

    private static final String SAMPLE =
            "3\n" +
            "5 7 0 4\n" +
            "0 1 50\n" +
            "0 2 10\n" +
            "1 2 -30\n" +
            "1 3 40\n" +
            "2 1 -5\n" +
            "2 3 60\n" +
            "3 4 20\n" +
            "4 4 0 3\n" +
            "0 1 20\n" +
            "1 2 30\n" +
            "2 1 -10\n" +
            "2 3 15\n" +
            "3 3 0 2\n" +
            "0 1 -40\n" +
            "1 2 -25\n" +
            "0 2 -80\n";

    @Override
    public String title() {
        return "Mision 3 - La reserva definitiva de churun";
    }

    @Override
    public String sampleInput() {
        return SAMPLE;
    }

    /**
     * Lee T y resuelve los T casos en orden. Si un caso tiene un error de formato, el mensaje se
     * vuelve a lanzar diciendo en que caso fue. Si sobran datos al final no se considera error,
     * pero se avisa en warnings; igual que si Floyd-Warshall y Bellman-Ford no coinciden en algun
     * caso, cosa que no deberia pasar nunca si ambos estan bien implementados.
     */
    @Override
    public MissionResult solve(String rawInput) throws InputFormatException {
        Tokenizer tokenizer = new Tokenizer(rawInput);
        if (!tokenizer.hasNext()) {
            throw new InputFormatException("La entrada esta vacia: pegue los datos de la mision o use el boton de cargar ejemplo");
        }

        int totalCasos = tokenizer.nextInt();
        if (totalCasos < 1) {
            throw new InputFormatException("El numero de casos T debe ser al menos 1, se encontro " + totalCasos);
        }

        List<CaseResult> casos = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        for (int k = 1; k <= totalCasos; k++) {
            try {
                casos.add(resolverCaso(tokenizer, k, avisos));
            } catch (InputFormatException error) {
                throw new InputFormatException("Error en el caso #" + k + ": " + error.getMessage());
            }
        }

        if (tokenizer.hasNext()) {
            avisos.add("Aviso: la entrada tiene datos de sobra despues del caso #" + totalCasos + ", se ignoraron");
        }
        return new MissionResult(casos, avisos);
    }

    private CaseResult resolverCaso(Tokenizer tokenizer, int numeroCaso, List<String> avisos) throws InputFormatException {
        int n = leerEntero(tokenizer, "N", MIN_N, MAX_N);
        int m = leerEntero(tokenizer, "M", MIN_M, MAX_M);
        int origen = leerEntero(tokenizer, "S", 0, n - 1);
        int destino = leerEntero(tokenizer, "D", 0, n - 1);

        int[] desde = new int[m];
        int[] hasta = new int[m];
        long[] peso = new long[m];
        for (int i = 0; i < m; i++) {
            desde[i] = leerEntero(tokenizer, "A", 0, n - 1);
            hasta[i] = leerEntero(tokenizer, "B", 0, n - 1);
            peso[i] = leerPeso(tokenizer);
        }

        FloydWarshall floyd = new FloydWarshall(n, desde, hasta, peso);
        BellmanFord bellman = new BellmanFord(n, desde, hasta, peso, origen);

        String categoriaFloyd = categoria(floyd.isReachable(origen, destino), floyd.isUnbounded(origen, destino));
        long valorFloyd = floyd.value(origen, destino);
        String categoriaBellman = categoria(bellman.isReachable(destino), bellman.isUnbounded(destino));
        long valorBellman = bellman.value(destino);

        String discrepancia = discrepancyWarning(numeroCaso, categoriaFloyd, valorFloyd, categoriaBellman, valorBellman);
        if (discrepancia != null) {
            avisos.add(discrepancia);
        }

        String linea = formatearLinea(numeroCaso, categoriaFloyd, valorFloyd);
        MatrixDrawing matriz = construirMatriz(n, floyd);

        Object dibujoGrafo = null;
        String razonOmision = null;
        if (n <= MAX_NODOS_DIBUJO_GRAFO) {
            dibujoGrafo = construirDibujoGrafo(n, desde, hasta, peso, origen, destino, categoriaFloyd, bellman);
        } else {
            razonOmision = "Dibujo omitido: la instancia tiene " + n + " nodos y el limite es " + MAX_NODOS_DIBUJO_GRAFO;
        }

        return new CaseResult(numeroCaso, linea, dibujoGrafo, matriz, razonOmision);
    }

    private String categoria(boolean alcanzable, boolean noAcotado) {
        if (!alcanzable) {
            return CATEGORIA_INALCANZABLE;
        }
        if (noAcotado) {
            return CATEGORIA_NO_ACOTADO;
        }
        return CATEGORIA_FINITA;
    }

    /**
     * Compara la categoria y el valor que dan Floyd-Warshall y Bellman-Ford para el mismo (S, D).
     * Publico y estatico a proposito: el enunciado pide poder forzar el aviso de discrepancia al
     * menos una vez para demostrar que funciona, y esta es la funcion que decide si se dispara.
     */
    public static String discrepancyWarning(int numeroCaso, String categoriaFloyd, long valorFloyd,
                                             String categoriaBellman, long valorBellman) {
        boolean coinciden = categoriaFloyd.equals(categoriaBellman)
                && (!CATEGORIA_FINITA.equals(categoriaFloyd) || valorFloyd == valorBellman);
        if (coinciden) {
            return null;
        }
        return "Aviso: Floyd-Warshall y Bellman-Ford no coinciden en el caso #" + numeroCaso;
    }

    // linea exacta de salida, en el orden de precedencia que pide el enunciado
    private String formatearLinea(int numeroCaso, String categoria, long valor) {
        if (CATEGORIA_INALCANZABLE.equals(categoria)) {
            return "Case #" + numeroCaso + ": " + MENSAJE_INALCANZABLE;
        }
        if (CATEGORIA_NO_ACOTADO.equals(categoria)) {
            return "Case #" + numeroCaso + ": " + MENSAJE_NO_ACOTADO;
        }
        return "Case #" + numeroCaso + ": " + valor;
    }

    private MatrixDrawing construirMatriz(int n, FloydWarshall floyd) {
        String[][] celdas = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!floyd.isReachable(i, j)) {
                    celdas[i][j] = "-";
                } else if (floyd.isUnbounded(i, j)) {
                    celdas[i][j] = "inf";
                } else {
                    celdas[i][j] = String.valueOf(floyd.value(i, j));
                }
            }
        }
        return new MatrixDrawing(n, celdas);
    }

    private GraphDrawing construirDibujoGrafo(int n, int[] desde, int[] hasta, long[] peso, int origen,
                                              int destino, String categoria, BellmanFord bellman) {
        boolean[] resaltada = new boolean[desde.length];
        GraphDrawing.Highlight tipo = GraphDrawing.Highlight.NONE;
        if (CATEGORIA_FINITA.equals(categoria)) {
            int[] camino = bellman.pathEdgesTo(destino);
            for (int id : camino) {
                resaltada[id] = true;
            }
            tipo = GraphDrawing.Highlight.PATH;
        } else if (CATEGORIA_NO_ACOTADO.equals(categoria)) {
            int[] ciclo = bellman.responsibleCycleEdges(destino);
            for (int id : ciclo) {
                resaltada[id] = true;
            }
            tipo = GraphDrawing.Highlight.CYCLE;
        }
        return new GraphDrawing(n, true, desde, hasta, peso, origen, destino, resaltada, tipo);
    }

    private int leerEntero(Tokenizer tokenizer, String nombre, int minimo, int maximo) throws InputFormatException {
        int valor = tokenizer.nextInt();
        if (valor < minimo || valor > maximo) {
            throw new InputFormatException(nombre + " debe estar entre " + minimo + " y " + maximo
                    + ", se encontro " + valor + " (token " + tokenizer.position() + ")");
        }
        return valor;
    }

    private long leerPeso(Tokenizer tokenizer) throws InputFormatException {
        long valor = tokenizer.nextLong();
        if (valor < MIN_PESO || valor > MAX_PESO) {
            throw new InputFormatException("W debe estar entre " + MIN_PESO + " y " + MAX_PESO
                    + ", se encontro " + valor + " (token " + tokenizer.position() + ")");
        }
        return valor;
    }
}
