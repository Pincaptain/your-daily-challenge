package com.ydc.akatosh.yourdailychallenge.Classes;

/**
 * {@author Borjan Gjorovski}
 * {@date 18-02-2018}
 * {@link User}
 *
 * {@description Stores a local copy of the users attributes.}
 */

public class User {

    private String id;
    private int day;
    private long points;
    private Challenge challenge;

    public User() {
        this.id = "";
        this.day = -1;
        this.points = 0;
        this.challenge = new Challenge("Loading...", 0);
    }

    public User(String id, int day, long points, Challenge challenge) {
        this.id = id;
        this.day = day;
        this.points = points;
        this.challenge = challenge;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

}
