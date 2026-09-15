package com.eia.felinegraph.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion JavaFX (lo arranca "mvn clean javafx:run").
 *
 * Primero muestra la portada con la historia (StoryScreen); al aceptar la mision, la portada se
 * desvanece y queda el tablero del juego (MainView). Ademas instala un manejador global de
 * errores: si algo inesperado explota en cualquier parte, se muestra un mensaje legible en una
 * ventana en vez de un stack trace en la consola.
 */
public final class App extends Application {

    private final StackPane raiz = new StackPane();
    private StoryScreen portada;

    @Override
    public void start(Stage ventana) {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> reportarErrorInesperado(error));

        portada = new StoryScreen(() -> comenzarAventura());
        raiz.getChildren().add(portada);

        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(1440, pantalla.getWidth() * 0.95);
        double alto = Math.min(900, pantalla.getHeight() * 0.95);
        Scene escena = new Scene(raiz, ancho, alto);
        escena.getStylesheets().add(App.class.getResource(Theme.CSS).toExternalForm());

        ventana.setTitle("The Feline Graph Chronicles");
        Image icono = Theme.imagen("pola");
        if (icono != null) {
            ventana.getIcons().add(icono);
        }
        ventana.setMinWidth(Math.min(1100, ancho));
        ventana.setMinHeight(Math.min(700, alto));
        ventana.setScene(escena);
        ventana.show();
        portada.iniciar();
    }

    /** Pone el tablero debajo de la portada y desvanece la portada; al terminar la quita. */
    private void comenzarAventura() {
        if (portada == null) {
            return;
        }
        StoryScreen saliente = portada;
        portada = null;
        raiz.getChildren().add(0, new MainView());
        Animaciones.desvanecer(saliente, 700, () -> {
            saliente.detener();
            raiz.getChildren().remove(saliente);
        });
    }

    /** Muestra cualquier error no controlado como un mensaje legible (nunca como stack trace). */
    private void reportarErrorInesperado(Throwable error) {
        String detalle = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        if (Platform.isFxApplicationThread()) {
            mostrarAlerta(detalle);
        } else {
            Platform.runLater(() -> mostrarAlerta(detalle));
        }
    }

    private void mostrarAlerta(String detalle) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Algo salió mal");
        alerta.setHeaderText("Nero metió la pata en algún lado");
        alerta.setContentText("Ocurrió un error inesperado, pero la aplicación sigue funcionando.\n\nDetalle: " + detalle);
        alerta.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
