package com.eia.felinegraph.ui.render;

import com.eia.felinegraph.core.draw.GridDrawing;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Renderer de la grilla de la Mision 1.
 *
 * Solo conoce GridDrawing. No conoce BFS, DFS ni Mission1Minefield.
 *
 * Dibuja:
 * - bombas;
 * - origen;
 * - destino;
 * - camino BFS;
 * - camino DFS.
 *
 * Los dos caminos usan estilos diferentes para poder distinguirse
 * incluso cuando comparten celdas.
 */
public final class GridRenderer {

    private static final double DEFAULT_MAX_WIDTH =
            800.0;

    private static final double DEFAULT_MAX_HEIGHT =
            800.0;

    private GridRenderer() {
    }

    public static Canvas render(
            GridDrawing drawing
    ) {

        return render(
                drawing,
                DEFAULT_MAX_WIDTH,
                DEFAULT_MAX_HEIGHT
        );
    }

    public static Canvas render(
            GridDrawing drawing,
            double maxWidth,
            double maxHeight
    ) {

        if (drawing == null) {

            throw new IllegalArgumentException(
                    "GridDrawing no puede ser null."
            );
        }

        if (drawing.rows <= 0
                || drawing.cols <= 0) {

            throw new IllegalArgumentException(
                    "La grilla debe tener dimensiones positivas."
            );
        }

        if (maxWidth <= 0
                || maxHeight <= 0) {

            throw new IllegalArgumentException(
                    "El area maxima de dibujo debe ser positiva."
            );
        }

        double candidate =
                Math.min(
                        maxWidth / drawing.cols,
                        maxHeight / drawing.rows
                );

        double cell =
                Math.max(
                        6.0,
                        Math.min(
                                32.0,
                                candidate
                        )
                );

        double width =
                drawing.cols * cell;

        double height =
                drawing.rows * cell;

        Canvas canvas =
                new Canvas(
                        width,
                        height
                );

        GraphicsContext graphics =
                canvas.getGraphicsContext2D();

        drawBackground(
                graphics,
                width,
                height
        );

        drawMines(
                graphics,
                drawing,
                cell
        );

        drawGrid(
                graphics,
                drawing,
                cell
        );

        /*
         * BFS: verde, linea solida.
         */
        drawPath(
                graphics,
                drawing.bfsPath,
                drawing.cols,
                drawing.rows,
                cell,
                Color.web("#00A67E"),
                false
        );

        /*
         * DFS: morado, linea discontinua.
         */
        drawPath(
                graphics,
                drawing.dfsPath,
                drawing.cols,
                drawing.rows,
                cell,
                Color.web("#7B4AE2"),
                true
        );

        drawEndpoints(
                graphics,
                drawing,
                cell
        );

        return canvas;
    }

    private static void drawBackground(
            GraphicsContext graphics,
            double width,
            double height
    ) {

        graphics.setFill(
                Color.web("#F6F4EF")
        );

        graphics.fillRect(
                0,
                0,
                width,
                height
        );
    }

    private static void drawMines(
            GraphicsContext graphics,
            GridDrawing drawing,
            double cell
    ) {

        graphics.setFill(
                Color.web("#B83A3A")
        );

        for (int row = 0;
             row < drawing.rows;
             row++) {

            for (int col = 0;
                 col < drawing.cols;
                 col++) {

                int index =
                        row * drawing.cols
                                + col;

                if (index < drawing.mine.length
                        && drawing.mine[index]) {

                    graphics.fillRect(
                            col * cell + 1,
                            row * cell + 1,
                            Math.max(
                                    0,
                                    cell - 2
                            ),
                            Math.max(
                                    0,
                                    cell - 2
                            )
                    );
                }
            }
        }
    }

    private static void drawGrid(
            GraphicsContext graphics,
            GridDrawing drawing,
            double cell
    ) {

        graphics.setStroke(
                Color.web("#B9B9B9")
        );

        graphics.setLineWidth(
                0.6
        );

        for (int col = 0;
             col <= drawing.cols;
             col++) {

            double x =
                    col * cell;

            graphics.strokeLine(
                    x,
                    0,
                    x,
                    drawing.rows * cell
            );
        }

        for (int row = 0;
             row <= drawing.rows;
             row++) {

            double y =
                    row * cell;

            graphics.strokeLine(
                    0,
                    y,
                    drawing.cols * cell,
                    y
            );
        }
    }

    private static void drawPath(
            GraphicsContext graphics,
            int[] path,
            int cols,
            int rows,
            double cell,
            Color color,
            boolean dashed
    ) {

        if (path == null
                || path.length == 0) {

            return;
        }

        graphics.save();

        graphics.setStroke(
                color
        );

        graphics.setLineWidth(
                Math.max(
                        2.0,
                        cell * 0.24
                )
        );

        if (dashed) {

            graphics.setLineDashes(
                    Math.max(
                            3.0,
                            cell * 0.55
                    ),
                    Math.max(
                            2.0,
                            cell * 0.30
                    )
            );
        }

        graphics.beginPath();

        boolean started =
                false;

        for (int index : path) {

            if (index < 0
                    || index >= rows * cols) {

                continue;
            }

            int row =
                    index / cols;

            int col =
                    index % cols;

            double x =
                    col * cell
                            + cell / 2.0;

            double y =
                    row * cell
                            + cell / 2.0;

            if (!started) {

                graphics.moveTo(
                        x,
                        y
                );

                started = true;

            } else {

                graphics.lineTo(
                        x,
                        y
                );
            }
        }

        if (started) {
            graphics.stroke();
        }

        graphics.restore();
    }

    private static void drawEndpoints(
            GraphicsContext graphics,
            GridDrawing drawing,
            double cell
    ) {

        if (drawing.startR == drawing.goalR
                && drawing.startC == drawing.goalC) {

            drawMarker(
                    graphics,
                    drawing.startR,
                    drawing.startC,
                    cell,
                    Color.web("#1565C0"),
                    "S/N"
            );

            return;
        }

        drawMarker(
                graphics,
                drawing.startR,
                drawing.startC,
                cell,
                Color.web("#1565C0"),
                "S"
        );

        drawMarker(
                graphics,
                drawing.goalR,
                drawing.goalC,
                cell,
                Color.web("#E58B19"),
                "N"
        );
    }

    private static void drawMarker(
            GraphicsContext graphics,
            int row,
            int col,
            double cell,
            Color color,
            String label
    ) {

        double x =
                col * cell;

        double y =
                row * cell;

        double inset =
                Math.max(
                        1.0,
                        cell * 0.08
                );

        graphics.save();

        graphics.setStroke(
                color
        );

        graphics.setLineWidth(
                Math.max(
                        2.0,
                        cell * 0.14
                )
        );

        graphics.strokeRect(
                x + inset,
                y + inset,
                Math.max(
                        0,
                        cell - 2 * inset
                ),
                Math.max(
                        0,
                        cell - 2 * inset
                )
        );

        graphics.setFill(
                color
        );

        graphics.setFont(
                Font.font(
                        Math.max(
                                8.0,
                                Math.min(
                                        16.0,
                                        cell * 0.52
                                )
                        )
                )
        );

        graphics.fillText(
                label,
                x + cell * 0.18,
                y + cell * 0.67
        );

        graphics.restore();
    }
}