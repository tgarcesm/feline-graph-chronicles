package com.eia.felinegraph.core.draw;

/**
 * Modelo neutro de la grilla de samuel
 */
public final class GridDrawing {

    public final int rows;
    public final int cols;
    public final boolean[] mine;
    public final int startR;
    public final int startC;
    public final int goalR;
    public final int goalC;
    public final int[] bfsPath;
    public final int[] dfsPath;

    public GridDrawing(int rows, int cols, boolean[] mine, int startR, int startC,
                       int goalR, int goalC, int[] bfsPath, int[] dfsPath) {
        this.rows = rows;
        this.cols = cols;
        this.mine = mine;
        this.startR = startR;
        this.startC = startC;
        this.goalR = goalR;
        this.goalC = goalC;
        this.bfsPath = bfsPath;
        this.dfsPath = dfsPath;
    }
}
