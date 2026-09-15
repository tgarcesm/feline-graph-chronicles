package com.eia.felinegraph.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pantalla final: aparece cuando las cuatro misiones estan resueltas.
 *
 * Secuencia: la pantalla aparece sobre el tablero, los villanos se ponen grises y huyen, caen
 * uno por uno los barrotes de la jaula, Nina queda libre, Pola y Minerva celebran saltando y
 * llueve confeti. Al final se puede volver al tablero o reiniciar la aventura desde cero.
 */
public final class EndingScreen extends StackPane {

    private static final String MENSAJE =
            "Nina volvió a la casa de Sebas, las cuentas de Claude fueron recuperadas y Limón se quedó "
            + "sin su churun infinito. Pola y Minerva salvaron el día. ¡Gracias por jugar!";

    private final List<Animation> animaciones = new ArrayList<>();
    private final Pane confeti = new Pane();
    private final NinaCage jaula;
    private final CatCharacter pola = new CatCharacter("pola", 130);
    private final CatCharacter minerva = new CatCharacter("minerva", 130);
    private final CatCharacter nero = new CatCharacter("nero", 64);
    private final CatCharacter limon = new CatCharacter("limon", 64);
    private final HBox villanos;
    private final Label titulo = new Label();
    private final Label mensaje = new Label();
    private final HBox botones;

    public EndingScreen(Runnable alVolver, Runnable alReiniciar) {
        getStyleClass().add("pantalla-final");
        Region fondo = new Region();
        fondo.getStyleClass().add("fondo-batalla");
        Region velo = new Region();
        velo.getStyleClass().add("velo");

        jaula = new NinaCage(200, MissionStory.coloresDe(MissionStory.todas()));
        Label nombreNina = new Label("Nina");
        nombreNina.getStyleClass().add("nombre-gato");
        VBox cajaNina = new VBox(8, jaula, nombreNina);
        cajaNina.setAlignment(Pos.CENTER);

        HBox escena = new HBox(50, pola, cajaNina, minerva);
        escena.setAlignment(Pos.CENTER);

        titulo.getStyleClass().add("final-titulo");
        mensaje.getStyleClass().add("historia-texto");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(720);
        mensaje.setMinHeight(70);
        mensaje.setAlignment(Pos.CENTER);

        Button volver = new Button("Volver al tablero");
        volver.getStyleClass().add("boton-grande-secundario");
        volver.setOnAction(evento -> alVolver.run());
        Button reiniciar = new Button("Jugar de nuevo");
        reiniciar.getStyleClass().add("boton-grande");
        reiniciar.setOnAction(evento -> alReiniciar.run());
        botones = new HBox(16, volver, reiniciar);
        botones.setAlignment(Pos.CENTER);
        botones.setOpacity(0);

        VBox contenido = new VBox(26, titulo, escena, mensaje, botones);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(30));

        Label derrotados = new Label("Villanos derrotados");
        derrotados.getStyleClass().add("rol-gato");
        villanos = new HBox(10, nero, limon, derrotados);
        villanos.setAlignment(Pos.CENTER_RIGHT);
        villanos.setMaxHeight(Region.USE_PREF_SIZE);
        villanos.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(villanos, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(villanos, new Insets(0, 30, 24, 0));

        confeti.setMouseTransparent(true);
        pola.setOpacity(0);
        minerva.setOpacity(0);

        getChildren().addAll(fondo, velo, contenido, villanos, confeti);
    }

    /** Arranca toda la secuencia final. */
    public void reproducir() {
        setOpacity(0);
        FadeTransition entrada = new FadeTransition(Duration.millis(700), this);
        entrada.setToValue(1);
        entrada.setOnFinished(evento -> empezarRescate());
        entrada.play();
    }

    /** Primera parte: titulo, heroinas, villanos huyendo y barrotes cayendo. */
    private void empezarRescate() {
        Animaciones.escribir(titulo, "¡Misión cumplida!", 55);
        Animaciones.aparecer(pola, 200);
        Animaciones.aparecer(minerva, 450);
        villanosHuyen();
        jaula.quitarTodasEnSecuencia(() -> ninaLibre());
    }

