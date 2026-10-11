package com.thestalker.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class StalkerDashHelper {

    private static final double FREEZE_FOV_DOT = 0.5;
    private static final int FREEZE_DURATION = 20;
    private static final int DASH_COOLDOWN = 40;
    private static final double DASH_STEP = 2.0;
    private static final double DASH_MAX_DISTANCE = 32.0;

    private int freezeTimer = 0;
    private int dashCooldown = 0;

    public void tick(StalkerEntity stalker, LivingEntity target) {
        if (target == null) return;
        if (!stalker.getStalkerKind().canDash()) return;

        if (dashCooldown > 0) {
            dashCooldown--;
        }

        if (isBeingLookedAt(stalker, target)) {
            freezeTimer = FREEZE_DURATION;
        } else if (freezeTimer > 0) {
            freezeTimer--;
        }

        if (freezeTimer > 0) {
            stalker.getNavigation().stop();
            return;
        }

        if (dashCooldown == 0) {
            double dist = stalker.distanceTo(target);
            if (dist > DASH_STEP && dist < DASH_MAX_DISTANCE) {
                tryDash(stalker, target);
            }
        }
    }

    private void tryDash(StalkerEntity stalker, LivingEntity target) {
        Vec3 from = stalker.position();
        Vec3 to = target.position();
        Vec3 dir = to.subtract(from).normalize().scale(DASH_STEP);
        Vec3 dest = from.add(dir);

        if (!stalker.level().noCollision(stalker, stalker.getBoundingBox().move(dir))) {
            dest = dest.subtract(0, 0.5, 0);
        }

        stalker.teleportTo(dest.x, dest.y, dest.z);
        stalker.level().playSound(null, stalker.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0f, 1.2f);
        dashCooldown = DASH_COOLDOWN;
    }

    private boolean isBeingLookedAt(StalkerEntity stalker, LivingEntity target) {
        Vec3 toStalker = stalker.position().subtract(target.getEyePosition()).normalize();
        Vec3 look = target.getViewVector(1.0f);
        return look.dot(toStalker) > FREEZE_FOV_DOT;
    }

    public boolean isFrozen() {
        return freezeTimer > 0;
    }

    public void reset() {
        freezeTimer = 0;
        dashCooldown = 0;
    }
}