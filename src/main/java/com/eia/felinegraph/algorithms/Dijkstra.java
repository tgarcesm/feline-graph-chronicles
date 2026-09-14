package com.eia.felinegraph.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Dijkstra {

    public static final long INFINITY = Long.MAX_VALUE;
    public static final class Edge {
        public final int to;
        public final long weight;
        public final int id;

        public Edge(int to, long weight, int id) {
            this.to = to;
            this.weight = weight;
            this.id = id;
        }
    }

    private static final class QueueEntry implements Comparable<QueueEntry> {
        final int node;
        final long distance;

        QueueEntry(int node, long distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(QueueEntry otro) {
            return Long.compare(this.distance, otro.distance);
        }
    }

    private final List<List<Edge>> adyacencia;
    private final int origen;
    private final long[] distancia;
    private final int[] nodoPadre;
    private final int[] aristaPadre;

    // ejecutamos de una vez
    public Dijkstra(List<List<Edge>> adyacencia, int origen) {
        this.adyacencia = adyacencia;
        this.origen = origen;
        int totalNodos = adyacencia.size();
        this.distancia = new long[totalNodos];
        this.nodoPadre = new int[totalNodos];
        this.aristaPadre = new int[totalNodos];
        ejecutar();
    }

    private void ejecutar() {
        for (int nodo = 0; nodo < distancia.length; nodo++) {
            distancia[nodo] = INFINITY;
            nodoPadre[nodo] = -1;
            aristaPadre[nodo] = -1;
        }
        distancia[origen] = 0;

        PriorityQueue<QueueEntry> cola = new PriorityQueue<>();
        cola.add(new QueueEntry(origen, 0));

        while (!cola.isEmpty()) {
            QueueEntry actual = cola.poll();
            int nodoActual = actual.node;

            //ya existe un camino mejor hacia este nodo
            if (actual.distance > distancia[nodoActual]) {
                continue;
            }
            if (distancia[nodoActual] == INFINITY) {
                continue;
            }

            for (Edge arista : adyacencia.get(nodoActual)) {
                long nuevaDistancia = distancia[nodoActual] + arista.weight;
                if (nuevaDistancia < distancia[arista.to]) {
                    distancia[arista.to] = nuevaDistancia;
                    nodoPadre[arista.to] = nodoActual;
                    aristaPadre[arista.to] = arista.id;
                    cola.add(new QueueEntry(arista.to, nuevaDistancia));
                }
            }
        }
    }

    // si existe un camino desde el origen hasta este nodo
    public boolean isReachable(int nodo) {
        return distancia[nodo] != INFINITY;
    }

    public long distanceTo(int nodo) {
        return distancia[nodo];
    }

    public int[] pathEdgesTo(int nodo) {
        if (!isReachable(nodo)) {
            return new int[0];
        }
        List<Integer> alReves = new ArrayList<>();
        int actual = nodo;
        while (actual != origen) {
            alReves.add(aristaPadre[actual]);
            actual = nodoPadre[actual];
        }
        int[] camino = new int[alReves.size()];
        for (int i = 0; i < camino.length; i++) {
            camino[i] = alReves.get(alReves.size() - 1 - i);
        }
        return camino;
    }
}
