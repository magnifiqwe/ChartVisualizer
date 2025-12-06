package org.example.championship;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация IResolver. Читает файл CSV и отвечает на запросы,
 * используя Stream‑API.
 */
public class ChampionshipResolver implements IResolver {

    private final List<Player> players = new ArrayList<>();

    public ChampionshipResolver(String filename) {
        loadPlayers(filename);
    }

    /** Читает CSV‑файл, заполняет список {@link #players}. */
    private void loadPlayers(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // заголовок, просто игнорируем

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                // 0  Name
                // 1  Team
                // 2  City
                // 3  Position
                // 4  Nationality
                // 5  Agency (может быть пустым)
                // 6  Transfer cost
                // 7  Participations
                // 8  Goals
                // 9  Assists
                //10  Yellow cards
                //11  Red cards
                String name = parts[0];
                String team = parts[1];
                String city = parts[2];
                String position = parts[3];
                String nationality = parts[4];
                String agency = parts[5].isEmpty() ? null : parts[5];
                long transferCost = parts[6].isEmpty() ? 0L : Long.parseLong(parts[6]);
                int participations = parts[7].isEmpty() ? 0 : Integer.parseInt(parts[7]);
                int goals = parts[8].isEmpty() ? 0 : Integer.parseInt(parts[8]);
                int assists = parts[9].isEmpty() ? 0 : Integer.parseInt(parts[9]);
                int yellow = parts[10].isEmpty() ? 0 : Integer.parseInt(parts[10]);
                int red = parts[11].isEmpty() ? 0 : Integer.parseInt(parts[11]);

                players.add(new Player(name, team, city, position, nationality,
                        agency, transferCost, participations,
                        goals, assists, yellow, red));
            }
        } catch (IOException e) {
            System.err.println("Не удалось прочитать файл \"" + filename + "\": " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга чисел в файле \"" + filename + "\": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    // Методы IResolver (все используют Stream API)
    // -----------------------------------------------------------------

    @Override
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public int getCountWithoutAgency() {
        return (int) players.stream()
                .filter(p -> p.getAgency() == null || p.getAgency().isEmpty())
                .count();
    }

    @Override
    public int getMaxDefenderGoalsCount() {
        return players.stream()
                .filter(p -> "DEFENDER".equals(p.getPosition()))
                .mapToInt(Player::getGoals)
                .max()
                .orElse(0);
    }

    @Override
    public String getTheExpensiveGermanPlayerPosition() {
        return players.stream()
                .filter(p -> "Germany".equals(p.getNationality()))
                .max(Comparator.comparingLong(Player::getTransferCost))
                .map(p -> {
                    switch (p.getPosition()) {
                        case "GOALKEEPER": return "Вратарь";
                        case "DEFENDER":   return "Защитник";
                        case "MIDFIELD":   return "Полузащитник";
                        case "FORWARD":    return "Нападающий";
                        default:           return p.getPosition();
                    }
                })
                .orElse("");
    }

    @Override
    public Map<String, String> getPlayersByPosition() {
        return players.stream()
                .collect(Collectors.groupingBy(
                        Player::getPosition,
                        Collectors.mapping(Player::getName, Collectors.joining(", "))
                ));
    }

    @Override
    public Set<String> getTeams() {
        return players.stream()
                .map(Player::getTeam)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Integer> getTop5TeamsByGoalsCount() {
        return players.stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.summingInt(Player::getGoals)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new   // сохраняем порядок
                ));
    }

    @Override
    public String getAgencyWithMinPlayersCount() {
        return players.stream()
                .filter(p -> p.getAgency() != null && !p.getAgency().isEmpty())
                .collect(Collectors.groupingBy(Player::getAgency))
                .entrySet().stream()
                .min(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse("");
    }

    @Override
    public String getTheRudestTeam() {
        return players.stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.averagingInt(Player::getRedCards)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }
}
