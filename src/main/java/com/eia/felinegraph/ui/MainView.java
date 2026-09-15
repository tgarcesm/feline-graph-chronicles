package com.eia.felinegraph.ui;

import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.Mission;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.draw.GraphDrawing;
import com.eia.felinegraph.core.draw.GridDrawing;
import com.eia.felinegraph.core.draw.MatrixDrawing;
import com.eia.felinegraph.ui.render.GraphRenderer;
import com.eia.felinegraph.ui.render.GridRenderer;
import com.eia.felinegraph.ui.render.MatrixRenderer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Tablero principal del juego.
 *
 * Arriba: titulo, progreso y la jaula de Nina (un barrote por mision). Izquierda: los cuatro
 * expedientes (misiones), que se pueden resolver en cualquier orden. Centro: la historia de la
 * mision con el gato que la narra, la entrada, la salida y el "mapa del caso" (dibujo). Abajo: la
 * barra de estado, donde se muestran los errores de formato de forma legible.
 *
 * La vista no sabe que algoritmo usa cada mision: solo llama mission.solve(texto) y muestra el
 * MissionResult. Para escoger el renderer revisa el tipo del dibujo con instanceof:
 *  - GridDrawing  -> GridRenderer   (Samuel, Mision 1)
 *  - GraphDrawing -> GraphRenderer  (Sebastian, Misiones 2, 3 y 4)
 *  - MatrixDrawing -> MatrixRenderer (Tomas, Mision 3), en su propia pestana
 *
 * Manejo de errores: solve() corre en un hilo aparte (asi la ventana nunca se congela) y
 * cualquier error se muestra como texto en la barra de estado y en la salida, nunca como stack trace.
 */
public final class MainView extends StackPane {

    private static final int MAX_ARISTAS_ANIMADAS = 1500;

    private final List<MissionStory> misiones = MissionStory.todas();
    private final boolean[] resueltas;
    private final String[] entradas;
    private final MissionResult[] resultados;
    private final List<HBox> tarjetas = new ArrayList<>();
    private final List<Label> pastillas = new ArrayList<>();

    private final BorderPane tablero = new BorderPane();
    private final NinaCage jaula;
    private final ProgressBar barraProgreso = new ProgressBar(0);
    private final Label textoProgreso = new Label();

    private final CatCharacter narrador = new CatCharacter("pola", 96);
    private final Label tituloMision = new Label();
    private final Label algoritmoMision = new Label();
    private final Label historiaMision = new Label();
    private final Label burbuja = new Label();

    private final TextArea entrada = new TextArea();
    private final TextArea salida = new TextArea();
    private final Label avisos = new Label();
    private final Button botonResolver = new Button("Resolver");
    private final ProgressIndicator cargando = new ProgressIndicator();

    private final Label etiquetaCaso = new Label();
    private final Button botonAnterior = new Button("<");
    private final Button botonSiguiente = new Button(">");
    private final Label respuesta = new Label();
    private final Label leyenda = new Label();
    private final StackPane areaDibujo = new StackPane();
    private final ScrollPane scrollDibujo = new ScrollPane(areaDibujo);
    private final TabPane pestanas = new TabPane();
    private final Tab pestanaDibujo = new Tab("Dibujo");
    private final Tab pestanaMatriz = new Tab("Matriz");

    private final Label estado = new Label();
    private final Circle puntoEstado = new Circle(5, Theme.TEXTO_SUAVE);

    private int indiceActual = -1;
    private int casoMostrado;
    private Timeline escrituraBurbuja;
    private Timeline trazado;
    private EndingScreen pantallaFinal;

