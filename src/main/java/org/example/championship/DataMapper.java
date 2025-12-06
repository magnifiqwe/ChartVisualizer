package org.example.championship;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилита‑маппер, которая подготавливает данные
 * для визуализации (например, для JFreeChart).
 */
public class DataMapper {

    private final IResolver resolver;

    public DataMapper(IResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Топ‑10 команд по сумме трансферных стоимостей.
     *
     * @return упорядоченную карту <команда, суммарная стоимость>
     */
    public Map<String, Long> getTop10TeamsByTransferCost() {
        return resolver.getPlayers().stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.summingLong(Player::getTransferCost)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /** Общее количество команд в чемпионате. */
    public int getTotalTeamsCount() {
        return resolver.getTeams().size();
    }

    /** Средняя трансферная стоимость всех игроков. */
    public double getAverageTransferCost() {
        List<Player> list = resolver.getPlayers();
        if (list.isEmpty()) {
            return 0.0;
        }
        return list.stream()
                .mapToLong(Player::getTransferCost)
                .average()
                .orElse(0.0);
    }
}
