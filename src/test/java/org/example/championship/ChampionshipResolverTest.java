package org.example.championship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChampionshipResolverTest {

    @TempDir
    File tempDir;

    private File csvFile;
    private ChampionshipResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        csvFile = new File(tempDir, "players.csv");
        writeSmallCsv(csvFile);
        resolver = new ChampionshipResolver(csvFile.getAbsolutePath());
    }

    private void writeSmallCsv(File file) throws IOException {
        try (FileWriter w = new FileWriter(file)) {
            w.write("Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards\n");
            w.write("P1;T1;C1;DEFENDER;Germany;A1;1000;10;5;2;1;0\n");
            w.write("P2;T1;C1;FORWARD;Brazil;;2000;12;7;3;2;1\n");
            w.write("P3;T2;C2;DEFENDER;Germany;A2;1500;9;8;4;0;2\n");
            w.write("P4;T3;C3;GOALKEEPER;Germany;A1;800;8;0;0;0;0\n");
            w.write("P5;T2;C2;MIDFIELD;France;A3;1200;11;3;5;1;1\n");
        }
    }

    @Test
    void testGetPlayers() {
        List<Player> list = resolver.getPlayers();
        assertEquals(5, list.size());
    }

    @Test
    void testGetCountWithoutAgency() {
        // Единственный игрок без агентства – P2
        assertEquals(1, resolver.getCountWithoutAgency());
    }

    @Test
    void testGetMaxDefenderGoalsCount() {
        // Защитники: P1 (5 голов), P3 (8 голов) → максимум 8
        assertEquals(8, resolver.getMaxDefenderGoalsCount());
    }

    @Test
    void testGetTheExpensiveGermanPlayerPosition() {
        // Самый дорогой немецкий игрок – P1 (transfer=1000) vs P3 (1500) vs P4 (800)
        // Самый дорогой – P3, позиция DEFENDER → «Защитник»
        assertEquals("Защитник", resolver.getTheExpensiveGermanPlayerPosition());
    }

    @Test
    void testGetPlayersByPosition() {
        Map<String, String> map = resolver.getPlayersByPosition();
        assertTrue(map.containsKey("DEFENDER"));
        assertTrue(map.get("DEFENDER").contains("P1"));
        assertTrue(map.get("DEFENDER").contains("P3"));
        assertTrue(map.containsKey("FORWARD"));
    }

    @Test
    void testGetTeams() {
        Set<String> teams = resolver.getTeams();
        assertEquals(Set.of("T1", "T2", "T3"), teams);
    }

    @Test
    void testGetTop5TeamsByGoalsCount() {
        Map<String, Integer> top = resolver.getTop5TeamsByGoalsCount();
        // Считаем вручную:
        // T1 → 5 + 7 = 12
        // T2 → 8 + 3 = 11
        // T3 → 0
        assertEquals(12, top.get("T1"));
        assertEquals(11, top.get("T2"));
        assertEquals(0, top.get("T3"));
    }

    @Test
    void testGetAgencyWithMinPlayersCount() {
        // A1 – 2 игрока, A2 – 1 игрок, A3 – 1 игрок
        // Ожидаем любой из {A2, A3}
        String minAgency = resolver.getAgencyWithMinPlayersCount();
        assertTrue(minAgency.equals("A2") || minAgency.equals("A3"));
    }

    @Test
    void testGetTheRudestTeam() {
        // Red cards: P1 0, P2 1, P3 2, P4 0, P5 1
        // Среднее:
        // T1 → (0+1)/2 = 0.5
        // T2 → (2+1)/2 = 1.5  <-- max
        // T3 → 0
        assertEquals("T2", resolver.getTheRudestTeam());
    }

    @Test
    void testEmptyFile() throws IOException {
        File empty = new File(tempDir, "empty.csv");
        try (FileWriter w = new FileWriter(empty)) {
            w.write("Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards\n");
        }
        ChampionshipResolver emptyResolver = new ChampionshipResolver(empty.getAbsolutePath());

        assertTrue(emptyResolver.getPlayers().isEmpty());
        assertEquals(0, emptyResolver.getCountWithoutAgency());
        assertEquals(0, emptyResolver.getMaxDefenderGoalsCount());
        assertEquals("", emptyResolver.getTheExpensiveGermanPlayerPosition());
        assertTrue(emptyResolver.getPlayersByPosition().isEmpty());
        assertTrue(emptyResolver.getTeams().isEmpty());
        assertTrue(emptyResolver.getTop5TeamsByGoalsCount().isEmpty());
        assertEquals("", emptyResolver.getAgencyWithMinPlayersCount());
        assertEquals("", emptyResolver.getTheRudestTeam());
    }
}
