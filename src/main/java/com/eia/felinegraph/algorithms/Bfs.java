package com.eia.felinegraph.algorithms;

import java.util.Arrays;

/**
 * Breadth-First Search sobre una grilla no ponderada.
 *
 * La grilla se almacena en un arreglo plano de R*C posiciones:
 * index = row * cols + col.
 *
 * BFS es la eleccion correcta para la Mision 1 porque todas las
 * aristas de la grilla tienen costo 1 y, por tanto, BFS encuentra
 * el camino con el minimo numero de movimientos.
 *
 * Complejidad temporal: O(R*C).
 * Complejidad espacial: O(R*C).
 */
public final class Bfs {

    private Bfs() {
    }

    public static final class Result {

        private final boolean reachable;
        private final int distance;
        private final int[] path;

        private Result(boolean reachable, int distance, int[] path) {
            this.reachable = reachable;
            this.distance = distance;
            this.path = path;
        }

        public boolean reachable() {
            return reachable;
        }

        public int distance() {
            return distance;
        }

        public int[] path() {
            return path.clone();
        }
    }

    public static Result search(
            int rows,
            int cols,
            boolean[] blocked,
            int startR,
            int startC,
            int goalR,
            int goalC
    ) {
        validateGrid(rows, cols, blocked);
        validateCoordinate(startR, startC, rows, cols, "origen");
        validateCoordinate(goalR, goalC, rows, cols, "destino");

        return search(
                rows,
                cols,
                blocked,
                startR * cols + startC,
                goalR * cols + goalC
        );
    }

    public static Result search(
            int rows,
            int cols,
            boolean[] blocked,
            int start,
            int goal
    ) {
        validateGrid(rows, cols, blocked);

        int total = rows * cols;

        if (start < 0 || start >= total || goal < 0 || goal >= total) {
            throw new IllegalArgumentException(
                    "Origen o destino fuera de la grilla."
            );
        }

        if (blocked[start] || blocked[goal]) {
            return unreachable();
        }

        if (start == goal) {
            return new Result(true, 0, new int[]{start});
        }

        int[] distance = new int[total];
        int[] parent = new int[total];

        Arrays.fill(distance, -1);
        Arrays.fill(parent, -1);

        int[] queue = new int[total];

        int head = 0;
        int tail = 0;

        distance[start] = 0;
        queue[tail++] = start;

        while (head < tail) {

            int current = queue[head++];

            int row = current / cols;
            int col = current % cols;

            // Arriba
            if (row > 0) {
                int next = current - cols;

                if (!blocked[next] && distance[next] == -1) {
                    distance[next] = distance[current] + 1;
                    parent[next] = current;

                    if (next == goal) {
                        return reached(start, goal, distance, parent);
                    }

                    queue[tail++] = next;
                }
            }

            // Abajo
            if (row + 1 < rows) {
                int next = current + cols;

                if (!blocked[next] && distance[next] == -1) {
                    distance[next] = distance[current] + 1;
                    parent[next] = current;

                    if (next == goal) {
                        return reached(start, goal, distance, parent);
                    }

                    queue[tail++] = next;
                }
            }

            // Izquierda
            if (col > 0) {
                int next = current - 1;

                if (!blocked[next] && distance[next] == -1) {
                    distance[next] = distance[current] + 1;
                    parent[next] = current;

                    if (next == goal) {
                        return reached(start, goal, distance, parent);
                    }

                    queue[tail++] = next;
                }
            }

            // Derecha
            if (col + 1 < cols) {
                int next = current + 1;

                if (!blocked[next] && distance[next] == -1) {
                    distance[next] = distance[current] + 1;
                    parent[next] = current;

                    if (next == goal) {
                        return reached(start, goal, distance, parent);
                    }

                    queue[tail++] = next;
                }
            }
        }

        return unreachable();
    }

    private static Result reached(
            int start,
            int goal,
            int[] distance,
            int[] parent
    ) {
        int[] path = new int[distance[goal] + 1];

        int current = goal;

        for (int i = path.length - 1; i >= 0; i--) {

            path[i] = current;

            if (current == start) {
                break;
            }

            current = parent[current];
        }

        return new Result(
                true,
                distance[goal],
                path
        );
    }

    private static Result unreachable() {
        return new Result(
                false,
                -1,
                new int[0]
        );
    }

    private static void validateGrid(
            int rows,
            int cols,
            boolean[] blocked
    ) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException(
                    "Las dimensiones deben ser positivas."
            );
        }

        long total = (long) rows * cols;

        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "La grilla es demasiado grande."
            );
        }

        if (blocked == null || blocked.length != (int) total) {
            throw new IllegalArgumentException(
                    "El arreglo de bombas no coincide con la grilla."
            );
        }
    }

    private static void validateCoordinate(
            int row,
            int col,
            int rows,
            int cols,
            String name
    ) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IllegalArgumentException(
                    "La coordenada de " + name + " esta fuera de la grilla."
            );
        }
    }
}