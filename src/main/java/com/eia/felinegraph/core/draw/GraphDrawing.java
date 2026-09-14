package com.eia.felinegraph.core.draw;

/**
 * Modelo neutro de un grafo listo para dibujar. No importa nada de JavaFX: solo guarda datos.
 * La mision lo llena y GraphRenderer (Sebastian, fase de la GUI) lo pinta en un Canvas.
 aca si ni idea
 */
public final class GraphDrawing {

    public enum Highlight { PATH, CYCLE, MST, NONE }

    public final int nodes;
    public final boolean directed;
    public final int[] edgeFrom;
    public final int[] edgeTo;
    public final long[] edgeWeight;
    public final int start;
    public final int goal;
    public final boolean[] highlighted;
    public final Highlight kind;

    public GraphDrawing(int nodes, boolean directed, int[] edgeFrom, int[] edgeTo, long[] edgeWeight,
                        int start, int goal, boolean[] highlighted, Highlight kind) {
        this.nodes = nodes;
        this.directed = directed;
        this.edgeFrom = edgeFrom;
        this.edgeTo = edgeTo;
        this.edgeWeight = edgeWeight;
        this.start = start;
        this.goal = goal;
        this.highlighted = highlighted;
        this.kind = kind;
    }
}
