package org.example.championship;

import java.util.Map;
import java.util.Set;

public class ChampionshipMain {
    public static void main(String[] args) {
        ChampionshipResolver resolver = new ChampionshipResolver("src/main/resources/fakePlayers.csv");

        System.out.println("1. Количество игроков без агентства: " + resolver.getCountWithoutAgency());

        System.out.println("2. Максимальное число голов у защитника: " + resolver.getMaxDefenderGoalsCount());

        System.out.println("3. Позиция самого дорогого немецкого игрока: " + resolver.getTheExpensiveGermanPlayerPosition());

        System.out.println("\n4. Игроки по позициям:");
        Map<String, String> playersByPosition = resolver.getPlayersByPosition();
        for (Map.Entry<String, String> entry : playersByPosition.entrySet()) {
            System.out.println("   " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\n5. Все команды в чемпионате:");
        Set<String> teams = resolver.getTeams();
        System.out.println("   Всего команд: " + teams.size());
        teams.stream().limit(10).forEach(team -> System.out.println("   - " + team));

        System.out.println("\n6. Топ-5 команд по забитым голам:");
        Map<String, Integer> topTeams = resolver.getTop5TeamsByGoalsCount();
        int place = 1;
        for (Map.Entry<String, Integer> entry : topTeams.entrySet()) {
            System.out.println("   " + (place++) + ". " + entry.getKey() + " - " + entry.getValue() + " голов");
        }

        System.out.println("7. Агентство с минимальным количеством игроков: " + resolver.getAgencyWithMinPlayersCount());

        System.out.println("8. Команда с наибольшим средним числом удалений: " + resolver.getTheRudestTeam());
    }
}
