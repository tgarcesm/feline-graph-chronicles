package com.eia.felinegraph.missions;

import com.eia.felinegraph.algorithms.Dijkstra;
import com.eia.felinegraph.core.CaseResult;
import com.eia.felinegraph.core.InputFormatException;
import com.eia.felinegraph.core.Mission;
import com.eia.felinegraph.core.MissionResult;
import com.eia.felinegraph.core.Tokenizer;
import com.eia.felinegraph.core.draw.GraphDrawing;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase hace todo lo que NO es el algoritmo: lee la entrada con el Tokenizer, valida los
 * rangos del enunciado, arma la lista de adyacencia, llama a Dijkstra, escribe la linea exacta
 * de salida y prepara el dibujo. El algoritmo vive aparte en algorithms/Dijkstra.java.
 */
public final class Mission2Accounts implements Mission {

    //hasta 60 nodos se dibuja
    private static final int MAX_NODOS = 10000;
    private static final int MAX_CONEXIONES = 100000;
    private static final long MAX_PESO = 1000000L;
    private static final int MAX_NODOS_DIBUJO = 60;

    private static final String MENSAJE_SIN_RUTA = "Nina is very sad";

    private static final String SAMPLE =
            "3\n" +
            "2 1 0 1\n" +
            "0 1 100\n" +
            "3 3 2 0\n" +
            "0 1 100\n" +
            "0 2 200\n" +
            "1 2 50\n" +
            "2 0 0 1\n";

    @Override
    public String title() {
        return "Mision 2 - Recuperando las cuentas de Claude";
    }

    @Override
    public String sampleInput() {
        return SAMPLE;
    }

    /**
     * Lee T y resuelve los T casos en orden. Si un caso tiene un error de formato, el mensaje se
     * vuelve a lanzar diciendo en que caso fue, para que el usuario lo encuentre facil.
     * Si sobran datos al final no se considera error, pero se avisa en warnings.
     */
    @Override
    public MissionResult solve(String rawInput) throws InputFormatException {
        Tokenizer tokenizer = new Tokenizer(rawInput);
        if (!tokenizer.hasNext()) {
            throw new InputFormatException("La entrada esta vacia: pegue los datos de la mision o use el boton de cargar ejemplo");
        }

        int totalCasos = tokenizer.nextInt();
        if (totalCasos < 1) {
            throw new InputFormatException("El numero de casos T debe ser al menos 1, se encontro " + totalCasos);
        }

        List<CaseResult> casos = new ArrayList<>();
        for (int k = 1; k <= totalCasos; k++) {
            try {
                casos.add(resolverCaso(tokenizer, k));
            } catch (InputFormatException error) {
                throw new InputFormatException("Error en el caso #" + k + ": " + error.getMessage());
            }
        }

        List<String> avisos = new ArrayList<>();
        if (tokenizer.hasNext()) {
            avisos.add("Aviso: la entrada tiene datos de sobra despues del caso #" + totalCasos + ", se ignoraron");
        }
        return new MissionResult(casos, avisos);
    }

    private CaseResult resolverCaso(Tokenizer tokenizer, int numeroCaso) throws InputFormatException {
        int totalNodos = leerEntero(tokenizer, "N", 1, MAX_NODOS);
        int totalConexiones = leerEntero(tokenizer, "C", 0, MAX_CONEXIONES);
        int origen = leerEntero(tokenizer, "S", 0, totalNodos - 1);
        int destino = leerEntero(tokenizer, "D", 0, totalNodos - 1);

        List<List<Dijkstra.Edge>> grafo = crearGrafoVacio(totalNodos);
        int[] desde = new int[totalConexiones];
        int[] hasta = new int[totalConexiones];
        long[] peso = new long[totalConexiones];

        for (int i = 0; i < totalConexiones; i++) {
            desde[i] = leerEntero(tokenizer, "A", 0, totalNodos - 1);
            hasta[i] = leerEntero(tokenizer, "B", 0, totalNodos - 1);
            peso[i] = leerPeso(tokenizer);
            agregarConexion(grafo, desde[i], hasta[i], peso[i], i);
        }

        Dijkstra dijkstra = new Dijkstra(grafo, origen);
        String linea = formatearLinea(numeroCaso, dijkstra, destino);

        if (totalNodos <= MAX_NODOS_DIBUJO) {
            GraphDrawing dibujo = construirDibujo(totalNodos, desde, hasta, peso, origen, destino, dijkstra);
            return new CaseResult(numeroCaso, linea, dibujo, null, null);
        }
        String razon = "Dibujo omitido: la instancia tiene " + totalNodos
                + " nodos y el limite es " + MAX_NODOS_DIBUJO;
        return new CaseResult(numeroCaso, linea, null, null, razon);
    }

    /** Lista de adyacencia con una lista vacia por cada nodo. */
    private List<List<Dijkstra.Edge>> crearGrafoVacio(int totalNodos) {
        List<List<Dijkstra.Edge>> grafo = new ArrayList<>();
        for (int nodo = 0; nodo < totalNodos; nodo++) {
            grafo.add(new ArrayList<>());
        }
        return grafo;
    }

    // La conexion es bidireccional: se agrega A->B y B->A con el mismo id.
    private void agregarConexion(List<List<Dijkstra.Edge>> grafo, int a, int b, long peso, int id) {
        grafo.get(a).add(new Dijkstra.Edge(b, peso, id));
        grafo.get(b).add(new Dijkstra.Edge(a, peso, id));
    }

    // Linea exacta de salida. El mensaje especial va en ASCII plano, sin tilde ni punto final.
    private String formatearLinea(int numeroCaso, Dijkstra dijkstra, int destino) {
        if (dijkstra.isReachable(destino)) {
            return "Case #" + numeroCaso + ": " + dijkstra.distanceTo(destino);
        }
        return "Case #" + numeroCaso + ": " + MENSAJE_SIN_RUTA;
    }

    private GraphDrawing construirDibujo(int totalNodos, int[] desde, int[] hasta, long[] peso,
                                         int origen, int destino, Dijkstra dijkstra) {
        boolean[] resaltada = new boolean[desde.length];
        GraphDrawing.Highlight tipo = GraphDrawing.Highlight.NONE;
        if (dijkstra.isReachable(destino)) {
            int[] camino = dijkstra.pathEdgesTo(destino);
            for (int i = 0; i < camino.length; i++) {
                resaltada[camino[i]] = true;
            }
            tipo = GraphDrawing.Highlight.PATH;
        }
        return new GraphDrawing(totalNodos, false, desde, hasta, peso, origen, destino, resaltada, tipo);
    }

    // verifica que el entero este dentro del rango
    private int leerEntero(Tokenizer tokenizer, String nombre, int minimo, int maximo) throws InputFormatException {
        int valor = tokenizer.nextInt();
        if (valor < minimo || valor > maximo) {
            throw new InputFormatException(nombre + " debe estar entre " + minimo + " y " + maximo
                    + ", se encontro " + valor + " (token " + tokenizer.position() + ")");
        }
        return valor;
    }

    private long leerPeso(Tokenizer tokenizer) throws InputFormatException {
        long valor = tokenizer.nextLong();
        if (valor < 0 || valor > MAX_PESO) {
            throw new InputFormatException("W debe estar entre 0 y " + MAX_PESO
                    + ", se encontro " + valor + " (token " + tokenizer.position() + ")");
        }
        return valor;
    }
}
