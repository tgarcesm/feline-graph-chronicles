package com.eia.felinegraph.core;

public interface Mission {

    String title();

    String sampleInput();

    MissionResult solve(String rawInput) throws InputFormatException;
}
