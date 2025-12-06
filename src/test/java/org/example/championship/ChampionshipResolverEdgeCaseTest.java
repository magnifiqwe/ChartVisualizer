package org.example.championship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChampionshipResolverEdgeCaseTest {

    @TempDir
    File tempDir;

    private File csvFile;
    private ChampionshipResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        csvFile = new File(tempDir, "players.csv");
        writeCsv(csvFile);
        resolver = new ChampionshipResolver(csvFile.getAbsolutePath());
    }

    private void writeCsv(File file) throws IOException {
        try (FileWriter w = new FileWriter(file)) {
            w.write("Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards\n");
            w.write("P1;TeamA;CityA;DEFENDER;Germany;Agency1;1000;10;5;2;1;0\n");
            w.write("P2;TeamB;CityB;FORWARD;Brazil;;2000;12;8;3;2;1\n");
        }
    }

    @Test
    void testCountWithoutAgency() {
        // В файле один игрок без агентства → ожидаем 1
        assertEquals(1, resolver.getCountWithoutAgency());
    }

    @Test
    void testMaxDefenderGoals() {
        // Защитник (P1) забил 5 голов → максимум 5
        assertEquals(5, resolver.getMaxDefenderGoalsCount());
    }

    @Test
    void testWithTryCatchDuringSetup() {
        // Пример, когда в одном тесте мы хотим создать отдельный файл без объявления throws
        File another = new File(tempDir, "another.csv");
        try (FileWriter w = new FileWriter(another)) {
            w.write("Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards\n");
            w.write("X;T;C;MIDFIELD;France;;0;0;0;0;0;0\n");
        } catch (IOException e) {
            fail("Не удалось записать временный CSV‑файл", e);
        }

        // Создаём резольвер так же, как в setUp()
        ChampionshipResolver anotherResolver = new ChampionshipResolver(another.getAbsolutePath());

        // В этом файле нет защитников → максимум 0
        assertEquals(0, anotherResolver.getMaxDefenderGoalsCount());
    }

    @Test
    void testMalformedNumbersDoNotCrash() throws IOException {
        File malformed = new File(tempDir, "malformed.csv");
        try (FileWriter w = new FileWriter(malformed)) {
            w.write("Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards\n");
            w.write("Bad;T;C;DEFENDER;Germany;A;NOT_A_NUMBER;NaN;X;Y;Z;W\n");
        }

        // Если в конструкторе возникнет необработанное NumberFormatException,
        // тест провалится. Мы ожидаем, что резолвер «съест» ошибку и создаст игрока
        // с нулевыми числовыми полями.
        ChampionshipResolver r = new ChampionshipResolver(malformed.getAbsolutePath());
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
