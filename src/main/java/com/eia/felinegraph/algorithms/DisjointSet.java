package com.eia.felinegraph.algorithms;

/**
 * Union-Find / Disjoint Set Union.
 *
 * Usa las dos optimizaciones requeridas por la Mision 4:
 * - compresion de caminos;
 * - union por tamano.
 *
 * Cada find/union tiene costo amortizado O(alpha(N)), practicamente
 * constante para los limites del proyecto.
 *
 * Complejidad espacial: O(N).
 */
public final class DisjointSet {

    private final int[] parent;
    private final int[] size;

    private int components;

    public DisjointSet(int elements) {

        if (elements < 0) {

            throw new IllegalArgumentException(
                    "El numero de elementos no puede ser negativo."
            );
        }

        parent =
                new int[elements];

        size =
                new int[elements];

        components =
                elements;

        for (int i = 0;
             i < elements;
             i++) {

            parent[i] = i;
            size[i] = 1;
        }
    }

    /**
     * Find con compresion completa del camino.
     */
    public int find(
            int element
    ) {

        validateElement(element);

        int root =
                element;

        while (root != parent[root]) {
            root = parent[root];
        }

        int current =
                element;

        while (current != root) {

            int next =
                    parent[current];

            parent[current] =
                    root;

            current =
                    next;
        }

        return root;
    }

    /**
     * Une por tamano.
     *
     * @return true si se unieron dos componentes diferentes,
     *         false si ya pertenecian a la misma componente.
     */
    public boolean union(
            int first,
            int second
    ) {

        int rootFirst =
                find(first);

        int rootSecond =
                find(second);

        if (rootFirst == rootSecond) {
            return false;
        }

        if (size[rootFirst]
                < size[rootSecond]) {

            int temporary =
                    rootFirst;

            rootFirst =
                    rootSecond;

            rootSecond =
                    temporary;
        }

        parent[rootSecond] =
                rootFirst;

        size[rootFirst] +=
                size[rootSecond];

        components--;

        return true;
    }

    public boolean connected(
            int first,
            int second
    ) {
        return find(first)
                == find(second);
    }

    public int componentSize(
            int element
    ) {
        return size[
                find(element)
                ];
    }

    public int components() {
        return components;
    }

    public int elementCount() {
        return parent.length;
    }

    private void validateElement(
            int element
    ) {

        if (element < 0
                || element >= parent.length) {

            throw new IndexOutOfBoundsException(
                    "Elemento fuera de rango: "
                            + element
            );
        }
    }
}