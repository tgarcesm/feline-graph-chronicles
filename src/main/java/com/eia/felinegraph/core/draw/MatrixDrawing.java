package com.eia.felinegraph.core.draw;
/**
 * Modelo neutro de la matriz N x N de Floyd-Warshall paarte de tomas
 */
public final class MatrixDrawing {

    public final int n;
    public final String[][] cells;

    public MatrixDrawing(int n, String[][] cells) {
        this.n = n;
        this.cells = cells;
    }
}
