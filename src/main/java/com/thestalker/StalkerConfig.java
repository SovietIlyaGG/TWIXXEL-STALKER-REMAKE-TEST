package com.thestalker;

import net.minecraftforge.common.ForgeConfigSpec;

public class StalkerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue STALK_ROLL_INTERVAL_MINUTES;
    public static final ForgeConfigSpec.DoubleValue DISTANT_STALK_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue CLOSE_STALK_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue STALK_PROXIMITY_DESPAWN;
    public static final ForgeConfigSpec.IntValue BEHIND_SCARE_COOLDOWN_MINUTES;
    public static final ForgeConfigSpec.IntValue CHASE_COOLDOWN_MINUTES;
    public static final ForgeConfigSpec.IntValue CHASE_DURATION_SECONDS;
    public static final ForgeConfigSpec.DoubleValue CHASE_SPAWN_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue CHASE_TELEPORT_STEP;
    public static final ForgeConfigSpec.IntValue CHASE_TELEPORT_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.DoubleValue CHASE_ATTACK_RANGE;
    public static final ForgeConfigSpec.DoubleValue CHASE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue CHASE_FOV_THRESHOLD;
    public static final ForgeConfigSpec.IntValue BREAK_EVENT_INTERVAL_MINUTES;

    // === НОВЫЕ ПАРАМЕТРЫ РЕМЕЙКА ===
    public static final ForgeConfigSpec.DoubleValue STALKER_FREEZE_FOV_DEGREES;
    public static final ForgeConfigSpec.DoubleValue STALKER_DASH_STEP;
    public static final ForgeConfigSpec.IntValue STALKER_DASH_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue STALKER_PEEK_ENABLED;
    public static final ForgeConfigSpec.IntValue STALKER_BARRICADE_SCAN_RADIUS;
    public static final ForgeConfigSpec.IntValue STALKER_BARRICADE_BREAK_COOLDOWN;

    public static long stalkRollIntervalTicks() {
        return (long) STALK_ROLL_INTERVAL_MINUTES.get().intValue() * 60L * 20L;
    }

    public static long behindScareCooldwonTicks() {
        return (long) BEHIND_SCARE_COOLDOWN_MINUTES.get().intValue() * 60L * 20L;
    }

    public static long chaseCooldownTicks() {
        return (long) CHASE_COOLDOWN_MINUTES.get().intValue() * 60L * 20L;
    }

    public static long chaseDurationTicks() {
        return (long) CHASE_DURATION_SECONDS.get().intValue() * 20L;
    }

    public static long chaseIntervalTicks() {
        return (long) CHASE_TELEPORT_INTERVAL_SECONDS.get().intValue() * 20L;
    }

    public static long breakEventIntervalTicks() {
        return (long) BREAK_EVENT_INTERVAL_MINUTES.get().intValue() * 60L * 20L;
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // === STALK ===
        builder.comment("Stalk Settings").push("stalk");
        STALK_ROLL_INTERVAL_MINUTES = builder
                .comment("Minutes between each spawn roll after the grace period.")
                .defineInRange("roll_interval_minutes", 2, 1, 120);
        DISTANT_STALK_DISTANCE = builder
                .comment("How far away (blocks) distant stalk spawns.")
                .defineInRange("distant_stalk_distance", 30.0, 1.0, 200.0);
        CLOSE_STALK_DISTANCE = builder
                .comment("How far away (blocks) close stalk spawns.")
                .defineInRange("close_stalk_distance", 10.0, 1.0, 100.0);
        STALK_PROXIMITY_DESPAWN = builder
                .comment("Distance in blocks at which the stalker despawns during a stalk and applies Darkness.")
                .defineInRange("proximity_despawn_distance", 3.0, 1.0, 20.0);
        builder.pop();

        // === BEHIND SCARE ===
        builder.comment("Behind Scare Settings").push("behind_scare");
        BEHIND_SCARE_COOLDOWN_MINUTES = builder
                .comment("Minutes between behind scares.")
                .defineInRange("cooldown_minutes", 5, 1, 300);
        builder.pop();

        // === CHASE ===
        builder.comment("Chase Settings").push("chase");
        CHASE_COOLDOWN_MINUTES = builder
                .comment("Minutes between chases.")
                .defineInRange("cooldown_minutes", 10, 1, 300);
        CHASE_DURATION_SECONDS = builder
                .comment("How many seconds the chase lasts before the entity despawns.")
                .defineInRange("duration_seconds", 60, 5, 600);
        CHASE_SPAWN_DISTANCE = builder
                .comment("How far away (blocks) the chase entity spawns.")
                .defineInRange("spawn_distance", 20.0, 5.0, 100.0);
        CHASE_TELEPORT_STEP = builder
                .comment("How many blocks closer the entity teleports each interval when out of sight.")
                .defineInRange("teleport_step_blocks", 10.0, 1.0, 50.0);
        CHASE_TELEPORT_INTERVAL_SECONDS = builder
                .comment("How many seconds between each teleport attempt during a chase.")
                .defineInRange("teleport_interval_seconds", 2, 1, 30);
        CHASE_ATTACK_RANGE = builder
                .comment("Distance in blocks at which the chase entity deals damage.")
                .defineInRange("attack_range_blocks", 2.0, 0.5, 10.0);
        CHASE_DAMAGE = builder
                .comment("How much damage the chase entity deals per hit.")
                .defineInRange("damage", 10.0, 0.0, 100.0);
        CHASE_FOV_THRESHOLD = builder
                .comment("Angle in degrees within which the entity counts as visible to the player.")
                .defineInRange("fov_threshold_degrees", 120.0, 10.0, 180.0);
        builder.pop();

        // === BREAK EVENTS ===
        builder.comment("Break Event Sounds").push("break_events");
        BREAK_EVENT_INTERVAL_MINUTES = builder
                .comment("Minutes between random break event sounds. Active from the moment you join (ignores grace period).")
                .defineInRange("interval_minutes", 3, 1, 120);
        builder.pop();

        // === REMAKE (новые механики) ===
        builder.comment("Remake Settings — new mechanics from TWIXXEL STALKER REMAKE").push("remake");
        STALKER_FREEZE_FOV_DEGREES = builder
                .comment("FOV angle in degrees to freeze stalker when player looks at it.")
                .defineInRange("freeze_fov_degrees", 60.0, 10.0, 180.0);
        STALKER_DASH_STEP = builder
                .comment("How many blocks stalker dashes when player looks away.")
                .defineInRange("dash_step_blocks", 2.0, 0.5, 10.0);
        STALKER_DASH_COOLDOWN = builder
                .comment("Cooldown in ticks between dashes (20 ticks = 1 second).")
                .defineInRange("dash_cooldown_ticks", 40, 5, 200);
        STALKER_PEEK_ENABLED = builder
                .comment("Allow stalkers to peek from trees, holes and floor.")
                .define("peek_enabled", true);
        STALKER_BARRICADE_SCAN_RADIUS = builder
                .comment("Radius around door to scan for barricades (2 = 5x3x5 area).")
                .defineInRange("barricade_scan_radius", 2, 1, 5);
        STALKER_BARRICADE_BREAK_COOLDOWN = builder
                .comment("Cooldown in ticks between breaking each barricade block (10 = 0.5 sec).")
                .defineInRange("barricade_break_cooldown_ticks", 10, 2, 100);
        builder.pop();

        SPEC = builder.build();
    }
}