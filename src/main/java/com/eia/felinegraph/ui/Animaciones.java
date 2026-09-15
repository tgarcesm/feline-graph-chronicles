package com.eia.felinegraph.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Animaciones cortas que se reutilizan en toda la interfaz.
 *
 * Todas usan el paquete javafx.animation: una "Transition" cambia una propiedad de un nodo
 * (opacidad, escala, posicion) durante un tiempo, y un "Timeline" ejecuta pasos cada cierto
 * tiempo. Ninguna bloquea la ventana: JavaFX las corre mientras el usuario sigue usando la app.
 */
public final class Animaciones {

    private Animaciones() {
    }

    /**
     * Efecto maquina de escribir: agrega una letra cada msPorLetra milisegundos.
     * Devuelve el Timeline para poder detenerlo si llega otro texto antes de terminar.
     */
    public static Timeline escribir(Label etiqueta, String texto, double msPorLetra) {
        etiqueta.setText("");
        Timeline escritura = new Timeline();
        if (texto.isEmpty()) {
            return escritura;
        }
        escritura.getKeyFrames().add(new KeyFrame(Duration.millis(msPorLetra), evento -> agregarLetra(etiqueta, texto)));
        escritura.setCycleCount(texto.length());
        escritura.play();
        return escritura;
    }

    /** Un paso de la maquina de escribir: el largo del texto actual sirve de contador. */
    private static void agregarLetra(Label etiqueta, String texto) {
        int siguiente = etiqueta.getText().length() + 1;
        if (siguiente <= texto.length()) {
            etiqueta.setText(texto.substring(0, siguiente));
        }
    }

    /** Aparicion con rebote: el nodo crece un poco de mas y luego se acomoda, mientras se vuelve visible. */
    public static void aparecer(Node nodo) {
        aparecer(nodo, 0);
    }

    /** Igual que aparecer(nodo), pero esperando retrasoMs antes de empezar (sirve para escalonar varios). */
    public static void aparecer(Node nodo, double retrasoMs) {
        nodo.setOpacity(0);
        nodo.setScaleX(0.6);
        nodo.setScaleY(0.6);

        FadeTransition visible = new FadeTransition(Duration.millis(320), nodo);
        visible.setToValue(1);
        ScaleTransition crecer = new ScaleTransition(Duration.millis(260), nodo);
        crecer.setToX(1.08);
        crecer.setToY(1.08);
        ScaleTransition asentar = new ScaleTransition(Duration.millis(140), nodo);
        asentar.setToX(1);
        asentar.setToY(1);

        ParallelTransition todo = new ParallelTransition(visible, new SequentialTransition(crecer, asentar));
        todo.setDelay(Duration.millis(retrasoMs));
        todo.play();
    }

    /** Sacudida horizontal: se usa cuando gana un villano o cuando la entrada tiene un error. */
    public static void sacudir(Node nodo) {
        TranslateTransition sacudida = new TranslateTransition(Duration.millis(55), nodo);
        sacudida.setFromX(0);
        sacudida.setByX(9);
        sacudida.setCycleCount(6);
        sacudida.setAutoReverse(true);
        sacudida.setOnFinished(evento -> nodo.setTranslateX(0));
        sacudida.play();
    }

    /** Dos saltitos de celebracion hacia arriba. */
    public static void saltar(Node nodo) {
        TranslateTransition salto = new TranslateTransition(Duration.millis(170), nodo);
        salto.setFromY(0);
        salto.setByY(-20);
        salto.setCycleCount(4);
        salto.setAutoReverse(true);
        salto.setOnFinished(evento -> nodo.setTranslateY(0));
        salto.play();
    }

    /** Desvanece el nodo y, cuando termina, ejecuta la accion indicada (por ejemplo, quitarlo de la pantalla). */
    public static void desvanecer(Node nodo, double milisegundos, Runnable alTerminar) {
        FadeTransition salida = new FadeTransition(Duration.millis(milisegundos), nodo);
        salida.setToValue(0);
        salida.setOnFinished(evento -> alTerminar.run());
        salida.play();
    }

    /** Cambia la escala de un nodo suavemente (efecto al pasar el mouse por encima). */
    public static void escalar(Node nodo, double escala) {
        ScaleTransition cambio = new ScaleTransition(Duration.millis(130), nodo);
        cambio.setToX(escala);
        cambio.setToY(escala);
        cambio.play();
    }
}
