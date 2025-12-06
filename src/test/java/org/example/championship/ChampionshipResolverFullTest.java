package org.example.championship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChampionshipResolverFullTest {

    @TempDir
    File tempDir;

    /* -----------------------------------------------------------------
       Вспомогательный метод записи CSV‑файла.
       ----------------------------------------------------------------- */
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

    /* -----------------------------------------------------------------
       1️⃣ Пустой файл → всё 0 / пустые коллекции.
       ----------------------------------------------------------------- */
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

    /* -----------------------------------------------------------------
       2️⃣ Файл без немецких игроков.
       ----------------------------------------------------------------- */
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

    /* -----------------------------------------------------------------
       3️⃣ Файл без защитников.
       ----------------------------------------------------------------- */
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

    /* -----------------------------------------------------------------
       4️⃣ Тестируем парсинг чисел‑ошибок (все числовые поля → 0).
       ----------------------------------------------------------------- */
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

    /* -----------------------------------------------------------------
       5️⃣ Тестируем топ‑5 команд по голам – важен порядок.
       ----------------------------------------------------------------- */
    @Test
    void testTop5TeamsByGoalsCountOrdering() throws IOException {
        List<String> rows = List.of(
                "A;T1;C1;FORWARD;Germany;A;1000;10;5;0;0;0",
                "B;T2;C2;DEFENDER;Germany;B;2000;12;7;0;0;0",
                "C;T3;C3;MIDFIELD;Germany;C;1500;8;3;0;0;0",
                "D;T1;C1;FORWARD;Germany;A;1200;9;6;0;0;0",
                "E;T2;C2;DEFENDER;Germany;B;1300;10;4;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        Map<String, Integer> top = r.getTop5TeamsByGoalsCount(); // {T2=11, T1=11, T3=3}
        assertEquals(3, top.size());

        // В случае одинаковых сумм порядок может быть любым.
        // Поэтому проверяем, что первые два значения обе равны 11, а не конкретный ключ.
        Iterator<Map.Entry<String, Integer>> it = top.entrySet().iterator();

        Map.Entry<String, Integer> first = it.next();
        Map.Entry<String, Integer> second = it.next();
        Map.Entry<String, Integer> third = it.next();

        assertEquals(11, first.getValue());
        assertEquals(11, second.getValue());
        assertEquals(3, third.getValue());

        // И проверяем, что набор ключей действительно {T1, T2, T3}
        Set<String> expectedKeys = Set.of("T1", "T2", "T3");
        assertEquals(expectedKeys, top.keySet());
    }

    /* -----------------------------------------------------------------
       6️⃣ Самый дорогой немецкий игрок (позиция переводится в русский).
       ----------------------------------------------------------------- */
    @Test
    void testExpensiveGermanPlayerPosition() throws IOException {
        List<String> rows = List.of(
                "P1;TeamA;C1;DEFENDER;Germany;A;5000;10;2;1;0;0",
                "P2;TeamB;C2;FORWARD;Germany;B;15000;12;5;2;1;0", // самый дорогой
                "P3;TeamC;C3;GOALKEEPER;Germany;C;10000;8;0;0;0;0"
        );
        File f = writeCsv(rows);
        ChampionshipResolver r = new ChampionshipResolver(f.getAbsolutePath());

        assertEquals("Нападающий", r.getTheExpensiveGermanPlayerPosition());
    }

    /* -----------------------------------------------------------------
       7️⃣ getPlayersByPosition (соединяем имена через запятую).
       ----------------------------------------------------------------- */
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
        assertEquals(3, map.size());

        String def = map.get("DEFENDER");
        assertTrue(def.contains("A"));
        assertTrue(def.contains("B"));
        assertTrue(map.get("FORWARD").contains("C"));
        assertTrue(map.get("MIDFIELD").contains("D"));
    }

    /* -----------------------------------------------------------------
       8️⃣ getTeams – проверяем уникальность названий.
       ----------------------------------------------------------------- */
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

    /* -----------------------------------------------------------------
       9️⃣ Тестируем getAgencyWithMinPlayersCount, когда есть «тройка» одинаковых.
       ----------------------------------------------------------------- */
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

        // A1 и A2 имеют по 2 игрока → выбираем любой (т.к. минимальное количество = 2)
        String agency = r.getAgencyWithMinPlayersCount();
        assertTrue(Set.of("A1", "A2").contains(agency));
    }

    /* -----------------------------------------------------------------
       10️⃣ getTheRudestTeam – проверяем tie‑ситуацию.
       ----------------------------------------------------------------- */
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

        // Среднее: TeamA = (2+1)/2 = 1.5, TeamB = (2+1)/2 = 1.5 → tie
        String rudest = r.getTheRudestTeam();
        assertTrue(Set.of("TeamA", "TeamB").contains(rudest));
    }
}
