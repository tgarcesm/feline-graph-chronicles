package com.eia.felinegraph.core;

import java.util.ArrayList;
import java.util.List;

public final class MissionResult {

    private final List<CaseResult> cases;
    private final List<String> warnings;

    public MissionResult(List<CaseResult> cases, List<String> warnings) {
        this.cases = new ArrayList<>(cases);
        if (warnings == null) {
            this.warnings = new ArrayList<>();
        } else {
            this.warnings = new ArrayList<>(warnings);
        }
    }

    public List<CaseResult> cases() {
        return cases;
    }

    public List<String> warnings() {
        return warnings;
    }

    /**
     * Todas las lineas "Case #k: ..." unidas con salto de linea, sin salto al final.
     * Es exactamente el texto que se compara contra la salida del enunciado en los tests.
     */
    public String consoleOutput() {
        StringBuilder salida = new StringBuilder();
        for (int i = 0; i < cases.size(); i++) {
            if (i > 0) {
                salida.append("\n");
            }
            salida.append(cases.get(i).line());
        }
        return salida.toString();
    }
}