    public MainView() {
        int total = misiones.size();
        resueltas = new boolean[total];
        entradas = new String[total];
        resultados = new MissionResult[total];
        jaula = new NinaCage(64, MissionStory.coloresDe(misiones));

        tablero.setTop(construirEncabezado());
        tablero.setLeft(construirBarraLateral());
        tablero.setCenter(construirCentro());
        tablero.setBottom(construirBarraEstado());
        getChildren().add(tablero);

        actualizarProgreso();
        seleccionarMision(0);
        mostrarEstado("Escoge un expediente a la izquierda. Cada misión resuelta rompe un barrote de la jaula de Nina.",
                "estado-info");
    }

    // ------------------------------------------------------------------ construccion de la pantalla

    private HBox construirEncabezado() {
        Label titulo = new Label("THE FELINE GRAPH CHRONICLES");
        titulo.getStyleClass().add("titulo-app");
        Label subtitulo = new Label("Pola y Minerva contra Limón y Nero  ·  Operación rescate de Nina");
        subtitulo.getStyleClass().add("subtitulo-app");
        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        textoProgreso.getStyleClass().add("progreso-texto");
        barraProgreso.setPrefWidth(230);
        VBox progreso = new VBox(6, textoProgreso, barraProgreso);
        progreso.setAlignment(Pos.CENTER_RIGHT);

        Label nombreNina = new Label("Nina");
        nombreNina.getStyleClass().add("rol-gato");
        VBox cajaNina = new VBox(2, jaula, nombreNina);
        cajaNina.setAlignment(Pos.CENTER);

        HBox encabezado = new HBox(18, textos, espacio, progreso, cajaNina);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getStyleClass().add("encabezado");
        return encabezado;
    }

    private VBox construirBarraLateral() {
        Label titulo = new Label("EXPEDIENTES");
        titulo.getStyleClass().add("seccion-titulo");
        VBox lateral = new VBox(12, titulo);
        lateral.getStyleClass().add("barra-lateral");
        for (int i = 0; i < misiones.size(); i++) {
            HBox tarjeta = crearTarjeta(i);
            tarjetas.add(tarjeta);
            lateral.getChildren().add(tarjeta);
            Animaciones.aparecer(tarjeta, 150 + i * 120);
        }
        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        Label consejo = new Label("Resuélvelas en el orden que quieras.\nAtajo: Ctrl + Enter para resolver.");
        consejo.getStyleClass().add("leyenda");
        consejo.setWrapText(true);
        lateral.getChildren().addAll(espacio, consejo);
        return lateral;
    }

    /** Tarjeta de una mision: retrato de quien la narra, nombre, algoritmo y estado (pendiente o resuelta). */
    private HBox crearTarjeta(int indice) {
        MissionStory mision = misiones.get(indice);
        CatCharacter retrato = new CatCharacter(mision.narrador, 46);
        retrato.quieto();

        Label numero = new Label("MISIÓN " + (indice + 1));
        numero.getStyleClass().add("tarjeta-numero");
        numero.setStyle("-fx-text-fill: " + mision.colorAcento + ";");
        Label nombre = new Label(mision.nombre);
        nombre.getStyleClass().add("tarjeta-nombre");
        Label algoritmo = new Label(mision.algoritmo);
        algoritmo.getStyleClass().add("tarjeta-algoritmo");
        Label pastilla = new Label("PENDIENTE");
        pastilla.getStyleClass().addAll("pastilla", "pastilla-pendiente");
        pastillas.add(pastilla);

        VBox textos = new VBox(2, numero, nombre, algoritmo, pastilla);
        HBox tarjeta = new HBox(12, retrato, textos);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.setStyle("-fx-border-color: " + mision.colorAcento + ";");
        tarjeta.setOnMouseClicked(evento -> seleccionarMision(indice));
        tarjeta.setOnMouseEntered(evento -> Animaciones.escalar(tarjeta, 1.03));
        tarjeta.setOnMouseExited(evento -> Animaciones.escalar(tarjeta, 1.0));
        return tarjeta;
    }

