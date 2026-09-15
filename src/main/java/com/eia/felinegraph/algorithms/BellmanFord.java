package com.eia.felinegraph.algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Bellman-Ford adaptado a maximizacion desde un unico origen S. Complejidad O(N * M): relaja las
 * M aristas N - 1 veces (cota superior de un camino simple), y una ronda extra detecta que nodos
 * todavia mejoran, lo que solo puede pasar si estan alimentados por un ciclo de ganancia
 * positiva. Es el algoritmo correcto para esta mision porque, a diferencia de Floyd-Warshall, deja
 * un arbol de caminos desde S que sirve para reconstruir la ruta (o el ciclo) que se resalta en
 * el dibujo, y porque el enunciado exige exercitar ambos algoritmos en cada caso para el
 * cross-check.
 */
public final class BellmanFord {

    public static final long NO_ROUTE = Long.MIN_VALUE;
    public static final long UNBOUNDED = Long.MAX_VALUE;

    private final int n;
    private final int start;
    private final int[] edgeFrom;
    private final int[] edgeTo;
    private final long[] dist;
    private final int[] parentNode;
    private final int[] parentEdge;
    private final boolean[] unbounded;
    private final List<List<Integer>> adyacenciaSalida;

    public BellmanFord(int n, int[] edgeFrom, int[] edgeTo, long[] edgeWeight, int start) {
        this.n = n;
        this.start = start;
        this.edgeFrom = edgeFrom;
        this.edgeTo = edgeTo;
        this.dist = new long[n];
        this.parentNode = new int[n];
        this.parentEdge = new int[n];
        this.unbounded = new boolean[n];
        Arrays.fill(dist, NO_ROUTE);
        Arrays.fill(parentNode, -1);
        Arrays.fill(parentEdge, -1);
        dist[start] = 0;

        int totalAristas = edgeFrom.length;
        for (int ronda = 1; ronda < n; ronda++) {
            if (!relajarTodas(edgeWeight, totalAristas)) {
                break;
            }
        }

        boolean[] marcadoDirecto = detectarNodosQueSiguenMejorando(edgeWeight, totalAristas);
        adyacenciaSalida = construirAdyacencia(totalAristas);
        propagarNoAcotado(marcadoDirecto);

        for (int v = 0; v < n; v++) {
            if (unbounded[v]) {
                dist[v] = UNBOUNDED;
            }
        }
    }

    private boolean relajarTodas(long[] edgeWeight, int totalAristas) {
        boolean cambio = false;
        for (int e = 0; e < totalAristas; e++) {
            int u = edgeFrom[e];
            int v = edgeTo[e];
            if (dist[u] == NO_ROUTE) {
                continue;
            }
            long candidato = dist[u] + edgeWeight[e];
            if (dist[v] == NO_ROUTE || candidato > dist[v]) {
                dist[v] = candidato;
                parentNode[v] = u;
                parentEdge[v] = e;
                cambio = true;
            }
        }
        return cambio;
    }

    // una ronda extra de solo-lectura: si una arista todavia relaja, el destino esta alimentado
    // por (o pertenece a) un ciclo de ganancia positiva.
    private boolean[] detectarNodosQueSiguenMejorando(long[] edgeWeight, int totalAristas) {
        boolean[] marcado = new boolean[n];
        for (int e = 0; e < totalAristas; e++) {
            int u = edgeFrom[e];
            int v = edgeTo[e];
            if (dist[u] == NO_ROUTE) {
                continue;
            }
            long candidato = dist[u] + edgeWeight[e];
            if (dist[v] == NO_ROUTE || candidato > dist[v]) {
                marcado[v] = true;
            }
        }
        return marcado;
    }

    private List<List<Integer>> construirAdyacencia(int totalAristas) {
        List<List<Integer>> adyacencia = new ArrayList<>();
        for (int nodo = 0; nodo < n; nodo++) {
            adyacencia.add(new ArrayList<>());
        }
        for (int e = 0; e < totalAristas; e++) {
            adyacencia.get(edgeFrom[e]).add(edgeTo[e]);
        }
        return adyacencia;
    }

