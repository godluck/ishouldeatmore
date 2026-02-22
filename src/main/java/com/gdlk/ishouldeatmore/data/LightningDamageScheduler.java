package com.gdlk.ishouldeatmore.data;

import com.gdlk.ishouldeatmore.network.FoodDataSync;
import com.gdlk.ishouldeatmore.network.FoodLevelStagePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Schedules and applies lightning damage when a player advances food level stage.
 */
public final class LightningDamageScheduler {

    /** Delay in server ticks before applying lightning damage (20 ≈ 1 second). */
    public static final int LIGHTNING_DAMAGE_DELAY_TICKS = 20;

    private final List<PendingLightningDamage> pending = new ArrayList<>();

    /**
     * Schedules lightning damage for the player when they have reached the next stage threshold.
     * Does nothing if this player already has pending damage.
     */
    public void scheduleForStageAdvance(ServerPlayer player, int currentStage, int newStage) {
        float hurtDamage = (float) Math.pow(10, currentStage) * 2;
        UUID playerId = player.getUUID();
        synchronized (pending) {
            if (pending.stream().noneMatch(p -> p.playerId().equals(playerId))) {
                pending.add(new PendingLightningDamage(
                        playerId, hurtDamage, currentStage, newStage, LIGHTNING_DAMAGE_DELAY_TICKS));
            }
        }
    }

    /**
     * Called every server tick to process pending lightning damage.
     */
    public void tick(MinecraftServer server) {
        synchronized (pending) {
            Iterator<PendingLightningDamage> it = pending.iterator();
            while (it.hasNext()) {
                PendingLightningDamage p = it.next();
                p.decrementTicksRemaining();
                if (p.ticksRemaining() > 0) {
                    continue;
                }
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(p.playerId());
                if (serverPlayer == null || !serverPlayer.isAlive()) {
                    it.remove();
                    continue;
                }
                if (applyLightning(serverPlayer, p.damagePerHit())) {
                    p.decrementNumHits();
                    p.resetTicksRemaining();
                    if (serverPlayer.getFoodData() instanceof FoodDataSync sync && p.numHits() == 0) {
                        it.remove();
                        sync.ishouldeatmore$setFoodLevelStage(p.newStage());
                        PacketDistributor.sendToPlayer(serverPlayer,
                                new FoodLevelStagePayload(sync.ishouldeatmore$getFoodLevelStage()));
                    }
                }
            }
        }
    }

    /**
     * Applies one lightning hit to the player and spawns a visual-only lightning bolt.
     *
     * @return true if the player was hurt, false if they resisted
     */
    public static boolean applyLightning(ServerPlayer player, float damage) {
        var damageSource = player.level().damageSources().source(DamageTypes.LIGHTNING_BOLT);
        if (player.hurt(damageSource, damage)) {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
            bolt.setPos(player.getX(), player.getY(), player.getZ());
            bolt.setVisualOnly(true);
            player.level().addFreshEntity(bolt);
            return true;
        }
        return false;
    }
}