    private VBox construirCentro() {
        VBox trabajo = construirColumnaTrabajo();
        VBox mapa = construirColumnaDibujo();
        HBox.setHgrow(mapa, Priority.ALWAYS);
        HBox columnas = new HBox(14, trabajo, mapa);
        VBox.setVgrow(columnas, Priority.ALWAYS);

        VBox centro = new VBox(14, construirBanner(), columnas);
        centro.setPadding(new Insets(16, 18, 14, 4));
        return centro;
    }

    /** Banner de la mision: el gato narrador (animado) con el titulo, la historia y su globo de dialogo. */
    private HBox construirBanner() {
        tituloMision.getStyleClass().add("mision-titulo");
        algoritmoMision.getStyleClass().add("mision-algoritmo");
        historiaMision.getStyleClass().add("mision-historia");
        historiaMision.setWrapText(true);
        burbuja.getStyleClass().add("burbuja");
        burbuja.setWrapText(true);
        burbuja.setMaxWidth(760);

        HBox fila = new HBox(10, tituloMision, algoritmoMision);
        fila.setAlignment(Pos.CENTER_LEFT);
        VBox textos = new VBox(6, fila, historiaMision, burbuja);
        HBox.setHgrow(textos, Priority.ALWAYS);

        HBox banner = new HBox(18, narrador, textos);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().add("banner");
        return banner;
    }

    private VBox construirColumnaTrabajo() {
        Label tituloEntrada = new Label("PISTA DE NERO  ·  ENTRADA");
        tituloEntrada.getStyleClass().add("panel-titulo");
        entrada.setPromptText("Pega aquí la entrada de la misión o usa \"Cargar ejemplo\"...");
        entrada.addEventFilter(KeyEvent.KEY_PRESSED, evento -> atajoTeclado(evento));
        VBox.setVgrow(entrada, Priority.ALWAYS);

        Button cargar = new Button("Cargar ejemplo");
        cargar.getStyleClass().add("boton");
        cargar.setOnAction(evento -> cargarEjemplo());
        Button limpiar = new Button("Limpiar");
        limpiar.getStyleClass().add("boton");
        limpiar.setOnAction(evento -> limpiarTodo());
        botonResolver.getStyleClass().addAll("boton", "boton-principal");
        botonResolver.setOnAction(evento -> resolver());
        cargar.setMinWidth(Region.USE_PREF_SIZE);
        limpiar.setMinWidth(Region.USE_PREF_SIZE);
        botonResolver.setMinWidth(Region.USE_PREF_SIZE);
        cargando.setPrefSize(24, 24);
        cargando.setVisible(false);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox acciones = new HBox(8, cargar, limpiar, espacio, cargando, botonResolver);
        acciones.setAlignment(Pos.CENTER_LEFT);

        Label tituloSalida = new Label("INFORME DE LA MISIÓN  ·  SALIDA");
        tituloSalida.getStyleClass().add("panel-titulo");
        salida.setEditable(false);
        salida.setWrapText(true);
        salida.getStyleClass().add("salida");
        VBox.setVgrow(salida, Priority.ALWAYS);
        avisos.getStyleClass().add("avisos");
        avisos.setWrapText(true);
        ocultar(avisos);

        VBox columna = new VBox(8, tituloEntrada, entrada, acciones, tituloSalida, salida, avisos);
        columna.getStyleClass().add("panel");
        columna.setPrefWidth(420);
        columna.setMinWidth(390);
        return columna;
    }

