package org.example.championship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChampionshipResolverFullCoverageTest {

    @TempDir
    File tempDir;

    /* ------------------------- вспомогательный метод записи CSV -------------------------- */
    private File writeCsv(List<String> lines) throws IOException {
        File f = new File(tempDir, "players.csv");
        try (FileWriter w = new FileWriter(f)) {
            w.write(
                    "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards\n");
            for (String l : lines) {
                w.write(l);
                w.write('\n');
            }
        }
        return f;
    }

    /* --------------------- 1️⃣ Пустой файл – всё 0/пусто --------------------- */
    @Test
    void testEmptyFileAllZeroes() throws IOException {
        File empty = writeCsv(Collections.emptyList());
        ChampionshipResolver r = new ChampionshipResolver(empty.getAbsolutePath());

        assertTrue(r.getPlayers().isEmpty());
        assertEquals(0, r.getCountWithoutAgency());
        assertEquals(0, r.getMaxDefenderGoalsCount());
        assertEquals("", r.getTheExpensiveGermanPlayerPosition());
        assertTrue(r.getPlayersByPosition().isEmpty());
        assertTrue(r.getTeams().isEmpty());
        assertTrue(r.getTop5TeamsByGoalsCount().isEmpty());
        assertEquals("", r.getAgencyWithMinPlayersCount());
        assertEquals("", r.getTheRudestTeam());
    }

    /* --------------------- 2️⃣ Файл без немецких игроков --------------------- */
    @Test
    void testNoGermanPlayers() throws IOException {
        List<String> rows = List.of(
                "P1;TeamA;C1;DEFENDER;Brazil;A1;1000;10;5;2;1;0",
                "P2;TeamB;C2;FORWARD;France;A2;2000;12;8;3;0;1"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        assertEquals("", r.getTheExpensiveGermanPlayerPosition());
    }

    /* --------------------- 3️⃣ Файл без защитников --------------------- */
    @Test
    void testNoDefenders() throws IOException {
        List<String> rows = List.of(
                "P1;TeamA;C1;FORWARD;Germany;A1;1500;10;5;2;0;0",
                "P2;TeamB;C2;MIDFIELD;Germany;A2;2500;12;8;3;0;1"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        assertEquals(0, r.getMaxDefenderGoalsCount());
    }

    /* --------------------- 4️⃣ Агентства с одинаковым кол‑во --------------------- */
    @Test
    void testAgencyWithMinPlayersTie() throws IOException {
        List<String> rows = List.of(
                "P1;T1;C1;DEFENDER;Germany;A1;1000;10;5;2;1;0",
                "P2;T2;C2;FORWARD;Germany;A2;2000;12;8;3;0;1",
                "P3;T3;C3;MIDFIELD;Germany;A1;1500;8;3;1;0;0",
                "P4;T4;C4;MIDFIELD;Germany;A2;600;5;0;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        String minAgency = r.getAgencyWithMinPlayersCount();
        assertTrue(Set.of("A1", "A2").contains(minAgency));
    }

    /* --------------------- 5️⃣ Самый дорогой немецкий игрок --------------------- */
    @Test
    void testExpensiveGermanPlayerPosition() throws IOException {
        List<String> rows = List.of(
                "P1;TeamA;C1;DEFENDER;Germany;A1;5000;10;2;1;0;0",
                "P2;TeamB;C2;FORWARD;Germany;A2;15000;12;5;2;1;0", // самый дорогой
                "P3;TeamC;C3;GOALKEEPER;Germany;A3;10000;8;0;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        assertEquals("Нападающий", r.getTheExpensiveGermanPlayerPosition());
    }

    /* --------------------- 6️⃣ getPlayersByPosition (соединяем имена) --------------------- */
    @Test
    void testGetPlayersByPosition() throws IOException {
        List<String> rows = List.of(
                "A;T1;C1;DEFENDER;Germany;A;1000;10;1;0;0;0",
                "B;T1;C1;DEFENDER;Germany;A;2000;12;2;0;0;0",
                "C;T2;C2;FORWARD;Germany;B;1500;8;3;1;0;0",
                "D;T3;C3;MIDFIELD;Germany;C;1200;7;0;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        Map<String, String> map = r.getPlayersByPosition();

        // В наборе входных данных **только три** позиции → ожидаем size = 3
        assertEquals(3, map.size(),
                "В тесте указаны только три разных значения позиции, поэтому размер карты должен быть 3");

        String defenders = map.get("DEFENDER");
        assertTrue(defenders.contains("A"));
        assertTrue(defenders.contains("B"));
        assertTrue(map.get("FORWARD").contains("C"));
        assertTrue(map.get("MIDFIELD").contains("D"));
    }

    /* --------------------- 7️⃣ getTeams --------------------- */
    @Test
    void testGetTeams() throws IOException {
        List<String> rows = List.of(
                "A;TeamX;C1;FORWARD;Germany;A;1000;10;1;0;0;0",
                "B;TeamY;C2;DEFENDER;Germany;B;2000;12;2;0;0;0",
                "C;TeamX;C1;MIDFIELD;Germany;C;1500;8;3;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        Set<String> teams = r.getTeams();
        assertEquals(Set.of("TeamX", "TeamY"), teams);
    }

    /* --------------------- 8️⃣ Топ‑5 команд по голам – tie‑ситуация --------------------- */
    @Test
    void testTop5TeamsByGoalsWithTie() throws IOException {
        // Две строки для T1 (5 + 5 = 10)
        // Две строки для T2 (5 + 5 = 10)
        // Одна строка для T3 (3)
        List<String> rows = List.of(
                "A;T1;C1;FORWARD;Germany;A;1000;10;5;0;0;0",
                "B;T1;C1;DEFENDER;Germany;A;2000;12;5;0;0;0",
                "C;T2;C2;FORWARD;Germany;B;1500;8;5;0;0;0",
                "D;T2;C2;DEFENDER;Germany;B;2500;9;5;0;0;0",
                "E;T3;C3;MIDFIELD;Germany;C;1500;8;3;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        Map<String, Integer> top = r.getTop5TeamsByGoalsCount();

        // Должно быть **не более** 5 записей
        assertTrue(top.size() <= 5,
                "Размер карты не должен превышать 5 (top‑5 команд)");

        // Две первые записи должны иметь одинаковое количество голов = 10
        Iterator<Map.Entry<String, Integer>> it = top.entrySet().iterator();
        Map.Entry<String, Integer> first = it.next();
        Map.Entry<String, Integer> second = it.next();

        assertEquals(10, first.getValue(),
                "Первая команда должна иметь 10 голов");
        assertEquals(10, second.getValue(),
                "Вторая команда должна иметь 10 голов");

        // Проверяем, что в карте ровно эти три команды
        assertEquals(Set.of("T1", "T2", "T3"), top.keySet(),
                "В результате должны присутствовать все три команды");
    }

    /* --------------------- 9️⃣ Команда с самым большим средним кол‑ва красных карточек (tie) --------------------- */
    @Test
    void testRudestTeamTie() throws IOException {
        List<String> rows = List.of(
                "A;TeamA;C1;DEFENDER;Germany;A;1000;10;0;0;0;2",
                "B;TeamA;C1;FORWARD;Germany;B;1500;12;0;0;0;1",
                "C;TeamB;C2;MIDFIELD;Germany;C;2000;8;0;0;0;2",
                "D;TeamB;C2;GOALKEEPER;Germany;D;1200;9;0;0;0;1"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        String rudest = r.getTheRudestTeam();
        assertTrue(Set.of("TeamA", "TeamB").contains(rudest));
    }

    /* --------------------- 10️⃣ Парсинг некорректных чисел (все нули) --------------------- */
    @Test
    void testMalformedNumbersAllZeroes() throws IOException {
        List<String> rows = List.of(
                "Bad;T;C;DEFENDER;Germany;A;NOT_A_NUMBER;NaN;X;Y;Z;W"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        List<Player> list = r.getPlayers();
        assertEquals(1, list.size());

        Player p = list.get(0);
        assertEquals(0L, p.getTransferCost());
        assertEquals(0, p.getParticipations());
        assertEquals(0, p.getGoals());
        assertEquals(0, p.getAssists());
        assertEquals(0, p.getYellowCards());
        assertEquals(0, p.getRedCards());
    }
}