    // un nodo marcado directamente contagia el "no acotado" a todo lo que puede alcanzar: si se
    // puede dar la vuelta al ciclo indefinidamente antes de seguir camino, cualquier nodo
    // despues del ciclo tambien recibe churun infinito.
    private void propagarNoAcotado(boolean[] marcadoDirecto) {
        Deque<Integer> cola = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (marcadoDirecto[v]) {
                unbounded[v] = true;
                cola.add(v);
            }
        }
        while (!cola.isEmpty()) {
            int u = cola.poll();
            for (int v : adyacenciaSalida.get(u)) {
                if (!unbounded[v]) {
                    unbounded[v] = true;
                    cola.add(v);
                }
            }
        }
    }

    public boolean isReachable(int nodo) {
        return dist[nodo] != NO_ROUTE;
    }

    public boolean isUnbounded(int nodo) {
        return unbounded[nodo];
    }

    public long value(int nodo) {
        return dist[nodo];
    }

    /** Aristas del camino de S a nodo, en orden, usando el arbol de padres de la ultima ronda. */
    public int[] pathEdgesTo(int nodo) {
        if (!isReachable(nodo) || nodo == start) {
            return new int[0];
        }
        List<Integer> alReves = new ArrayList<>();
        int actual = nodo;
        while (actual != start) {
            int arista = parentEdge[actual];
            if (arista < 0) {
                return new int[0];
            }
            alReves.add(arista);
            actual = parentNode[actual];
        }
        int[] camino = new int[alReves.size()];
        for (int i = 0; i < camino.length; i++) {
            camino[i] = alReves.get(alReves.size() - 1 - i);
        }
        return camino;
    }

    /**
     * Aristas del ciclo de ganancia positiva responsable de que 'objetivo' sea no acotado, si tal
     * ciclo puede alcanzarlo. Tecnica estandar: caminar N pasos hacia atras por el arbol de
     * padres desde un nodo que todavia mejoraba garantiza caer dentro del ciclo; desde ahi se
     * sigue el mismo arbol de padres, que dentro del ciclo forma un lazo cerrado.
     */
    public int[] responsibleCycleEdges(int objetivo) {
        if (!unbounded[objetivo]) {
            return new int[0];
        }
        for (int v = 0; v < n; v++) {
            if (parentEdge[v] < 0) {
                continue;
            }
            int actual = aterrizarEnElCiclo(v);
            if (actual < 0) {
                continue;
            }
            int[] ciclo = trazarCiclo(actual);
            if (ciclo.length > 0 && cicloAlcanza(actual, objetivo)) {
                return ciclo;
            }
        }
        return new int[0];
    }

    private int aterrizarEnElCiclo(int nodoInicial) {
        int actual = nodoInicial;
        for (int paso = 0; paso < n; paso++) {
            if (parentNode[actual] < 0) {
                return -1;
            }
            actual = parentNode[actual];
        }
        return actual;
    }

    private int[] trazarCiclo(int inicioCiclo) {
        List<Integer> aristas = new ArrayList<>();
        boolean[] visitado = new boolean[n];
        int actual = inicioCiclo;
        while (!visitado[actual]) {
            visitado[actual] = true;
            int arista = parentEdge[actual];
            if (arista < 0) {
                return new int[0];
            }
            aristas.add(arista);
            actual = parentNode[actual];
            if (actual == inicioCiclo) {
                break;
            }
        }
        int[] resultado = new int[aristas.size()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = aristas.get(i);
        }
        return resultado;
    }

    private boolean cicloAlcanza(int nodoDelCiclo, int objetivo) {
        boolean[] visitado = new boolean[n];
        Deque<Integer> cola = new ArrayDeque<>();
        visitado[nodoDelCiclo] = true;
        cola.add(nodoDelCiclo);
        while (!cola.isEmpty()) {
            int u = cola.poll();
            if (u == objetivo) {
                return true;
            }
            for (int v : adyacenciaSalida.get(u)) {
                if (!visitado[v]) {
                    visitado[v] = true;
                    cola.add(v);
                }
            }
        }
        return false;
    }
}
