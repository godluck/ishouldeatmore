package com.gdlk.ishouldeatmore.data;

import java.util.UUID;

/**
 * Holds pending lightning damage to apply to a player after a delay (e.g. when advancing food stage).
 */
public final class PendingLightningDamage {
    private final UUID playerId;
    private final float damagePerHit;
    private int numHits;
    private final int newStage;
    private int ticksRemaining;
    private final int delayPerHit;

    public PendingLightningDamage(UUID playerId, float damagePerHit, int numHits, int newStage, int delayPerHit) {
        this.playerId = playerId;
        this.damagePerHit = damagePerHit;
        this.numHits = numHits;
        this.newStage = newStage;
        this.ticksRemaining = delayPerHit;
        this.delayPerHit = delayPerHit;
    }

    public UUID playerId() {
        return playerId;
    }

    public float damagePerHit() {
        return damagePerHit;
    }

    public int numHits() {
        return numHits;
    }

    public void decrementNumHits() {
        this.numHits--;
    }

    public int newStage() {
        return newStage;
    }

    public int ticksRemaining() {
        return ticksRemaining;
    }

    public void decrementTicksRemaining() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public void resetTicksRemaining() {
        this.ticksRemaining = delayPerHit;
    }

    public int delayPerHit() {
        return delayPerHit;
    }
}
