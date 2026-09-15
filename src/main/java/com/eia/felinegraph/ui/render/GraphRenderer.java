package com.eia.felinegraph.ui.render;

import com.eia.felinegraph.core.draw.GraphDrawing;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Dibuja un GraphDrawing en un Canvas. Lo comparten las Misiones 2 (Sebastian), 3 (Tomas) y 4 (Samuel).
 * Solo conoce el modelo neutro GraphDrawing: no sabe nada de Dijkstra, Floyd-Warshall ni Kruskal.
 *
 * Que dibuja:
 *  - Nodos en circulo (layout circular): el nodo i va en el angulo 360 * i / N.
 *  - Aristas normales en gris y, encima, las resaltadas con brillo. El color depende de kind:
 *    PATH dorado (ruta), CYCLE morado (ciclo de churun infinito), MST celeste (cables de Kruskal).
 *  - Flechas si el grafo es dirigido (Mision 3).
 *  - Aristas repetidas entre el mismo par se dibujan curvas, cada una hacia un lado, para que no
 *    queden una encima de otra. Los lazos (A = B) se dibujan como un circulito junto al nodo.
 *  - Los pesos, solo si hay pocas aristas (con cientos de aristas no se alcanzarian a leer).
 *
 * El parametro progreso (de 0 a 1) controla que tanto de lo resaltado se ve: la GUI lo anima de
 * 0 a 1 para que la ruta "se trace sola" en pantalla.
 *
 * Complejidad: O(V + E) por cada dibujo (un recorrido de nodos y otro de aristas).
 */
public final class GraphRenderer {

    private static final int MAX_ARISTAS_CON_PESO = 60;
    private static final double SEPARACION_PARALELAS = 26;
    private static final double ALTO_LEYENDA = 34;

    private static final Color COLOR_ARISTA = Color.web("#6B6377");
    private static final Color COLOR_NODO = Color.web("#2A2530");
    private static final Color COLOR_BORDE_NODO = Color.web("#A79FB3");
    private static final Color COLOR_TEXTO = Color.web("#F2EDE4");
    private static final Color COLOR_TEXTO_SUAVE = Color.web("#A79FB3");
    private static final Color COLOR_INICIO = Color.web("#5DD39E");
    private static final Color COLOR_DESTINO = Color.web("#F2C14E");
    private static final Color COLOR_RUTA = Color.web("#F2C14E");
    private static final Color COLOR_CICLO = Color.web("#C77DFF");
    private static final Color COLOR_MST = Color.web("#6FD6FF");
    private static final Color COLOR_FONDO_PESO = Color.web("#1A161F");

    /** Geometria de una arista ya calculada: donde empieza, por donde se curva y donde termina. */
    private static final class Trazo {
        double inicioX;
        double inicioY;
        double controlX;
        double controlY;
        double finX;
        double finY;
        boolean esLazo;
        boolean esRecta;
        double radioLazo;
        double direccionX;
        double direccionY;
    }

    private GraphRenderer() {
    }

    /** Crea un Canvas del tamano pedido y dibuja el grafo completo, sin animacion. */
    public static Canvas render(GraphDrawing grafo, double ancho, double alto) {
        Canvas lienzo = new Canvas(ancho, alto);
        dibujar(lienzo.getGraphicsContext2D(), grafo, ancho, alto, 1.0);
        return lienzo;
    }

    /** Dibuja todo el grafo. Primero las aristas normales y encima las resaltadas, para que la ruta nunca quede tapada. */
    public static void dibujar(GraphicsContext gc, GraphDrawing grafo, double ancho, double alto, double progreso) {
        gc.clearRect(0, 0, ancho, alto);
        double radio = radioNodo(grafo.nodes);
        double[] x = new double[grafo.nodes];
        double[] y = new double[grafo.nodes];
        calcularPosiciones(grafo.nodes, ancho, alto - ALTO_LEYENDA, radio, x, y);

        int[] orden = ordenEntreParalelas(grafo);
        Trazo[] trazos = new Trazo[grafo.edgeFrom.length];
        for (int i = 0; i < trazos.length; i++) {
            trazos[i] = calcularTrazo(grafo, i, x, y, radio, orden[i], ancho, alto - ALTO_LEYENDA);
        }

        Color resaltado = colorResaltado(grafo.kind);
        for (int i = 0; i < trazos.length; i++) {
            if (!grafo.highlighted[i]) {
                dibujarArista(gc, trazos[i], grafo.directed, COLOR_ARISTA, 1.5, 1.0);
            }
        }
        gc.setEffect(new DropShadow(12, resaltado));
        for (int i = 0; i < trazos.length; i++) {
            if (grafo.highlighted[i]) {
                dibujarArista(gc, trazos[i], grafo.directed, resaltado, 4.0, progreso);
            }
        }
        gc.setEffect(null);

        boolean conPesos = trazos.length <= MAX_ARISTAS_CON_PESO;
        for (int i = 0; i < trazos.length; i++) {
            if (grafo.highlighted[i] && progreso > 0.95) {
                dibujarPeso(gc, trazos[i], grafo.edgeWeight[i], resaltado);
            } else if (!grafo.highlighted[i] && conPesos) {
                dibujarPeso(gc, trazos[i], grafo.edgeWeight[i], COLOR_TEXTO_SUAVE);
            }
        }

        for (int nodo = 0; nodo < grafo.nodes; nodo++) {
            dibujarNodo(gc, grafo, nodo, x[nodo], y[nodo], radio);
        }
        dibujarLeyenda(gc, grafo, alto, resaltado);
    }