    private VBox construirColumnaDibujo() {
        Label titulo = new Label("MAPA DEL CASO");
        titulo.getStyleClass().add("panel-titulo");
        botonAnterior.getStyleClass().addAll("boton", "boton-caso");
        botonAnterior.setOnAction(evento -> mostrarCaso(casoMostrado - 1));
        botonSiguiente.getStyleClass().addAll("boton", "boton-caso");
        botonSiguiente.setOnAction(evento -> mostrarCaso(casoMostrado + 1));
        etiquetaCaso.getStyleClass().add("progreso-texto");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox cabecera = new HBox(8, titulo, espacio, botonAnterior, etiquetaCaso, botonSiguiente);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        respuesta.getStyleClass().add("respuesta");
        areaDibujo.setPadding(new Insets(8));
        scrollDibujo.setFitToWidth(true);
        scrollDibujo.setFitToHeight(true);
        pestanaDibujo.setContent(scrollDibujo);
        pestanaDibujo.setClosable(false);
        pestanaMatriz.setClosable(false);
        pestanas.getTabs().add(pestanaDibujo);
        VBox.setVgrow(pestanas, Priority.ALWAYS);

        leyenda.getStyleClass().add("leyenda");
        leyenda.setWrapText(true);

        VBox columna = new VBox(8, cabecera, respuesta, pestanas, leyenda);
        columna.getStyleClass().add("panel");
        columna.setPrefWidth(300);
        return columna;
    }

    private HBox construirBarraEstado() {
        HBox barra = new HBox(10, puntoEstado, estado);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-estado");
        return barra;
    }

    // ------------------------------------------------------------------ seleccion de mision

    /** Cambia de mision: guarda la entrada de la anterior y muestra la historia, el narrador y el resultado previo si lo hay. */
    private void seleccionarMision(int indice) {
        if (indice == indiceActual) {
            return;
        }
        if (indiceActual >= 0) {
            entradas[indiceActual] = entrada.getText();
        }
        indiceActual = indice;
        MissionStory mision = misiones.get(indice);

        tablero.setStyle("-accent: " + mision.colorAcento + ";");
        for (HBox tarjeta : tarjetas) {
            tarjeta.getStyleClass().remove("tarjeta-seleccionada");
        }
        tarjetas.get(indice).getStyleClass().add("tarjeta-seleccionada");

        tituloMision.setText(mision.nombre);
        algoritmoMision.setText(mision.algoritmo);
        historiaMision.setText(mision.historia);
        Animaciones.aparecer(tituloMision);
        narrador.cambiarA(mision.narrador);
        hablar(mision.narrador, mision.fraseInicio, false);

        if (entradas[indice] == null) {
            entrada.setText("");
        } else {
            entrada.setText(entradas[indice]);
        }
        if (resultados[indice] == null) {
            limpiarResultado();
        } else {
            mostrarResultado(resultados[indice]);
        }
    }

    /** Pone un texto en el globo de dialogo, escrito letra por letra y con el nombre de quien habla. */
    private void hablar(String gato, String frase, boolean esVillano) {
        if (escrituraBurbuja != null) {
            escrituraBurbuja.stop();
        }
        burbuja.getStyleClass().remove("burbuja-villano");
        if (esVillano) {
            burbuja.getStyleClass().add("burbuja-villano");
        }
        escrituraBurbuja = Animaciones.escribir(burbuja, Theme.nombreDe(gato) + ": " + frase, 18);
    }

    // ------------------------------------------------------------------ acciones de los botones

    private void cargarEjemplo() {
        entrada.setText(misiones.get(indiceActual).mission.sampleInput());
        mostrarEstado("Pista de ejemplo cargada. Presiona \"Resolver\" (o Ctrl + Enter).", "estado-info");
    }

    private void limpiarTodo() {
        entrada.clear();
        resultados[indiceActual] = null;
        limpiarResultado();
        mostrarEstado("Entrada y resultados limpiados.", "estado-info");
    }

    private void atajoTeclado(KeyEvent evento) {
        if (evento.isControlDown() && evento.getCode() == KeyCode.ENTER) {
            resolver();
            evento.consume();
        }
    }

