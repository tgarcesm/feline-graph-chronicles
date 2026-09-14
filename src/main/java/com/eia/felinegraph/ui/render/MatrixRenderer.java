package com.eia.felinegraph.ui.render;

import com.eia.felinegraph.core.draw.MatrixDrawing;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 * Dibuja la matriz N x N de Floyd-Warshall (Mision 3) dentro de un panel con scroll, tal como
 * exige la seccion 2.3 del enunciado para todo N hasta 100 (N nunca supera 100 en esta mision,
 * asi que la matriz nunca se omite). No conoce nada de FloydWarshall ni de Mission3Churun: solo
 * recibe el modelo neutro MatrixDrawing y lo pinta, igual que GraphRenderer con GraphDrawing.
 */
public final class MatrixRenderer {

    private static final double CELL_SIZE = 36;

    private MatrixRenderer() {
    }

    public static ScrollPane render(MatrixDrawing matriz) {
        GridPane grilla = new GridPane();
        grilla.setHgap(1);
        grilla.setVgap(1);
        grilla.setPadding(new Insets(4));

        grilla.add(celdaEncabezado(""), 0, 0);
        for (int j = 0; j < matriz.n; j++) {
            grilla.add(celdaEncabezado(String.valueOf(j)), j + 1, 0);
        }
        for (int i = 0; i < matriz.n; i++) {
            grilla.add(celdaEncabezado(String.valueOf(i)), 0, i + 1);
            for (int j = 0; j < matriz.n; j++) {
                grilla.add(celdaValor(matriz.cells[i][j]), j + 1, i + 1);
            }
        }

        ScrollPane panel = new ScrollPane(grilla);
        panel.setFitToWidth(false);
        panel.setFitToHeight(false);
        return panel;
    }

    private static Label celdaEncabezado(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-weight: bold;");
        dimensionar(label);
        return label;
    }

    private static Label celdaValor(String valor) {
        Label label = new Label(valor);
        if ("inf".equals(valor)) {
            label.setTextFill(Color.web("#b30000"));
        } else if ("-".equals(valor)) {
            label.setTextFill(Color.GRAY);
        }
        dimensionar(label);
        return label;
    }

    private static void dimensionar(Label label) {
        label.setMinWidth(CELL_SIZE);
        label.setPrefWidth(CELL_SIZE);
        label.setAlignment(Pos.CENTER);
    }
}