    /** Radio de los nodos: mas pequenos cuantos mas nodos haya, para que quepan en el circulo. */
    private static double radioNodo(int nodos) {
        if (nodos <= 8) {
            return 22;
        }
        if (nodos <= 20) {
            return 17;
        }
        if (nodos <= 40) {
            return 13;
        }
        if (nodos <= 60) {
            return 11;
        }
        return 8;
    }

    /** Layout circular: reparte los nodos en un circulo, empezando arriba y avanzando en sentido horario. */
    private static void calcularPosiciones(int nodos, double ancho, double alto, double radio, double[] x, double[] y) {
        double centroX = ancho / 2;
        double centroY = alto / 2 + 4;
        double radioCirculo = Math.min(ancho, alto) / 2 - radio - 34;
        if (radioCirculo < 10) {
            radioCirculo = 10;
        }
        if (nodos == 1) {
            x[0] = centroX;
            y[0] = centroY;
            return;
        }
        for (int i = 0; i < nodos; i++) {
            double angulo = -Math.PI / 2 + 2 * Math.PI * i / nodos;
            x[i] = centroX + radioCirculo * Math.cos(angulo);
            y[i] = centroY + radioCirculo * Math.sin(angulo);
        }
    }

    /**
     * Para cada arista, cuantas aristas anteriores unen el mismo par de nodos (sin importar el
     * sentido). Se usa un HashMap con el par como llave para que sea O(E) y no O(E^2): la
     * Mision 2 puede tener hasta 100000 aristas entre solo 60 nodos.
     */
    private static int[] ordenEntreParalelas(GraphDrawing grafo) {
        int[] orden = new int[grafo.edgeFrom.length];
        Map<Long, Integer> vistas = new HashMap<>();
        for (int i = 0; i < orden.length; i++) {
            long menor = Math.min(grafo.edgeFrom[i], grafo.edgeTo[i]);
            long mayor = Math.max(grafo.edgeFrom[i], grafo.edgeTo[i]);
            long llave = menor * 1000000L + mayor;
            int anteriores = vistas.getOrDefault(llave, 0);
            orden[i] = anteriores;
            vistas.put(llave, anteriores + 1);
        }
        return orden;
    }

    /** Desplazamiento de la curva: la primera arista va recta, las siguientes se alternan a cada lado. */
    private static double curvatura(int orden) {
        if (orden == 0) {
            return 0;
        }
        int nivel = (orden + 1) / 2;
        if (orden % 2 == 1) {
            return nivel * SEPARACION_PARALELAS;
        }
        return -nivel * SEPARACION_PARALELAS;
    }

