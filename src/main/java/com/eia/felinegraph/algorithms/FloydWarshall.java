package com.eia.felinegraph.algorithms;

import java.util.Arrays;

/**
 * Floyd-Warshall adaptado a maximizacion: en vez del camino mas corto entre cada par de nodos,
 * calcula el mayor churun acumulable sobre cualquier caminata (walk, puede repetir nodos y
 * aristas) entre cada par. Complejidad O(N^3) en tiempo y O(N^2) en espacio: es el algoritmo
 * correcto para esta mision porque el enunciado pide la matriz completa de todos los pares, no
 * solo la distancia desde un origen (eso lo cubre Bellman-Ford).
 */
public final class FloydWarshall {

    public static final long NO_ROUTE = Long.MIN_VALUE;
    public static final long UNBOUNDED = Long.MAX_VALUE;

    private final int n;
    private final long[][] dist;

    public FloydWarshall(int n, int[] edgeFrom, int[] edgeTo, long[] edgeWeight) {
        this.n = n;
        this.dist = new long[n][n];
        for (long[] fila : dist) {
            Arrays.fill(fila, NO_ROUTE);
        }
        for (int i = 0; i < n; i++) {
            dist[i][i] = 0;
        }
        // pares repetidos: se queda el maximo, como pide el enunciado
        for (int e = 0; e < edgeFrom.length; e++) {
            int a = edgeFrom[e];
            int b = edgeTo[e];
            long w = edgeWeight[e];
            if (dist[a][b] == NO_ROUTE || w > dist[a][b]) {
                dist[a][b] = w;
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == NO_ROUTE) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == NO_ROUTE) {
                        continue;
                    }
                    long candidato = dist[i][k] + dist[k][j];
                    if (dist[i][j] == NO_ROUTE || candidato > dist[i][j]) {
                        dist[i][j] = candidato;
                    }
                }
            }
        }

        marcarNoAcotados();
    }

    /**
     * (i, j) es no acotado si y solo si existe k con dist[i][k] finito, dist[k][k] > 0 (hay un
     * ciclo de ganancia positiva alcanzable desde i que pasa por k) y dist[k][j] finito. Se marca
     * sobre una copia booleana primero: si se escribiera UNBOUNDED directamente en dist mientras
     * se recorre, contaminaria la aritmetica de las parejas que todavia faltan por revisar.
     * Los pares que ya son NO_ROUTE se dejan asi: un ciclo positivo no crea alcanzabilidad que no
     * existia, y esto respeta la precedencia del enunciado (inalcanzable gana sobre no acotado).
     */
    private void marcarNoAcotados() {
        boolean[][] noAcotado = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][j] == NO_ROUTE) {
                    continue;
                }
                for (int k = 0; k < n; k++) {
                    if (dist[i][k] != NO_ROUTE && dist[k][k] > 0 && dist[k][j] != NO_ROUTE) {
                        noAcotado[i][j] = true;
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (noAcotado[i][j]) {
                    dist[i][j] = UNBOUNDED;
                }
            }
        }
    }

    public boolean isReachable(int i, int j) {
        return dist[i][j] != NO_ROUTE;
    }

    public boolean isUnbounded(int i, int j) {
        return dist[i][j] == UNBOUNDED;
    }

    public long value(int i, int j) {
        return dist[i][j];
    }

    public int size() {
        return n;
    }
}
