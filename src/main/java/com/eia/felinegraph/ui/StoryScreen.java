package com.eia.felinegraph.ui;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Portada del juego: cuenta la historia antes de empezar.
 *
 * Fondo: la ilustracion de la batalla final, desenfocada y con un zoom muy lento. Encima, el
 * "expediente" con Limon palpitando y el texto de la historia escribiendose letra por letra.
 * Abajo se presentan los cuatro gatos (heroinas contra villanos) y el boton para empezar.
 * Hacer clic sobre el texto lo completa de una vez, por si el usuario no quiere esperar.
 */
public final class StoryScreen extends StackPane {

    private static final String HISTORIA =
            "En las sombras del laboratorio de Lenguajes y Compiladores, Limón, el líder del mal, y su secuaz "
            + "Nero robaron todas las cuentas de Claude Pro del curso. Su plan: usar todo ese poder para conseguir "
            + "churun infinito, su golosina favorita.\n\n"
            + "Y para que nadie los detuviera, secuestraron a Nina, la gata de la casa de Sebas.\n\n"
            + "Pola y Minerva encontraron las pistas que dejaron los villanos: cuatro grafos. Resuélvelos en el "
            + "orden que quieras. Cada misión rompe un barrote de la jaula de Nina.";

    private final Label texto = new Label();
    private final Region fondo = new Region();
    private final List<Animation> animaciones = new ArrayList<>();
    private Timeline escritura;

    public StoryScreen(Runnable alAceptar) {
        getStyleClass().add("pantalla-historia");

        fondo.getStyleClass().add("fondo-batalla");
        fondo.setEffect(new GaussianBlur(9));
        Region velo = new Region();
        velo.getStyleClass().add("velo");

        Label sello = new Label("EXPEDIENTE CLASIFICADO  ·  CASO #EIA-2026");
        sello.getStyleClass().add("expediente");
        Label titulo = new Label("The Feline Graph Chronicles");
        titulo.getStyleClass().add("historia-titulo");

        CatCharacter limon = new CatCharacter("limon", 170);
        texto.getStyleClass().add("historia-texto");
        texto.setWrapText(true);
        texto.setPrefWidth(600);
        texto.setMinHeight(250);
        texto.setAlignment(Pos.TOP_LEFT);
        texto.setOnMouseClicked(evento -> completarTexto());
        HBox expediente = new HBox(30, limon, texto);
        expediente.setAlignment(Pos.CENTER_LEFT);
        expediente.getStyleClass().add("tarjeta-historia");
        expediente.setMaxWidth(900);

        Label versus = new Label("VS");
        versus.getStyleClass().add("vs");
        HBox elenco = new HBox(26,
                presentarGato("pola", "La heroína"),
                presentarGato("minerva", "La estratega"),
                versus,
                presentarGato("nero", "El secuaz"),
                presentarGato("limon", "Líder del mal"));
        elenco.setAlignment(Pos.CENTER);

        Button aceptar = new Button("Aceptar la misión");
        aceptar.getStyleClass().add("boton-grande");
        aceptar.setOnAction(evento -> alAceptar.run());
        animaciones.add(latido(aceptar));

        VBox contenido = new VBox(22, sello, titulo, expediente, elenco, aceptar);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(30));

        getChildren().addAll(fondo, velo, contenido);
    }

    /** Retrato de un gato con su nombre y su papel en la historia. */
    private VBox presentarGato(String gato, String rol) {
        CatCharacter retrato = new CatCharacter(gato, 74);
        Label nombre = new Label(Theme.nombreDe(gato));
        nombre.getStyleClass().add("nombre-gato");
        Label papel = new Label(rol);
        papel.getStyleClass().add("rol-gato");
        VBox caja = new VBox(4, retrato, nombre, papel);
        caja.setAlignment(Pos.CENTER);
        return caja;
    }

    /** Hace que un nodo "respire" (crezca y se encoja) para llamar la atencion. */
    private Animation latido(Region nodo) {
        ScaleTransition latido = new ScaleTransition(Duration.millis(900), nodo);
        latido.setToX(1.06);
        latido.setToY(1.06);
        latido.setAutoReverse(true);
        latido.setCycleCount(Animation.INDEFINITE);
        return latido;
    }

    /** Arranca las animaciones; se llama cuando la ventana ya esta visible. */
    public void iniciar() {
        ScaleTransition zoom = new ScaleTransition(Duration.seconds(18), fondo);
        zoom.setFromX(1.0);
        zoom.setFromY(1.0);
        zoom.setToX(1.12);
        zoom.setToY(1.12);
        zoom.setAutoReverse(true);
        zoom.setCycleCount(Animation.INDEFINITE);
        animaciones.add(zoom);
        for (Animation animacion : animaciones) {
            animacion.play();
        }
        escritura = Animaciones.escribir(texto, HISTORIA, 14);
    }

    private void completarTexto() {
        if (escritura != null) {
            escritura.stop();
        }
        texto.setText(HISTORIA);
    }

    /** Detiene las animaciones infinitas para que no sigan corriendo cuando la portada ya no se ve. */
    public void detener() {
        for (Animation animacion : animaciones) {
            animacion.stop();
        }
        if (escritura != null) {
            escritura.stop();
        }
    }
}