    /**
     * Calcula por donde pasa la arista i. Empieza y termina en el borde de los nodos (no en el
     * centro) para que la flecha se vea. Si es curva, el punto de control se mueve en direccion
     * perpendicular a la linea entre los dos nodos.
     */
    private static Trazo calcularTrazo(GraphDrawing grafo, int i, double[] x, double[] y, double radio, int orden,
                                       double ancho, double alto) {
        Trazo trazo = new Trazo();
        int desde = grafo.edgeFrom[i];
        int hasta = grafo.edgeTo[i];

        if (desde == hasta) {
            // Lazo: circulito hacia afuera del circulo principal
            double dx = x[desde] - ancho / 2;
            double dy = y[desde] - alto / 2;
            double largo = Math.sqrt(dx * dx + dy * dy);
            if (largo < 0.001) {
                dx = 0;
                dy = -1;
                largo = 1;
            }
            trazo.esLazo = true;
            trazo.direccionX = dx / largo;
            trazo.direccionY = dy / largo;
            trazo.radioLazo = radio * 0.8 + orden * 4;
            trazo.controlX = x[desde] + trazo.direccionX * (radio + trazo.radioLazo * 0.7);
            trazo.controlY = y[desde] + trazo.direccionY * (radio + trazo.radioLazo * 0.7);
            return trazo;
        }

        double dx = x[hasta] - x[desde];
        double dy = y[hasta] - y[desde];
        double largo = Math.sqrt(dx * dx + dy * dy);
        if (largo < 0.001) {
            largo = 0.001;
        }
        double perpendicularX = -dy / largo;
        double perpendicularY = dx / largo;
        if (desde > hasta) {
            // Se usa siempre el mismo sentido (del nodo menor al mayor) para que las curvas se alternen bien
            perpendicularX = -perpendicularX;
            perpendicularY = -perpendicularY;
        }
        double curva = curvatura(orden);
        trazo.esRecta = curva == 0;
        trazo.controlX = (x[desde] + x[hasta]) / 2 + perpendicularX * curva * 2;
        trazo.controlY = (y[desde] + y[hasta]) / 2 + perpendicularY * curva * 2;

        double[] salida = unitario(trazo.controlX - x[desde], trazo.controlY - y[desde]);
        double[] llegada = unitario(x[hasta] - trazo.controlX, y[hasta] - trazo.controlY);
        trazo.inicioX = x[desde] + salida[0] * radio;
        trazo.inicioY = y[desde] + salida[1] * radio;
        trazo.finX = x[hasta] - llegada[0] * radio;
        trazo.finY = y[hasta] - llegada[1] * radio;
        trazo.direccionX = llegada[0];
        trazo.direccionY = llegada[1];
        return trazo;
    }

    /** Vector de largo 1 en la direccion (dx, dy). */
    private static double[] unitario(double dx, double dy) {
        double largo = Math.sqrt(dx * dx + dy * dy);
        if (largo < 0.001) {
            return new double[] {0, 0};
        }
        return new double[] {dx / largo, dy / largo};
    }

    /**
     * Dibuja una arista. Si es recta, crece desde el inicio segun el progreso (efecto de trazo);
     * si es curva o lazo, aparece con transparencia.
     */
    private static void dibujarArista(GraphicsContext gc, Trazo trazo, boolean dirigido, Color color,
                                      double grosor, double progreso) {
        gc.setStroke(color);
        gc.setFill(color);
        gc.setLineWidth(grosor);

        if (trazo.esLazo) {
            gc.setGlobalAlpha(progreso);
            gc.strokeOval(trazo.controlX - trazo.radioLazo, trazo.controlY - trazo.radioLazo,
                    trazo.radioLazo * 2, trazo.radioLazo * 2);
            gc.setGlobalAlpha(1);
            return;
        }

        double puntaX;
        double puntaY;
        if (trazo.esRecta) {
            puntaX = trazo.inicioX + (trazo.finX - trazo.inicioX) * progreso;
            puntaY = trazo.inicioY + (trazo.finY - trazo.inicioY) * progreso;
            gc.strokeLine(trazo.inicioX, trazo.inicioY, puntaX, puntaY);
        } else {
            gc.setGlobalAlpha(progreso);
            gc.beginPath();
            gc.moveTo(trazo.inicioX, trazo.inicioY);
            gc.quadraticCurveTo(trazo.controlX, trazo.controlY, trazo.finX, trazo.finY);
            gc.stroke();
            puntaX = trazo.finX;
            puntaY = trazo.finY;
        }
        if (dirigido && progreso > 0.05) {
            dibujarFlecha(gc, puntaX, puntaY, trazo.direccionX, trazo.direccionY, grosor);
        }
        gc.setGlobalAlpha(1);
    }

    /** Triangulo en la punta de la arista, apuntando en la direccion de llegada. */
    private static void dibujarFlecha(GraphicsContext gc, double puntaX, double puntaY,
                                      double direccionX, double direccionY, double grosor) {
        double tamano = 8 + grosor * 1.5;
        double baseX = puntaX - direccionX * tamano;
        double baseY = puntaY - direccionY * tamano;
        double ladoX = -direccionY * tamano * 0.5;
        double ladoY = direccionX * tamano * 0.5;
        gc.fillPolygon(
                new double[] {puntaX, baseX + ladoX, baseX - ladoX},
                new double[] {puntaY, baseY + ladoY, baseY - ladoY},
                3);
    }

