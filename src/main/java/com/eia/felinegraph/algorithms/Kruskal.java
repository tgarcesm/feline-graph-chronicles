package com.eia.felinegraph.algorithms;

import java.util.Arrays;

/**
 * Algoritmo de Kruskal para construir un Minimum Spanning Tree.
 *
 * Es apropiado para la Mision 4 porque el problema pide conectar todas
 * las intersecciones con costo total minimo en un grafo no dirigido.
 *
 * Las aristas se ordenan por costo y se aceptan solamente cuando unen
 * dos componentes diferentes del Union-Find.
 *
 * Complejidad temporal: O(C log C), dominada por ordenar las aristas.
 * Complejidad espacial: O(C + N).
 */
public final class Kruskal {

    private Kruskal() {
    }

    public static final class Edge {

        private final int from;
        private final int to;
        private final long weight;
        private final int originalIndex;

        public Edge(
                int from,
                int to,
                long weight,
                int originalIndex
        ) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.originalIndex = originalIndex;
        }

        public int from() {
            return from;
        }

        public int to() {
            return to;
        }

        public long weight() {
            return weight;
        }

        public int originalIndex() {
            return originalIndex;
        }
    }

    public static final class Result {

        private final boolean connected;
        private final long totalCost;
        private final int acceptedEdges;
        private final boolean[] selected;

        private Result(
                boolean connected,
                long totalCost,
                int acceptedEdges,
                boolean[] selected
        ) {

            this.connected = connected;
            this.totalCost = totalCost;
            this.acceptedEdges = acceptedEdges;
            this.selected = selected;
        }

        public boolean connected() {
            return connected;
        }

        public long totalCost() {
            return totalCost;
        }

        public int acceptedEdges() {
            return acceptedEdges;
        }

        public boolean[] selected() {
            return selected.clone();
        }
    }

    public static Result minimumSpanningTree(
            int nodeCount,
            Edge[] edges
    ) {

        validateInput(
                nodeCount,
                edges
        );

        if (nodeCount == 1) {

            return new Result(
                    true,
                    0L,
                    0,
                    new boolean[
                            edges.length
                            ]
            );
        }

        Edge[] sorted =
                edges.clone();

        Arrays.sort(
                sorted,
                (first, second) -> {

                    int byWeight =
                            Long.compare(
                                    first.weight,
                                    second.weight
                            );

                    if (byWeight != 0) {
                        return byWeight;
                    }

                    return Integer.compare(
                            first.originalIndex,
                            second.originalIndex
                    );
                }
        );

        DisjointSet dsu =
                new DisjointSet(
                        nodeCount
                );

        boolean[] selected =
                new boolean[
                        edges.length
                        ];

        long totalCost =
                0L;

        int accepted =
                0;

        for (Edge edge : sorted) {

            /*
             * Los lazos se descartan automaticamente porque
             * union(x, x) devuelve false.
             */
            if (!dsu.union(
                    edge.from,
                    edge.to
            )) {
                continue;
            }

            selected[
                    edge.originalIndex
                    ] = true;

            totalCost +=
                    edge.weight;

            accepted++;

            if (accepted
                    == nodeCount - 1) {

                break;
            }
        }

        return new Result(
                accepted
                        == nodeCount - 1,
                totalCost,
                accepted,
                selected
        );
    }

    public static Result minimumSpanningTree(
            int nodeCount,
            int[] from,
            int[] to,
            long[] weight
    ) {

        if (from == null
                || to == null
                || weight == null) {

            throw new IllegalArgumentException(
                    "Los arreglos de aristas no pueden ser null."
            );
        }

        if (from.length != to.length
                || from.length != weight.length) {

            throw new IllegalArgumentException(
                    "Los arreglos de aristas deben tener igual longitud."
            );
        }

        Edge[] edges =
                new Edge[
                        from.length
                        ];

        for (int i = 0;
             i < from.length;
             i++) {

            edges[i] =
                    new Edge(
                            from[i],
                            to[i],
                            weight[i],
                            i
                    );
        }

        return minimumSpanningTree(
                nodeCount,
                edges
        );
    }

    private static void validateInput(
            int nodeCount,
            Edge[] edges
    ) {

        if (nodeCount <= 0) {

            throw new IllegalArgumentException(
                    "El numero de nodos debe ser positivo."
            );
        }

        if (edges == null) {

            throw new IllegalArgumentException(
                    "El arreglo de aristas no puede ser null."
            );
        }

        boolean[] seen =
                new boolean[
                        edges.length
                        ];

        for (int i = 0;
             i < edges.length;
             i++) {

            Edge edge =
                    edges[i];

            if (edge == null) {

                throw new IllegalArgumentException(
                        "Arista null en posicion "
                                + i
                );
            }

            if (edge.from < 0
                    || edge.from >= nodeCount
                    || edge.to < 0
                    || edge.to >= nodeCount) {

                throw new IllegalArgumentException(
                        "Arista con nodo fuera de rango."
                );
            }

            if (edge.originalIndex < 0
                    || edge.originalIndex
                    >= edges.length
                    || seen[
                    edge.originalIndex
                    ]) {

                throw new IllegalArgumentException(
                        "Indice original de arista invalido."
                );
            }

            seen[
                    edge.originalIndex
                    ] = true;
        }
    }
}