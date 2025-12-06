package org.example.championship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataMapperTest {

    private IResolver resolverMock;
    private DataMapper mapper;

    @BeforeEach
    void setUp() {
        resolverMock = mock(IResolver.class);
        mapper = new DataMapper(resolverMock);
    }

    @Test
    void testGetTop10TeamsByTransferCost() {
        List<Player> players = List.of(
                new Player("A","TeamX","C","FORWARD","BRA","Agt1",1_000_000L,10,5,2,1,0),
                new Player("B","TeamX","C","DEFENDER","GER","Agt2",500_000L,8,3,1,0,1),
                new Player("C","TeamY","C","MIDFIELD","FRA","Agt1",2_000_000L,12,7,4,2,0)
        );
        when(resolverMock.getPlayers()).thenReturn(players);

        Map<String, Long> result = mapper.getTop10TeamsByTransferCost();
        assertEquals(2, result.size());
        assertEquals(1_500_000L, result.get("TeamX"));
        assertEquals(2_000_000L, result.get("TeamY"));
    }

    @Test
    void testGetTop10TeamsByTransferCostEmpty() {
        when(resolverMock.getPlayers()).thenReturn(Collections.emptyList());
        assertTrue(mapper.getTop10TeamsByTransferCost().isEmpty());
    }

    @Test
    void testGetTotalTeamsCount() {
        Set<String> teams = Set.of("T1", "T2", "T3");
        when(resolverMock.getTeams()).thenReturn(teams);
        assertEquals(3, mapper.getTotalTeamsCount());
    }

    @Test
    void testGetAverageTransferCost() {
        List<Player> players = List.of(
                new Player("A","T1","C","FORWARD","BRA","A1",1_000_000L,10,5,2,1,0),
                new Player("B","T2","C","DEFENDER","GER","A2",2_000_000L,8,3,1,0,1)
        );
        when(resolverMock.getPlayers()).thenReturn(players);
        double avg = mapper.getAverageTransferCost();
        assertEquals(1_500_000.0, avg, 0.0001);
    }

    @Test
    void testGetAverageTransferCostEmpty() {
        when(resolverMock.getPlayers()).thenReturn(Collections.emptyList());
        assertEquals(0.0, mapper.getAverageTransferCost(), 0.0001);
    }
}
