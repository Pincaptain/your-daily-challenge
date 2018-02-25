package com.ydc.akatosh.yourdailychallenge.Classes;

/**
 * {@author Borjan Gjorovski}
 * {@date 20-02-2018}
 * {@link Challenge}
 *
 * {@description Stores a local copy of the challenge attributes.}
 */

public class Challenge {

    private String challenge;
    private long points;
    private boolean finished = false;

    public Challenge() {}

    public Challenge(String challenge, long points) {
        this.challenge = challenge;
        this.points = points;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

}
