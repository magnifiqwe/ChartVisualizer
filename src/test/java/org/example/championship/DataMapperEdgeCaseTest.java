package org.example.championship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataMapperEdgeCaseTest {

    private IResolver resolverMock;
    private DataMapper mapper;

    @BeforeEach
    void setUp() {
        resolverMock = mock(IResolver.class);
        mapper = new DataMapper(resolverMock);
    }

    // ---------------------------------------------------------------
    // 1. Пустой список игроков → все методы возвращают «нулевые» значения
    // ---------------------------------------------------------------
    @Test
    void testAllEmptyCollections() {
        when(resolverMock.getPlayers()).thenReturn(Collections.emptyList());
        when(resolverMock.getTeams()).thenReturn(Collections.emptySet());

        assertTrue(mapper.getTop10TeamsByTransferCost().isEmpty());
        assertEquals(0, mapper.getTotalTeamsCount());
        assertEquals(0.0, mapper.getAverageTransferCost(), 0.0001);
    }

    // ---------------------------------------------------------------
    // 2. Точная проверка порядка в топ‑10 (должен быть упорядочен по убыванию)
    // ---------------------------------------------------------------
    @Test
    void testTop10OrderAndContent() {
        List<Player> players = List.of(
                new Player("P1", "TeamA", "C1", "FORWARD", "DE", "A", 5_000_000L, 10, 2, 1, 0, 0),
                new Player("P2", "TeamB", "C2", "MIDFIELD", "DE", "B", 4_000_000L, 12, 1, 1, 0, 0),
                new Player("P3", "TeamA", "C1", "DEFENDER", "DE", "A", 3_000_000L, 8, 0, 0, 0, 0),
                new Player("P4", "TeamC", "C3", "GOALKEEPER", "DE", "C", 1_000_000L, 5, 0, 0, 0, 0)
        );

        when(resolverMock.getPlayers()).thenReturn(players);

        Map<String, Long> top = mapper.getTop10TeamsByTransferCost();
        assertEquals(3, top.size());           // только 3 разных команды
        Iterator<Map.Entry<String, Long>> it = top.entrySet().iterator();

        Map.Entry<String, Long> first = it.next();
        assertEquals("TeamA", first.getKey());
        assertEquals(8_000_000L, first.getValue()); // 5M + 3M

        Map.Entry<String, Long> second = it.next();
        assertEquals("TeamB", second.getKey());
        assertEquals(4_000_000L, second.getValue());

        Map.Entry<String, Long> third = it.next();
        assertEquals("TeamC", third.getKey());
        assertEquals(1_000_000L, third.getValue());
    }

    // ---------------------------------------------------------------
    // 3. Проверка среднего Transfer Cost, когда в списке есть и нули
    // ---------------------------------------------------------------
    @Test
    void testAverageTransferCostWithZeros() {
        List<Player> players = List.of(
                new Player("P1", "T1", "C1", "FORWARD", "DE", "A", 0L, 0, 0, 0, 0, 0),
                new Player("P2", "T2", "C2", "MIDFIELD", "DE", "B", 2_000_000L, 0, 0, 0, 0, 0)
        );
        when(resolverMock.getPlayers()).thenReturn(players);
        double avg = mapper.getAverageTransferCost();
        assertEquals(1_000_000.0, avg, 0.001);
    }

    // ---------------------------------------------------------------
    // 4. Проверка getTotalTeamsCount, когда getTeams() возвращает Set‑с дублями (по сути Set не имеет дублей)
    // ---------------------------------------------------------------
    @Test
    void testTotalTeamsCountFromResolver() {
        Set<String> teams = new HashSet<>(Arrays.asList("A", "B", "C"));
        when(resolverMock.getTeams()).thenReturn(teams);
        assertEquals(3, mapper.getTotalTeamsCount());
    }
}
