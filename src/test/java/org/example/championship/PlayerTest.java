package org.example.championship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testAllGettersAndToString() {
        Player p = new Player(
                "John Doe",
                "TeamA",
                "CityX",
                "FORWARD",
                "Germany",
                "SuperAgency",
                12_345L,
                10,
                7,
                3,
                2,
                1
        );

        assertEquals("John Doe",          p.getName());
        assertEquals("TeamA",            p.getTeam());
        assertEquals("CityX",            p.getCity());
        assertEquals("FORWARD",          p.getPosition());
        assertEquals("Germany",          p.getNationality());
        assertEquals("SuperAgency",      p.getAgency());
        assertEquals(12_345L,            p.getTransferCost());
        assertEquals(10,                 p.getParticipations());
        assertEquals(7,                  p.getGoals());
        assertEquals(3,                  p.getAssists());
        assertEquals(2,                  p.getYellowCards());
        assertEquals(1,                  p.getRedCards());

        // toString должен содержать имя, команду и количество голов
        String s = p.toString();
        assertTrue(s.contains("John Doe"));
        assertTrue(s.contains("TeamA"));
        assertTrue(s.contains("7"));
    }

    @Test
    void testEqualsAndHashCode() {
        Player p1 = new Player(
                "Jane", "TeamB", "C", "DEFENDER",
                "France", "AgencyX", 5_000L, 8, 2, 1, 0, 0);
        Player p2 = new Player(
                "Jane", "TeamB", "C", "DEFENDER",
                "France", "AgencyX", 5_000L, 8, 2, 1, 0, 0);
        Player p3 = new Player(
                "Jane", "TeamB", "C", "DEFENDER",
                "France", "AgencyX", 5_001L, 8, 2, 1, 0, 0);   // отличается transferCost

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        assertNotEquals(p1, p3);
        assertNotEquals(p1.hashCode(), p3.hashCode());
    }
}
