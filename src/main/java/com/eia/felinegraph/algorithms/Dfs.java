package com.eia.felinegraph.algorithms;

import java.util.Arrays;

/**
 * Depth-First Search iterativo para la Mision 1.
 *
 * El DFS utiliza una pila explicita en lugar de recursion porque
 * las grillas pueden alcanzar 1.000.000 de celdas y una solucion
 * recursiva puede producir StackOverflowError.
 *
 * El orden logico exigido es:
 * arriba, abajo, izquierda, derecha.
 *
 * Como la pila es LIFO, los vecinos se insertan en orden inverso:
 * derecha, izquierda, abajo, arriba.
 *
 * La celda se marca visitada cuando se desapila. Esta es la variante
 * determinista que reproduce DFS 32 en el sample oficial.
 *
 * Complejidad temporal: O(R*C).
 * Complejidad espacial: O(R*C).
 */
public final class Dfs {

    private Dfs() {
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
            return new Result(
                    true,
                    0,
                    new int[]{start}
            );
        }

        boolean[] visited = new boolean[total];

        int[] parent = new int[total];
        int[] depth = new int[total];

        Arrays.fill(parent, -1);
        Arrays.fill(depth, -1);

        LongStack stack =
                new LongStack(
                        Math.min(
                                Math.max(total, 16),
                                1_000_000
                        )
                );

        stack.push(start, -1);

        while (!stack.isEmpty()) {

            long item = stack.pop();

            int current =
                    (int) item;

            int candidateParent =
                    (int) (item >>> 32) - 1;

            /*
             * Es intencional marcar visitado al desapilar.
             * Puede haber duplicados temporales en la pila.
             */
            if (visited[current]) {
                continue;
            }

            visited[current] = true;
            parent[current] = candidateParent;

            if (current == start) {
                depth[current] = 0;
            } else {
                depth[current] =
                        depth[candidateParent] + 1;
            }

            if (current == goal) {
                return reached(
                        start,
                        goal,
                        depth,
                        parent
                );
            }

            int row =
                    current / cols;

            int col =
                    current % cols;

            /*
             * Orden de insercion inverso:
             * derecha, izquierda, abajo, arriba.
             */

            // Derecha
            if (col + 1 < cols) {

                int next =
                        current + 1;

                if (!blocked[next]
                        && !visited[next]) {

                    stack.push(
                            next,
                            current
                    );
                }
            }

            // Izquierda
            if (col > 0) {

                int next =
                        current - 1;

                if (!blocked[next]
                        && !visited[next]) {

                    stack.push(
                            next,
                            current
                    );
                }
            }

            // Abajo
            if (row + 1 < rows) {

                int next =
                        current + cols;

                if (!blocked[next]
                        && !visited[next]) {

                    stack.push(
                            next,
                            current
                    );
                }
            }

            // Arriba
            if (row > 0) {

                int next =
                        current - cols;

                if (!blocked[next]
                        && !visited[next]) {

                    stack.push(
                            next,
                            current
                    );
                }
            }
        }

        return unreachable();
    }

    private static Result reached(
            int start,
            int goal,
            int[] depth,
            int[] parent
    ) {

        int[] path =
                new int[depth[goal] + 1];

        int current =
                goal;

        for (int i = path.length - 1;
             i >= 0;
             i--) {

            path[i] = current;

            if (current == start) {
                break;
            }

            current =
                    parent[current];
        }

        return new Result(
                true,
                depth[goal],
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

        long total =
                (long) rows * cols;

        if (total > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "La grilla es demasiado grande."
            );
        }

        if (blocked == null
                || blocked.length != (int) total) {

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

        if (row < 0
                || row >= rows
                || col < 0
                || col >= cols) {

            throw new IllegalArgumentException(
                    "La coordenada de "
                            + name
                            + " esta fuera de la grilla."
            );
        }
    }

    /**
     * Pila especializada para guardar el par (nodo, padre)
     * sin crear objetos por cada entrada.
     */
    private static final class LongStack {

        private long[] data;
        private int size;

        private LongStack(int capacity) {

            data =
                    new long[
                            Math.max(
                                    1,
                                    capacity
                            )
                            ];
        }

        private boolean isEmpty() {
            return size == 0;
        }

        private void push(
                int node,
                int parent
        ) {

            if (size == data.length) {

                data =
                        Arrays.copyOf(
                                data,
                                data.length
                                        + (data.length >> 1)
                                        + 1
                        );
            }

            /*
             * parent + 1 permite representar -1 como 0.
             * Los 32 bits bajos guardan el nodo.
             */
            data[size++] =
                    ((long) (parent + 1) << 32)
                            | (node & 0xffffffffL);
        }

        private long pop() {

            if (size == 0) {

                throw new IllegalStateException(
                        "La pila esta vacia."
                );
            }

            return data[--size];
        }
    }
}