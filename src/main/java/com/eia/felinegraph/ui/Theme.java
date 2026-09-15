package com.eia.felinegraph.ui;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Paleta del tema felino y carga de imagenes.
 *
 * El estilo de botones, paneles y textos vive en theme.css. Aqui solo se repiten los colores que
 * se usan desde codigo (retratos, jaula de Nina, dibujos en Canvas), porque un Canvas no puede
 * leer CSS. Los villanos usan morado porque los dos llevan capa morada en sus retratos.
 */
public final class Theme {

    public static final Color FONDO = Color.web("#1F1B24");
    public static final Color PANEL = Color.web("#2A2530");
    public static final Color TEXTO = Color.web("#F2EDE4");
    public static final Color TEXTO_SUAVE = Color.web("#A79FB3");
    public static final Color POLA = Color.web("#E08A3C");
    public static final Color MINERVA = Color.web("#B8C4CE");
    public static final Color VILLANO = Color.web("#9B5DE5");
    public static final Color PELIGRO = Color.web("#E4574C");
    public static final Color EXITO = Color.web("#5DD39E");
    public static final Color DORADO = Color.web("#F2C14E");

    public static final String CSS = "/com/eia/felinegraph/theme.css";
    private static final String CARPETA_IMAGENES = "/com/eia/felinegraph/img/";

    private static final Map<String, Image> imagenes = new HashMap<>();

    private Theme() {
    }

    /** Carga una imagen de resources una sola vez (queda guardada); si el archivo no existe devuelve null. */
    public static Image imagen(String nombre) {
        if (imagenes.containsKey(nombre)) {
            return imagenes.get(nombre);
        }
        Image imagen = null;
        URL archivo = Theme.class.getResource(CARPETA_IMAGENES + nombre + ".png");
        if (archivo != null) {
            imagen = new Image(archivo.toExternalForm());
        }
        imagenes.put(nombre, imagen);
        return imagen;
    }

    /** Color que identifica a cada gato: se usa en el aro de su retrato y en su brillo. */
    public static Color colorDe(String gato) {
        if (gato.equals("pola")) {
            return POLA;
        }
        if (gato.equals("minerva")) {
            return MINERVA;
        }
        if (gato.equals("nero") || gato.equals("limon")) {
            return VILLANO;
        }
        return DORADO;
    }

    /** Nombre del gato tal como se muestra en pantalla. */
    public static String nombreDe(String gato) {
        if (gato.equals("pola")) {
            return "Pola";
        }
        if (gato.equals("minerva")) {
            return "Minerva";
        }
        if (gato.equals("nero")) {
            return "Nero";
        }
        if (gato.equals("limon")) {
            return "Limón";
        }
        return "Nina";
    }
}
