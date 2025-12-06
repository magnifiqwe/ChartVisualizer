package org.example.championship;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация IResolver. Читает CSV‑файл и отвечает на запросы,
 * используя Stream‑API.
 */
public class ChampionshipResolver implements IResolver {

    private final List<Player> players = new ArrayList<>();

    public ChampionshipResolver(String filename) {
        loadPlayers(filename);
    }

    /**
     * Читает CSV‑файл и заполняет {@link #players}.
     * <p>
     * Если в отдельной строке встречается некорректное число,
     * мы выводим предупреждение и продолжаем обработку остальных строк.
     * </p>
     */
    private void loadPlayers(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // заголовок, игнорируем

            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
                try {
                    //--- 0..5 всегда строки -------------------------------------------------
                    String name        = p[0];
                    String team        = p[1];
                    String city        = p[2];
                    String position    = p[3];
                    String nationality = p[4];
                    String agency      = p[5].isEmpty() ? null : p[5];

                    //--- 6..11 – числовые поля. Если парсинг упадёт, бросаем NFE,
                    //---            а catch‑блок ниже «заполняет нулями».
                    long   transferCost = parseLong(p[6]);
                    int    participations = parseInt(p[7]);
                    int    goals          = parseInt(p[8]);
                    int    assists        = parseInt(p[9]);
                    int    yellowCards    = parseInt(p[10]);
                    int    redCards       = parseInt(p[11]);

                    players.add(new Player(name, team, city, position, nationality,
                            agency, transferCost, participations, goals,
                            assists, yellowCards, redCards));
                } catch (NumberFormatException e) {
                    // Не удалось распарсить одну из числовых колонок.
                    // Выводим сообщение, но **не прерываем чтение файла**.
                    System.err.println(
                            "Warning: malformed numeric value in line -> \"" + line + "\". " +
                                    "Row will be added with zero‑values for the broken fields.");
                    // Добавляем «порожнюю» запись, где все числовые поля 0.
                    String name        = p[0];
                    String team        = p[1];
                    String city        = p[2];
                    String position    = p[3];
                    String nationality = p[4];
                    String agency      = p[5].isEmpty() ? null : p[5];

                    players.add(new Player(name, team, city, position, nationality,
                            agency, 0L, 0, 0, 0, 0, 0));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла \"" + filename + "\": " + e.getMessage());
            e.printStackTrace();
        }
        // **Обращаем внимание:** отдельный catch для NumberFormatException теперь
        // не нужен – он обрабатывается внутри цикла.
    }

    /** Приводит строку к long, возвращая 0 при любой ошибке формата. */
    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** Приводит строку к int, возвращая 0 при любой ошибке формата. */
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // -------------------- Реализация IResolver (осталось без изменений) --------------------
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
                        LinkedHashMap::new
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