    /** Peso de la arista dentro de una etiquetita, en la mitad de la arista (o junto al lazo). */
    private static void dibujarPeso(GraphicsContext gc, Trazo trazo, long peso, Color color) {
        double px;
        double py;
        if (trazo.esLazo) {
            px = trazo.controlX + trazo.direccionX * (trazo.radioLazo + 10);
            py = trazo.controlY + trazo.direccionY * (trazo.radioLazo + 10);
        } else {
            // Punto de la curva en t = 0.5: 1/4 del inicio + 1/2 del control + 1/4 del fin
            px = 0.25 * trazo.inicioX + 0.5 * trazo.controlX + 0.25 * trazo.finX;
            py = 0.25 * trazo.inicioY + 0.5 * trazo.controlY + 0.25 * trazo.finY;
        }
        String texto = String.valueOf(peso);
        double anchoTexto = texto.length() * 7 + 10;
        gc.setFill(COLOR_FONDO_PESO);
        gc.fillRoundRect(px - anchoTexto / 2, py - 9, anchoTexto, 18, 8, 8);
        gc.setStroke(color);
        gc.setLineWidth(1);
        gc.strokeRoundRect(px - anchoTexto / 2, py - 9, anchoTexto, 18, 8, 8);
        gc.setFill(color);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(texto, px, py);
    }

    /** Nodo: circulo con su numero. El inicio (S) va con borde verde y el destino (D) con borde dorado. */
    private static void dibujarNodo(GraphicsContext gc, GraphDrawing grafo, int nodo, double x, double y, double radio) {
        Color borde = COLOR_BORDE_NODO;
        double grosor = 2;
        String marca = null;
        if (nodo == grafo.start) {
            borde = COLOR_INICIO;
            grosor = 3.5;
            marca = "S";
        } else if (nodo == grafo.goal) {
            borde = COLOR_DESTINO;
            grosor = 3.5;
            marca = "D";
        }
        if (marca != null) {
            gc.setEffect(new DropShadow(14, borde));
        }
        gc.setFill(COLOR_NODO);
        gc.fillOval(x - radio, y - radio, radio * 2, radio * 2);
        gc.setStroke(borde);
        gc.setLineWidth(grosor);
        gc.strokeOval(x - radio, y - radio, radio * 2, radio * 2);
        gc.setEffect(null);

        gc.setFill(COLOR_TEXTO);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, Math.max(8, Math.min(14, radio * 0.9))));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(nodo), x, y);

        if (marca != null) {
            gc.setFill(borde);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.fillText(marca, x, y - radio - 10);
        }
    }

    /** Leyenda de colores en la parte de abajo del dibujo. */
    private static void dibujarLeyenda(GraphicsContext gc, GraphDrawing grafo, double alto, Color resaltado) {
        double y = alto - ALTO_LEYENDA / 2;
        double x = 14;
        gc.setFont(Font.font("Segoe UI", 12));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);

        if (grafo.kind != GraphDrawing.Highlight.NONE) {
            gc.setStroke(resaltado);
            gc.setLineWidth(4);
            gc.strokeLine(x, y, x + 26, y);
            gc.setFill(COLOR_TEXTO);
            gc.fillText(textoResaltado(grafo.kind), x + 34, y);
            x += 250;
        }
        if (grafo.start >= 0) {
            gc.setFill(COLOR_INICIO);
            gc.fillText("S = inicio", x, y);
            gc.setFill(COLOR_DESTINO);
            gc.fillText("D = destino", x + 80, y);
            x += 180;
        }
        gc.setFill(COLOR_TEXTO_SUAVE);
        gc.fillText(grafo.directed ? "Grafo dirigido" : "Grafo no dirigido", x, y);
    }

    private static Color colorResaltado(GraphDrawing.Highlight tipo) {
        if (tipo == GraphDrawing.Highlight.CYCLE) {
            return COLOR_CICLO;
        }
        if (tipo == GraphDrawing.Highlight.MST) {
            return COLOR_MST;
        }
        return COLOR_RUTA;
    }

    private static String textoResaltado(GraphDrawing.Highlight tipo) {
        if (tipo == GraphDrawing.Highlight.CYCLE) {
            return "Ciclo que da churun infinito";
        }
        if (tipo == GraphDrawing.Highlight.MST) {
            return "Cables elegidos por Kruskal";
        }
        return "Ruta encontrada";
    }
}
