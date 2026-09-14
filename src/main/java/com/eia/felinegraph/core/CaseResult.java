package com.eia.felinegraph.core;

import com.eia.felinegraph.core.draw.MatrixDrawing;

public final class CaseResult {

    private final int caseNumber;
    private final String line;
    private final Object drawing;
    private final MatrixDrawing matrix;
    private final String drawingSkippedReason;

    public CaseResult(int caseNumber, String line, Object drawing,
                      MatrixDrawing matrix, String drawingSkippedReason) {
        this.caseNumber = caseNumber;
        this.line = line;
        this.drawing = drawing;
        this.matrix = matrix;
        this.drawingSkippedReason = drawingSkippedReason;
    }

    public int caseNumber() {
        return caseNumber;
    }

    public String line() {
        return line;
    }

    public Object drawing() {
        return drawing;
    }

    public MatrixDrawing matrix() {
        return matrix;
    }

    public String drawingSkippedReason() {
        return drawingSkippedReason;
    }
}
