package com.eia.felinegraph.ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Retrato animado de un gato (Pola, Minerva, Nero o Limon), recortado en circulo sobre su cara.
 *
 * Tiene dos capas a proposito:
 *  - "cuerpo" lleva la animacion de reposo, que nunca se detiene y depende de la personalidad:
 *    Pola rebota aplastandose (es gordita), Minerva flota, Nero se balancea con arrogancia y
 *    Limon palpita con un brillo amenazante.
 *  - el nodo completo (this) recibe las reacciones puntuales: saltar, sacudirse o cambiar de gato.
 * Si las dos animaciones movieran el mismo nodo se pelearian por la misma propiedad.
 */
public final class CatCharacter extends StackPane {

    private final double tamano;
    private final StackPane cuerpo = new StackPane();
    private final ImageView foto = new ImageView();
    private final Circle aro;
    private final DropShadow brillo = new DropShadow();
    private Animation reposo;
    private ScaleTransition cambio;
    private String gato = "";

    public CatCharacter(String gato, double tamano) {
        this.tamano = tamano;
        aro = new Circle(tamano / 2 + 3);
        aro.setFill(Theme.PANEL);
        aro.setStrokeWidth(Math.max(2.5, tamano / 28));
        foto.setFitWidth(tamano);
        foto.setFitHeight(tamano);
        foto.setSmooth(true);
        foto.setClip(new Circle(tamano / 2, tamano / 2, tamano / 2));
        brillo.setSpread(0.2);
        cuerpo.getChildren().addAll(aro, foto);
        cuerpo.setEffect(brillo);
        getChildren().add(cuerpo);
        aplicar(gato);
    }

    /** Pone la foto, el color del aro y la animacion de reposo del gato indicado (sin transicion). */
    private void aplicar(String nuevoGato) {
        gato = nuevoGato;
        Image imagen = Theme.imagen(nuevoGato);
        foto.setImage(imagen);
        if (imagen != null) {
            foto.setViewport(recorteDeCara(imagen, nuevoGato));
        }
        aro.setStroke(Theme.colorDe(nuevoGato));
        brillo.setColor(Theme.colorDe(nuevoGato).deriveColor(0, 1, 1, 0.75));
        brillo.setRadius(tamano / 5);
        iniciarReposo();
    }

    /** Cambia de gato con transicion: se encoge, cambia la foto y reaparece con rebote. */
    public void cambiarA(String nuevoGato) {
        if (nuevoGato.equals(gato)) {
            return;
        }
        if (cambio != null) {
            cambio.stop();
        }
        cambio = new ScaleTransition(Duration.millis(140), this);
        cambio.setToX(0.2);
        cambio.setToY(0.2);
        cambio.setOnFinished(evento -> {
            aplicar(nuevoGato);
            Animaciones.aparecer(this);
        });
        cambio.play();
    }

    public void celebrar() {
        Animaciones.saltar(this);
    }

    public void amenazar() {
        Animaciones.sacudir(this);
    }

    /** Detiene la animacion de reposo (para retratos pequenos que no deben distraer). */
    public void quieto() {
        if (reposo != null) {
            reposo.stop();
        }
        reiniciarCuerpo();
    }

    public String gatoActual() {
        return gato;
    }

    /**
     * Cuadrado de la imagen que contiene la cara. Las fotos son verticales y cada gato tiene la
     * cara a una altura distinta, por eso se guarda esa altura (como fraccion de la imagen).
     */
    private Rectangle2D recorteDeCara(Image imagen, String nombre) {
        double lado = imagen.getWidth() * 0.72;
        double x = (imagen.getWidth() - lado) / 2;
        double y = imagen.getHeight() * alturaDeCara(nombre) - lado / 2;
        if (y < 0) {
            y = 0;
        }
        if (y + lado > imagen.getHeight()) {
            y = imagen.getHeight() - lado;
        }
        return new Rectangle2D(x, y, lado, lado);
    }

    private double alturaDeCara(String nombre) {
        if (nombre.equals("pola")) {
            return 0.40;
        }
        if (nombre.equals("minerva")) {
            return 0.33;
        }
        if (nombre.equals("nero")) {
            return 0.27;
        }
        if (nombre.equals("limon")) {
            return 0.46;
        }
        return 0.40;
    }

    /** Reemplaza la animacion de reposo por la que corresponde a la personalidad del gato actual. */
    private void iniciarReposo() {
        if (reposo != null) {
            reposo.stop();
        }
        reiniciarCuerpo();
        reposo = crearReposo();
        reposo.play();
    }

    private void reiniciarCuerpo() {
        cuerpo.setTranslateY(0);
        cuerpo.setRotate(0);
        cuerpo.setScaleX(1);
        cuerpo.setScaleY(1);
    }

    /** Animacion continua (va y vuelve para siempre) segun la personalidad de cada gato. */
    private Animation crearReposo() {
        Timeline animacion = new Timeline();
        if (gato.equals("pola")) {
            // Pola rebota aplastandose y estirandose, como una gata "pasadita de peso pero hermosa"
            animacion.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(cuerpo.scaleXProperty(), 1.0),
                            new KeyValue(cuerpo.scaleYProperty(), 1.0),
                            new KeyValue(cuerpo.translateYProperty(), 0)),
                    new KeyFrame(Duration.millis(650),
                            new KeyValue(cuerpo.scaleXProperty(), 1.06, Interpolator.EASE_BOTH),
                            new KeyValue(cuerpo.scaleYProperty(), 0.94, Interpolator.EASE_BOTH),
                            new KeyValue(cuerpo.translateYProperty(), 3, Interpolator.EASE_BOTH)));
        } else if (gato.equals("minerva")) {
            // Minerva flota tranquila, como la estratega del equipo
            animacion.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(cuerpo.translateYProperty(), 0)),
                    new KeyFrame(Duration.millis(1500), new KeyValue(cuerpo.translateYProperty(), -8, Interpolator.EASE_BOTH)));
        } else if (gato.equals("nero")) {
            // Nero se balancea de lado a lado, burlon
            animacion.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(cuerpo.rotateProperty(), -5)),
                    new KeyFrame(Duration.millis(1700), new KeyValue(cuerpo.rotateProperty(), 5, Interpolator.EASE_BOTH)));
        } else {
            // Limon palpita y su brillo morado crece y se encoge
            animacion.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(brillo.radiusProperty(), tamano / 8),
                            new KeyValue(cuerpo.scaleXProperty(), 1.0),
                            new KeyValue(cuerpo.scaleYProperty(), 1.0)),
                    new KeyFrame(Duration.millis(1100),
                            new KeyValue(brillo.radiusProperty(), tamano / 3, Interpolator.EASE_BOTH),
                            new KeyValue(cuerpo.scaleXProperty(), 1.04, Interpolator.EASE_BOTH),
                            new KeyValue(cuerpo.scaleYProperty(), 1.04, Interpolator.EASE_BOTH)));
        }
        animacion.setAutoReverse(true);
        animacion.setCycleCount(Animation.INDEFINITE);
        return animacion;
    }
}
