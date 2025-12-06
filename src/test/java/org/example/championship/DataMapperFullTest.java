package org.example.championship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataMapperFullTest {

    private IResolver resolver;
    private DataMapper mapper;

    @BeforeEach
    void setUp() {
        resolver = mock(IResolver.class);
        mapper = new DataMapper(resolver);
    }

    /* ------------------- 1. Топ‑10 команд по стоимости ------------------- */
    @Test
    void testTop10TeamsByTransferCostOrdered() {
        List<Player> players = List.of(
                new Player("A","TeamA","C","FORWARD","DE","A",5_000_000L,10,5,2,0,0),
                new Player("B","TeamB","C","DEFENDER","DE","B",4_000_000L,12,8,3,0,0),
                new Player("C","TeamA","C","MIDFIELD","DE","C",3_000_000L,8,0,0,0,0),
                new Player("D","TeamC","C","GOALKEEPER","DE","D",2_000_000L,5,0,0,0,0),
                new Player("E","TeamD","C","FORWARD","DE","E",1_000_000L,7,1,0,0,0)
        );
        when(resolver.getPlayers()).thenReturn(players);

        Map<String, Long> top = mapper.getTop10TeamsByTransferCost();

        // Должны попасть все 4 команды (TeamA, TeamB, TeamC, TeamD) в порядке убывания стоимости
        List<String> expectedOrder = List.of("TeamA", "TeamB", "TeamC", "TeamD");
        assertEquals(expectedOrder, new ArrayList<>(top.keySet()));
        assertEquals(8_000_000L, top.get("TeamA"));
        assertEquals(4_000_000L, top.get("TeamB"));
        assertEquals(2_000_000L, top.get("TeamC"));
        assertEquals(1_000_000L, top.get("TeamD"));
    }

    /* ------------------- 2. Топ‑10 при пустом списке -------------------- */
    @Test
    void testTop10Empty() {
        when(resolver.getPlayers()).thenReturn(Collections.emptyList());
        assertTrue(mapper.getTop10TeamsByTransferCost().isEmpty());
    }

    /* ------------------- 3. Общее количество команд -------------------- */
    @Test
    void testTotalTeamsCount() {
        when(resolver.getTeams()).thenReturn(Set.of("A", "B", "C"));
        assertEquals(3, mapper.getTotalTeamsCount());
    }

    /* ------------------- 4. Средняя трансферная стоимость -------------- */
    @Test
    void testAverageTransferCost() {
        List<Player> players = List.of(
                new Player("A","T1","C","FORWARD","DE","A",1_000L,10,5,2,0,0),
                new Player("B","T2","C","DEFENDER","DE","B",9_000L,12,8,3,0,0)
        );
        when(resolver.getPlayers()).thenReturn(players);
        double avg = mapper.getAverageTransferCost();
        assertEquals(5_000.0, avg, 0.001);
    }

    /* ------------------- 5. Средняя стоимость при пустом списке ---- */
    @Test
    void testAverageTransferCostEmpty() {
        when(resolver.getPlayers()).thenReturn(Collections.emptyList());
        assertEquals(0.0, mapper.getAverageTransferCost(), 0.001);
    }
}