    /** Segunda parte: Nina ya esta libre, las heroinas celebran y llueve confeti. */
    private void ninaLibre() {
        Animaciones.escribir(mensaje, MENSAJE, 22);
        Timeline celebracion = new Timeline(new KeyFrame(Duration.millis(1400), evento -> {
            pola.celebrar();
            minerva.celebrar();
        }));
        celebracion.setCycleCount(Animation.INDEFINITE);
        celebracion.play();
        animaciones.add(celebracion);
        pola.celebrar();
        minerva.celebrar();
        lanzarConfeti();

        FadeTransition mostrarBotones = new FadeTransition(Duration.millis(600), botones);
        mostrarBotones.setDelay(Duration.millis(1200));
        mostrarBotones.setToValue(1);
        mostrarBotones.play();
    }

    /** Nero y Limon pierden el color (quedan en gris) y se escapan por la derecha. */
    private void villanosHuyen() {
        ColorAdjust gris = new ColorAdjust();
        gris.setSaturation(-1);
        gris.setBrightness(-0.25);
        villanos.setEffect(gris);
        nero.amenazar();
        limon.amenazar();

        TranslateTransition huir = new TranslateTransition(Duration.millis(1400), villanos);
        huir.setDelay(Duration.millis(2600));
        huir.setByX(500);
        huir.setInterpolator(Interpolator.EASE_IN);
        FadeTransition desaparecer = new FadeTransition(Duration.millis(1400), villanos);
        desaparecer.setDelay(Duration.millis(2600));
        desaparecer.setToValue(0);
        new ParallelTransition(huir, desaparecer).play();
    }

    /** Lluvia de confeti con los colores de las misiones: cada pieza cae y gira sin parar. */
    private void lanzarConfeti() {
        Random azar = new Random();
        double ancho = getWidth() > 0 ? getWidth() : 1400;
        double alto = getHeight() > 0 ? getHeight() : 900;
        Color[] colores = {Theme.POLA, Theme.DORADO, Theme.EXITO, Theme.MINERVA,
                Color.web("#4FC1B9"), Color.web("#E4574C"), Color.web("#8FA8F0")};

        for (int i = 0; i < 80; i++) {
            Rectangle pieza = new Rectangle(6 + azar.nextInt(6), 10 + azar.nextInt(8));
            pieza.setFill(colores[azar.nextInt(colores.length)]);
            pieza.setX(azar.nextDouble() * ancho);
            pieza.setY(-30);
            confeti.getChildren().add(pieza);

            TranslateTransition caida = new TranslateTransition(Duration.seconds(3 + azar.nextDouble() * 3), pieza);
            caida.setByY(alto + 60);
            caida.setDelay(Duration.seconds(azar.nextDouble() * 2.5));
            caida.setInterpolator(Interpolator.LINEAR);
            caida.setCycleCount(Animation.INDEFINITE);
            RotateTransition giro = new RotateTransition(Duration.seconds(1 + azar.nextDouble() * 2), pieza);
            giro.setByAngle(azar.nextBoolean() ? 360 : -360);
            giro.setCycleCount(Animation.INDEFINITE);
            caida.play();
            giro.play();
            animaciones.add(caida);
            animaciones.add(giro);
        }
        ScaleTransition latidoJaula = new ScaleTransition(Duration.millis(700), jaula);
        latidoJaula.setToX(1.06);
        latidoJaula.setToY(1.06);
        latidoJaula.setAutoReverse(true);
        latidoJaula.setCycleCount(Animation.INDEFINITE);
        latidoJaula.play();
        animaciones.add(latidoJaula);
    }

    /** Detiene las animaciones infinitas (confeti, celebracion) antes de quitar la pantalla. */
    public void detener() {
        for (Animation animacion : animaciones) {
            animacion.stop();
        }
    }
}
