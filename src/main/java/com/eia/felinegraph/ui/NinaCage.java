package com.eia.felinegraph.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * Jaula de Nina: es el indicador de progreso del juego.
 *
 * Tiene un barrote por mision, del color de esa mision. Cada vez que se resuelve una mision su
 * barrote cae; cuando caen los cuatro, Nina queda libre. Todo se dibuja en un espacio de
 * 100 x 100 unidades y luego se escala al tamano pedido, asi la misma jaula sirve pequena
 * (encabezado) y grande (pantalla final).
 *
 * No tenemos foto de Nina, asi que se dibuja su silueta con ojos que parpadean. Si algun dia se
 * agrega img/nina.png a resources, se usa la foto (en gris mientras esta encerrada).
 */
public final class NinaCage extends Pane {

    private static final String SILUETA =
            "M22 40 L16 10 L40 28 Q50 25 60 28 L84 10 L78 40 Q88 56 80 70 Q94 80 96 100 "
            + "L4 100 Q6 80 20 70 Q12 56 22 40 Z";

    private final Rectangle[] barras;
    private final Ellipse ojoIzquierdo = new Ellipse(38, 52, 5, 3.5);
    private final Ellipse ojoDerecho = new Ellipse(62, 52, 5, 3.5);
    private final Circle resplandor = new Circle(50, 58, 44);
    private final ColorAdjust tonoFoto = new ColorAdjust(0, -1, -0.35, 0);
    private final boolean tieneFoto;

    public NinaCage(double tamano, Color[] colores) {
        setPrefSize(tamano, tamano);
        setMinSize(tamano, tamano);
        setMaxSize(tamano, tamano);

        Group dibujo = new Group();
        Rectangle fondo = new Rectangle(0, 0, 100, 100);
        fondo.setArcWidth(18);
        fondo.setArcHeight(18);
        fondo.setFill(Color.web("#15121A"));
        fondo.setStroke(Color.web("#3B3246"));
        fondo.setStrokeWidth(2);

        resplandor.setFill(Theme.DORADO.deriveColor(0, 1, 1, 0.35));
        resplandor.setOpacity(0);
        dibujo.getChildren().addAll(fondo, resplandor);

        Image foto = Theme.imagen("nina");
        tieneFoto = foto != null;
        if (tieneFoto) {
            dibujo.getChildren().add(crearFoto(foto));
        } else {
            dibujo.getChildren().addAll(crearSilueta(), ojoIzquierdo, ojoDerecho);
            iniciarParpadeo();
        }

        barras = new Rectangle[colores.length];
        for (int i = 0; i < colores.length; i++) {
            barras[i] = crearBarra(i, colores.length, colores[i]);
            dibujo.getChildren().add(barras[i]);
        }
        dibujo.getChildren().addAll(crearViga(0), crearViga(93));

        dibujo.getTransforms().add(new Scale(tamano / 100.0, tamano / 100.0, 0, 0));
        getChildren().add(dibujo);

        Rectangle recorte = new Rectangle(tamano, tamano);
        recorte.setArcWidth(tamano * 0.18);
        recorte.setArcHeight(tamano * 0.18);
        setClip(recorte);
    }

    private SVGPath crearSilueta() {
        SVGPath silueta = new SVGPath();
        silueta.setContent(SILUETA);
        silueta.setFill(Color.web("#0B090E"));
        silueta.setStroke(Color.web("#4A4056"));
        silueta.setStrokeWidth(1.2);
        ojoIzquierdo.setFill(Theme.MINERVA);
        ojoDerecho.setFill(Theme.MINERVA);
        DropShadow brilloOjos = new DropShadow(6, Theme.MINERVA);
        ojoIzquierdo.setEffect(brilloOjos);
        ojoDerecho.setEffect(brilloOjos);
        return silueta;
    }

    private ImageView crearFoto(Image foto) {
        ImageView vista = new ImageView(foto);
        vista.setX(10);
        vista.setY(12);
        vista.setFitWidth(80);
        vista.setFitHeight(80);
        vista.setClip(new Circle(50, 52, 40));
        vista.setEffect(tonoFoto);
        return vista;
    }

