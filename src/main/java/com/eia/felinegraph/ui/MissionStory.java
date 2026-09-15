package com.eia.felinegraph.ui;

import com.eia.felinegraph.core.Mission;
import com.eia.felinegraph.missions.Mission1Minefield;
import com.eia.felinegraph.missions.Mission2Accounts;
import com.eia.felinegraph.missions.Mission3Churun;
import com.eia.felinegraph.missions.Mission4Network;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * La parte "narrativa" de cada mision: nombre, historia, color, que gato la cuenta y que dice
 * cada gato segun el resultado de un caso.
 *
 * La mision (Mission) sabe resolver; esta clase solo sabe contar la historia alrededor. Asi la
 * GUI sigue sin conocer los algoritmos: solo compara el texto de salida con los mensajes
 * especiales del enunciado ("Nina is very sad", "Limon blocked the way", etc.).
 *
 * Aqui se juntan las cuatro misiones del grupo:
 *  - Mision 1 (Samuel):    Mission1Minefield
 *  - Mision 2 (Sebastian): Mission2Accounts
 *  - Mision 3 (Tomas):     Mission3Churun
 *  - Mision 4 (Samuel):    Mission4Network
 */
public final class MissionStory {

    public final Mission mission;
    public final String nombre;
    public final String algoritmo;
    public final String historia;
    public final String colorAcento;
    public final String narrador;
    public final String fraseInicio;
    public final String fraseExito;
    public final String villano;
    public final String mensajeVillano;
    public final String fraseVillano;
    public final String gatoEspecial;
    public final String mensajeEspecial;
    public final String fraseEspecial;

    private MissionStory(Mission mission, String nombre, String algoritmo, String historia, String colorAcento,
                         String narrador, String fraseInicio, String fraseExito,
                         String villano, String mensajeVillano, String fraseVillano,
                         String gatoEspecial, String mensajeEspecial, String fraseEspecial) {
        this.mission = mission;
        this.nombre = nombre;
        this.algoritmo = algoritmo;
        this.historia = historia;
        this.colorAcento = colorAcento;
        this.narrador = narrador;
        this.fraseInicio = fraseInicio;
        this.fraseExito = fraseExito;
        this.villano = villano;
        this.mensajeVillano = mensajeVillano;
        this.fraseVillano = fraseVillano;
        this.gatoEspecial = gatoEspecial;
        this.mensajeEspecial = mensajeEspecial;
        this.fraseEspecial = fraseEspecial;
    }

    /** Las cuatro misiones, en el orden en que aparecen en el selector. */
    public static List<MissionStory> todas() {
        List<MissionStory> misiones = new ArrayList<>();

        misiones.add(new MissionStory(new Mission1Minefield(),
                "El campo minado", "BFS + DFS",
                "Limón escondió a Nina al final de un campo lleno de bombas. Pola y Minerva deben cruzarlo sin pisar "
                        + "ninguna: BFS encuentra la ruta más corta y DFS una ruta válida con el orden arriba, abajo, izquierda, derecha.",
                "#E4574C",
                "pola", "¡Cuidado dónde pisas, Minerva! Pega el mapa de bombas y lo cruzamos juntas.",
                "¡Llegamos sin pisar ni una bomba! BFS siempre da el camino más corto en una grilla sin pesos.",
                "limon", "Nina is unreachable", "¡Jajaja! Rodeé a Nina de bombas. Por ahí nunca van a llegar.",
                null, null, null));

        misiones.add(new MissionStory(new Mission2Accounts(),
                "Las cuentas robadas", "Dijkstra",
                "Nero fue descuidado y dejó tirado el mapa de la red donde esconden las cuentas de Claude. Cada conexión "
                        + "tiene un costo: hay que llegar al servidor por la ruta más barata.",
                "#4FC1B9",
                "minerva", "Nero dejó su mapa tirado. Con Dijkstra encontramos la ruta más barata al servidor.",
                "¡Ruta más barata encontrada! Una cuenta de Claude menos en las garras de Limón.",
                "nero", "Nina is very sad", "Ese servidor ni siquiera está conectado. Nina se quedará muy triste, miau.",
                null, null, null));

        misiones.add(new MissionStory(new Mission3Churun(),
                "La reserva de churun", "Floyd-Warshall + Bellman-Ford",
                "Antes de la batalla final hay que reunir todo el churun posible. Limón envenenó algunos pasadizos "
                        + "(pesos negativos) y otros forman ciclos que dan churun sin fin.",
                "#F0A93B",
                "pola", "¿Churun? ¡Churun! Déjamelo a mí, sé exactamente cuánto cabe en esta pancita.",
                "¡Máximo churun calculado! Floyd-Warshall y Bellman-Ford están de acuerdo.",
                "limon", "Limon blocked the way", "Bloqueé todos los pasadizos. Ese churun es solo mío.",
                "pola", "Infinite churun!", "¿¡CHURUN INFINITO!? Este es el mejor día de mi vida."));

        misiones.add(new MissionStory(new Mission4Network(),
                "Reconectando la red", "Kruskal + Union-Find",
                "En un último acto desesperado, los villanos destruyeron los cables de la universidad. Hay que "
                        + "reconectar todas las intersecciones usando la menor cantidad de cable posible.",
                "#8FA8F0",
                "minerva", "Kruskal toma siempre el cable más barato que no cierre un ciclo. Vamos a reconectarlo todo.",
                "¡Red reconectada con el mínimo costo! La universidad vuelve a estar en línea.",
                "limon", "Limon cut too many cables", "Corté demasiados cables. Esta red no se vuelve a conectar.",
                null, null, null));

        return misiones;
    }

    /** Colores de las misiones en orden: son los colores de los barrotes de la jaula de Nina. */
    public static Color[] coloresDe(List<MissionStory> misiones) {
        Color[] colores = new Color[misiones.size()];
        for (int i = 0; i < misiones.size(); i++) {
            colores[i] = Color.web(misiones.get(i).colorAcento);
        }
        return colores;
    }
}