    /**
     * Resuelve la mision actual en un hilo aparte (Task) para no congelar la ventana con entradas
     * grandes. El hilo se crea con pila ampliada por seguridad (el enunciado lo acepta para el DFS).
     * Cuando termina, JavaFX llama a mostrarResultadoNuevo o a mostrarFallo en el hilo de la ventana.
     */
    private void resolver() {
        String texto = entrada.getText();
        if (texto.trim().isEmpty()) {
            mostrarFallo(indiceActual, new InputFormatException(
                    "La entrada está vacía: pega la pista de la misión o usa \"Cargar ejemplo\"."));
            return;
        }
        int indice = indiceActual;
        Mission mision = misiones.get(indice).mission;
        Task<MissionResult> tarea = new Task<MissionResult>() {
            @Override
            protected MissionResult call() throws Exception {
                return mision.solve(texto);
            }
        };
        tarea.setOnSucceeded(evento -> mostrarResultadoNuevo(indice, tarea.getValue()));
        tarea.setOnFailed(evento -> mostrarFallo(indice, tarea.getException()));

        botonResolver.setDisable(true);
        cargando.setVisible(true);
        mostrarEstado("Pola y Minerva están analizando la pista...", "estado-info");
        Thread hilo = new Thread(null, tarea, "resolver-mision", 256L * 1024 * 1024);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void terminarCarga() {
        botonResolver.setDisable(false);
        cargando.setVisible(false);
    }

    /** Llega un resultado nuevo: se guarda, se marca la mision como resuelta y se muestra si sigue seleccionada. */
    private void mostrarResultadoNuevo(int indice, MissionResult resultado) {
        terminarCarga();
        resultados[indice] = resultado;
        marcarResuelta(indice);
        String casos = resultado.cases().size() == 1 ? "1 caso" : resultado.cases().size() + " casos";
        if (resultado.warnings().isEmpty()) {
            mostrarEstado("Misión " + (indice + 1) + " resuelta: " + casos + ".", "estado-ok");
        } else {
            mostrarEstado("Misión " + (indice + 1) + " resuelta con avisos: " + resultado.warnings().get(0), "estado-aviso");
        }
        if (indice == indiceActual) {
            mostrarResultado(resultado);
        }
    }

    /**
     * Cualquier error termina aqui y se muestra como texto legible. Un InputFormatException es un
     * error de formato de la entrada; cualquier otra cosa se reporta como error inesperado, pero
     * igual sin stack trace. Nero aparece burlandose porque "saboteo la pista".
     */
    private void mostrarFallo(int indice, Throwable error) {
        terminarCarga();
        String detalle = error.getMessage() == null ? "sin detalle" : error.getMessage();
        String mensaje;
        if (error instanceof InputFormatException) {
            mensaje = detalle;
        } else {
            mensaje = "Error inesperado (" + error.getClass().getSimpleName() + "): " + detalle;
        }
        mostrarEstado("No se pudo resolver la misión " + (indice + 1) + ": " + mensaje, "estado-error");
        if (indice != indiceActual) {
            return;
        }
        resultados[indice] = null;
        limpiarResultado();
        salida.setText("No se pudo resolver la misión.\n\n" + mensaje);
        narrador.cambiarA("nero");
        narrador.amenazar();
        hablar("nero", "¡Je, je! Saboteé la pista. Revisa el formato de la entrada.", true);
        Animaciones.sacudir(entrada);
    }

    // ------------------------------------------------------------------ resultado y casos

    private void mostrarResultado(MissionResult resultado) {
        salida.setText(resultado.consoleOutput());
        salida.positionCaret(0);
        mostrarAvisos(resultado.warnings());
        if (resultado.cases().isEmpty()) {
            limpiarResultado();
            return;
        }
        mostrarCaso(0);
    }

    private void mostrarAvisos(List<String> lista) {
        if (lista.isEmpty()) {
            ocultar(avisos);
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (String aviso : lista) {
            if (texto.length() > 0) {
                texto.append("\n");
            }
            texto.append(aviso);
        }
        avisos.setText(texto.toString());
        avisos.setVisible(true);
        avisos.setManaged(true);
    }

    /** Muestra el caso k: su linea de respuesta junto al dibujo, y la reaccion del gato que corresponde. */
    private void mostrarCaso(int k) {
        MissionResult resultado = resultados[indiceActual];
        if (resultado == null || k < 0 || k >= resultado.cases().size()) {
            return;
        }
        casoMostrado = k;
        CaseResult caso = resultado.cases().get(k);
        etiquetaCaso.setText("Caso " + (k + 1) + " de " + resultado.cases().size());
        botonAnterior.setDisable(k == 0);
        botonSiguiente.setDisable(k == resultado.cases().size() - 1);

        respuesta.setText(caso.line());
        boolean ganoVillano = reaccionar(caso.line());
        respuesta.getStyleClass().remove("respuesta-villano");
        if (ganoVillano) {
            respuesta.getStyleClass().add("respuesta-villano");
        }
        Animaciones.aparecer(respuesta);
        dibujar(caso);
    }

    /**
     * Escoge que gato reacciona segun la linea de salida del caso. Si aparece el mensaje del villano
     * (por ejemplo "Nina is very sad"), el villano se burla; si aparece un mensaje especial ("Infinite
     * churun!"), Pola celebra de forma especial; si no, la narradora celebra. Devuelve true si gano el villano.
     */
    private boolean reaccionar(String linea) {
        MissionStory mision = misiones.get(indiceActual);
        if (linea.contains(mision.mensajeVillano)) {
            narrador.cambiarA(mision.villano);
            narrador.amenazar();
            hablar(mision.villano, mision.fraseVillano, true);
            return true;
        }
        if (mision.mensajeEspecial != null && linea.contains(mision.mensajeEspecial)) {
            narrador.cambiarA(mision.gatoEspecial);
            narrador.celebrar();
            hablar(mision.gatoEspecial, mision.fraseEspecial, false);
            return false;
        }
        narrador.cambiarA(mision.narrador);
        narrador.celebrar();
        hablar(mision.narrador, mision.fraseExito, false);
        return false;
    }

    /** Escoge el renderer segun el tipo de dibujo; si la instancia es muy grande muestra el aviso de dibujo omitido. */
    private void dibujar(CaseResult caso) {
        if (trazado != null) {
            trazado.stop();
        }
        areaDibujo.getChildren().clear();
        double ancho = anchoDisponible();
        double alto = altoDisponible();
        Object dibujo = caso.drawing();

        if (dibujo instanceof GridDrawing) {
            areaDibujo.getChildren().add(GridRenderer.render((GridDrawing) dibujo, ancho, alto));
            leyenda.setText("Campo minado: se muestran el camino de BFS (el más corto) y el de DFS "
                    + "(orden arriba, abajo, izquierda, derecha). Los dos exploran la misma grilla; solo cambia el orden.");
        } else if (dibujo instanceof GraphDrawing) {
            dibujarGrafoAnimado((GraphDrawing) dibujo, ancho, alto);
            leyenda.setText(textoLeyenda((GraphDrawing) dibujo));
        } else {
            String razon = caso.drawingSkippedReason();
            if (razon == null) {
                razon = "Este caso no tiene dibujo.";
            }
            Label aviso = new Label(razon + "\n\nLa respuesta sí se calculó: está arriba y en el informe.");
            aviso.getStyleClass().add("aviso-omitido");
            aviso.setWrapText(true);
            aviso.setMaxWidth(480);
            areaDibujo.getChildren().add(aviso);
            leyenda.setText("El enunciado solo exige dibujar instancias pequeñas; por encima del límite se muestra este aviso.");
        }
        Animaciones.aparecer(areaDibujo.getChildren().get(0));
        mostrarMatriz(caso.matrix());
    }

    /**
     * Dibuja el grafo y anima la ruta: una propiedad "progreso" va de 0 a 1 en 1.3 segundos y cada
     * vez que cambia se redibuja el Canvas. Con demasiadas aristas se dibuja de una vez, sin animar.
     */
    private void dibujarGrafoAnimado(GraphDrawing grafo, double ancho, double alto) {
        Canvas lienzo = new Canvas(ancho, alto);
        areaDibujo.getChildren().add(lienzo);
        if (grafo.edgeFrom.length > MAX_ARISTAS_ANIMADAS) {
            GraphRenderer.dibujar(lienzo.getGraphicsContext2D(), grafo, ancho, alto, 1.0);
            return;
        }
        DoubleProperty progreso = new SimpleDoubleProperty(0);
        progreso.addListener((propiedad, anterior, nuevo) ->
                GraphRenderer.dibujar(lienzo.getGraphicsContext2D(), grafo, ancho, alto, nuevo.doubleValue()));
        GraphRenderer.dibujar(lienzo.getGraphicsContext2D(), grafo, ancho, alto, 0);
        trazado = new Timeline(new KeyFrame(Duration.millis(1300), new KeyValue(progreso, 1.0, Interpolator.EASE_BOTH)));
        trazado.setDelay(Duration.millis(250));
        trazado.play();
    }

    private String textoLeyenda(GraphDrawing grafo) {
        if (grafo.kind == GraphDrawing.Highlight.CYCLE) {
            return "En morado, el ciclo de ganancia positiva que alcanza al destino: por eso el churun no tiene límite.";
        }
        if (grafo.kind == GraphDrawing.Highlight.MST) {
            return "En celeste, los cables del árbol de expansión mínima. Los grises se descartan porque cerrarían un ciclo.";
        }
        if (grafo.kind == GraphDrawing.Highlight.PATH) {
            return "En dorado, la ruta que da la respuesta, de S (inicio) a D (destino).";
        }
        return "No hay ruta que resaltar: el destino no se puede alcanzar desde el inicio.";
    }

    /** La matriz de Floyd-Warshall (solo Mision 3) va en su propia pestana, sobre fondo claro para leerla bien. */
    private void mostrarMatriz(MatrixDrawing matriz) {
        if (matriz == null) {
            pestanas.getTabs().remove(pestanaMatriz);
            pestanas.getSelectionModel().select(pestanaDibujo);
            return;
        }
        StackPane papel = new StackPane(MatrixRenderer.render(matriz));
        papel.getStyleClass().add("papel");
        pestanaMatriz.setText("Matriz Floyd-Warshall (" + matriz.n + " x " + matriz.n + ")");
        pestanaMatriz.setContent(papel);
        if (!pestanas.getTabs().contains(pestanaMatriz)) {
            pestanas.getTabs().add(pestanaMatriz);
        }
    }

    private double anchoDisponible() {
        double ancho = scrollDibujo.getViewportBounds().getWidth() - 18;
        if (ancho < 250) {
            ancho = 620;
        }
        return ancho;
    }

    private double altoDisponible() {
        double alto = scrollDibujo.getViewportBounds().getHeight() - 18;
        if (alto < 250) {
            alto = 480;
        }
        return alto;
    }

    /** Deja la zona de resultados vacia, con un texto que invita a resolver. */
    private void limpiarResultado() {
        if (trazado != null) {
            trazado.stop();
        }
        salida.clear();
        ocultar(avisos);
        respuesta.setText("Esperando la pista...");
        respuesta.getStyleClass().remove("respuesta-villano");
        etiquetaCaso.setText("Sin casos");
        botonAnterior.setDisable(true);
        botonSiguiente.setDisable(true);
        areaDibujo.getChildren().clear();
        Label vacio = new Label("Aquí aparecerá el mapa del caso cuando resuelvas la misión.");
        vacio.getStyleClass().add("leyenda");
        areaDibujo.getChildren().add(vacio);
        leyenda.setText("");
        mostrarMatriz(null);
    }

    // ------------------------------------------------------------------ progreso y final

    /** La primera vez que se resuelve una mision: pastilla verde, cae su barrote y, si ya van las 4, llega el final. */
    private void marcarResuelta(int indice) {
        if (resueltas[indice]) {
            return;
        }
        resueltas[indice] = true;
        Label pastilla = pastillas.get(indice);
        pastilla.setText("RESUELTA");
        pastilla.getStyleClass().remove("pastilla-pendiente");
        pastilla.getStyleClass().add("pastilla-resuelta");
        Animaciones.aparecer(pastilla);
        jaula.quitarBarra(indice);
        actualizarProgreso();

        if (contarResueltas() == misiones.size()) {
            PauseTransition espera = new PauseTransition(Duration.seconds(2.5));
            espera.setOnFinished(evento -> mostrarFinal());
            espera.play();
        }
    }

    private int contarResueltas() {
        int total = 0;
        for (boolean resuelta : resueltas) {
            if (resuelta) {
                total++;
            }
        }
        return total;
    }

    /** Actualiza el texto y anima la barra de progreso hasta la nueva fraccion. */
    private void actualizarProgreso() {
        int total = contarResueltas();
        textoProgreso.setText("Barrotes rotos: " + total + " de " + misiones.size());
        Timeline avance = new Timeline(new KeyFrame(Duration.millis(700),
                new KeyValue(barraProgreso.progressProperty(), (double) total / misiones.size(), Interpolator.EASE_BOTH)));
        avance.play();
    }

    private void mostrarFinal() {
        if (pantallaFinal != null) {
            return;
        }
        pantallaFinal = new EndingScreen(() -> cerrarFinal(), () -> reiniciarAventura());
        getChildren().add(pantallaFinal);
        pantallaFinal.reproducir();
        mostrarEstado("¡Nina está libre! Las cuatro misiones fueron resueltas.", "estado-ok");
    }

    private void cerrarFinal() {
        if (pantallaFinal == null) {
            return;
        }
        EndingScreen saliente = pantallaFinal;
        pantallaFinal = null;
        saliente.detener();
        Animaciones.desvanecer(saliente, 500, () -> getChildren().remove(saliente));
    }

    /** Vuelve todo al inicio: misiones pendientes, jaula con sus cuatro barrotes y entradas vacias. */
    private void reiniciarAventura() {
        cerrarFinal();
        for (int i = 0; i < misiones.size(); i++) {
            resueltas[i] = false;
            entradas[i] = null;
            resultados[i] = null;
            Label pastilla = pastillas.get(i);
            pastilla.setText("PENDIENTE");
            pastilla.getStyleClass().remove("pastilla-resuelta");
            pastilla.getStyleClass().add("pastilla-pendiente");
        }
        jaula.ponerTodasLasBarras();
        actualizarProgreso();
        indiceActual = -1;
        seleccionarMision(0);
        mostrarEstado("Nueva aventura: Limón volvió a encerrar a Nina. ¡A resolver de nuevo!", "estado-info");
    }

    // ------------------------------------------------------------------ utilidades

    /** Cambia el mensaje de la barra de estado y su color segun el tipo (info, ok, aviso o error). */
    private void mostrarEstado(String texto, String tipo) {
        estado.getStyleClass().removeAll("estado-info", "estado-ok", "estado-aviso", "estado-error");
        estado.getStyleClass().add(tipo);
        estado.setText(texto);
        Color color = Theme.TEXTO_SUAVE;
        if (tipo.equals("estado-ok")) {
            color = Theme.EXITO;
        } else if (tipo.equals("estado-aviso")) {
            color = Theme.DORADO;
        } else if (tipo.equals("estado-error")) {
            color = Theme.PELIGRO;
        }
        puntoEstado.setFill(color);
        Animaciones.aparecer(puntoEstado);
    }

    private void ocultar(Region nodo) {
        nodo.setVisible(false);
        nodo.setManaged(false);
    }
}
