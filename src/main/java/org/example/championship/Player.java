package org.example.championship;

import java.util.Objects;

/**
 * Класс‑модель игрока.
 */
public class Player {
    private String name;
    private String team;
    private String city;
    private String position;
    private String nationality;
    private String agency;                // может быть null
    private long transferCost;
    private int participations;
    private int goals;
    private int assists;
    private int yellowCards;
    private int redCards;

    public Player(String name, String team, String city, String position,
                  String nationality, String agency, long transferCost,
                  int participations, int goals, int assists,
                  int yellowCards, int redCards) {
        this.name = name;
        this.team = team;
        this.city = city;
        this.position = position;
        this.nationality = nationality;
        this.agency = agency;
        this.transferCost = transferCost;
        this.participations = participations;
        this.goals = goals;
        this.assists = assists;
        this.yellowCards = yellowCards;
        this.redCards = redCards;
    }

    // ---------- Getters ----------
    public String getName() { return name; }
    public String getTeam() { return team; }
    public String getCity() { return city; }
    public String getPosition() { return position; }
    public String getNationality() { return nationality; }
    public String getAgency() { return agency; }
    public long getTransferCost() { return transferCost; }
    public int getParticipations() { return participations; }
    public int getGoals() { return goals; }
    public int getAssists() { return assists; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards() { return redCards; }

    // ---------- equals / hashCode / toString ----------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player p = (Player) o;
        return transferCost == p.transferCost &&
                participations == p.participations &&
                goals == p.goals &&
                assists == p.assists &&
                yellowCards == p.yellowCards &&
                redCards == p.redCards &&
                Objects.equals(name, p.name) &&
                Objects.equals(team, p.team) &&
                Objects.equals(city, p.city) &&
                Objects.equals(position, p.position) &&
                Objects.equals(nationality, p.nationality) &&
                Objects.equals(agency, p.agency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, team, city, position, nationality, agency,
                transferCost, participations, goals, assists, yellowCards, redCards);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", team='" + team + '\'' +
                ", position='" + position + '\'' +
                ", goals=" + goals +
                '}';
    }
}