    /** Parpadeo de los ojos de la silueta cada 3.5 segundos, para que Nina se sienta viva. */
    private void iniciarParpadeo() {
        Timeline parpadeo = new Timeline(
                new KeyFrame(Duration.millis(3200),
                        new KeyValue(ojoIzquierdo.scaleYProperty(), 1),
                        new KeyValue(ojoDerecho.scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(3330),
                        new KeyValue(ojoIzquierdo.scaleYProperty(), 0.1),
                        new KeyValue(ojoDerecho.scaleYProperty(), 0.1)),
                new KeyFrame(Duration.millis(3480),
                        new KeyValue(ojoIzquierdo.scaleYProperty(), 1),
                        new KeyValue(ojoDerecho.scaleYProperty(), 1)));
        parpadeo.setCycleCount(Animation.INDEFINITE);
        parpadeo.play();
    }

    /** Barrote vertical i, repartido a lo ancho de la jaula y con el brillo del color de su mision. */
    private Rectangle crearBarra(int indice, int total, Color color) {
        double espacio = 100.0 / (total + 1);
        Rectangle barra = new Rectangle(espacio * (indice + 1) - 3, 4, 6, 92);
        barra.setArcWidth(6);
        barra.setArcHeight(6);
        barra.setFill(color);
        barra.setEffect(new DropShadow(6, color));
        return barra;
    }

    private Rectangle crearViga(double y) {
        Rectangle viga = new Rectangle(0, y, 100, 7);
        viga.setFill(Color.web("#6B6377"));
        return viga;
    }

    /** Animacion de un barrote cayendo: baja, gira un poco y se desvanece. */
    private Animation caidaDeBarra(int indice) {
        Rectangle barra = barras[indice];
        TranslateTransition bajar = new TranslateTransition(Duration.millis(700), barra);
        bajar.setByY(110);
        bajar.setInterpolator(Interpolator.EASE_IN);
        RotateTransition girar = new RotateTransition(Duration.millis(700), barra);
        girar.setByAngle(indice % 2 == 0 ? 35 : -35);
        FadeTransition desaparecer = new FadeTransition(Duration.millis(700), barra);
        desaparecer.setToValue(0);
        ParallelTransition caida = new ParallelTransition(bajar, girar, desaparecer);
        caida.setOnFinished(evento -> barra.setVisible(false));
        return caida;
    }

    /** Hace caer el barrote de la mision indicada (si aun no ha caido). */
    public void quitarBarra(int indice) {
        if (barras[indice].isVisible() && barras[indice].getOpacity() > 0.99) {
            caidaDeBarra(indice).play();
        }
    }

    /** Hace caer, uno por uno, los barrotes que quedan; al final libera a Nina y ejecuta alTerminar. */
    public void quitarTodasEnSecuencia(Runnable alTerminar) {
        SequentialTransition secuencia = new SequentialTransition();
        for (int i = 0; i < barras.length; i++) {
            if (barras[i].isVisible()) {
                secuencia.getChildren().add(new PauseTransition(Duration.millis(350)));
                secuencia.getChildren().add(caidaDeBarra(i));
            }
        }
        secuencia.setOnFinished(evento -> {
            liberar();
            alTerminar.run();
        });
        secuencia.play();
    }

    /** Vuelve a poner todos los barrotes (cuando se reinicia la aventura). */
    public void ponerTodasLasBarras() {
        for (Rectangle barra : barras) {
            barra.setVisible(true);
            barra.setOpacity(1);
            barra.setTranslateY(0);
            barra.setRotate(0);
        }
        resplandor.setOpacity(0);
        ojoIzquierdo.setRadiusY(3.5);
        ojoDerecho.setRadiusY(3.5);
        ojoIzquierdo.setFill(Theme.MINERVA);
        ojoDerecho.setFill(Theme.MINERVA);
        tonoFoto.setSaturation(-1);
        tonoFoto.setBrightness(-0.35);
    }

    /** Nina libre: aparece un resplandor dorado y sus ojos se vuelven felices (o su foto recupera el color). */
    public void liberar() {
        FadeTransition luz = new FadeTransition(Duration.millis(600), resplandor);
        luz.setToValue(1);
        ScaleTransition latido = new ScaleTransition(Duration.millis(900), resplandor);
        latido.setFromX(0.9);
        latido.setFromY(0.9);
        latido.setToX(1.12);
        latido.setToY(1.12);
        latido.setAutoReverse(true);
        latido.setCycleCount(Animation.INDEFINITE);
        luz.play();
        latido.play();

        ojoIzquierdo.setFill(Theme.DORADO);
        ojoDerecho.setFill(Theme.DORADO);
        ojoIzquierdo.setRadiusY(1.6);
        ojoDerecho.setRadiusY(1.6);

        Timeline color = new Timeline(new KeyFrame(Duration.millis(900),
                new KeyValue(tonoFoto.saturationProperty(), 0),
                new KeyValue(tonoFoto.brightnessProperty(), 0)));
        color.play();
    }

    public boolean usaFoto() {
        return tieneFoto;
    }
}
