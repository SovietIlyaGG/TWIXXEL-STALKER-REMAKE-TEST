package com.thestalker.client;

import com.thestalker.StalkerConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StalkerConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Stalker Config"))
                .setSavingRunnable(StalkerConfigScreen::save);

        ConfigEntryBuilder entry = builder.entryBuilder();

        ConfigCategory stalk = builder.getOrCreateCategory(Component.literal("Stalk"));
        stalk.addEntry(entry.startIntField(Component.literal("Roll Interval (minutes)"),
                        StalkerConfig.STALK_ROLL_INTERVAL_MINUTES.get())
                .setDefaultValue(2).setMin(1).setMax(120)
                .setTooltip(Component.literal("Minutes between each spawn roll."))
                .setSaveConsumer(v -> StalkerConfig.STALK_ROLL_INTERVAL_MINUTES.set(v))
                .build());
        stalk.addEntry(entry.startDoubleField(Component.literal("Distant Stalk Distance (blocks)"),
                        StalkerConfig.DISTANT_STALK_DISTANCE.get())
                .setDefaultValue(30.0).setMin(1.0).setMax(200.0)
                .setTooltip(Component.literal("How far away distant stalk spawns."))
                .setSaveConsumer(v -> StalkerConfig.DISTANT_STALK_DISTANCE.set(v))
                .build());
        stalk.addEntry(entry.startDoubleField(Component.literal("Close Stalk Distance (blocks)"),
                        StalkerConfig.CLOSE_STALK_DISTANCE.get())
                .setDefaultValue(10.0).setMin(1.0).setMax(100.0)
                .setTooltip(Component.literal("How far away close stalk spawns."))
                .setSaveConsumer(v -> StalkerConfig.CLOSE_STALK_DISTANCE.set(v))
                .build());
        stalk.addEntry(entry.startDoubleField(Component.literal("Proximity Despawn Distance"),
                        StalkerConfig.STALK_PROXIMITY_DESPAWN.get())
                .setDefaultValue(3.0).setMin(1.0).setMax(20.0)
                .setTooltip(Component.literal("Distance at which stalker despawns and applies Darkness."))
                .setSaveConsumer(v -> StalkerConfig.STALK_PROXIMITY_DESPAWN.set(v))
                .build());

        ConfigCategory behindScare = builder.getOrCreateCategory(Component.literal("Behind Scare"));
        behindScare.addEntry(entry.startIntField(Component.literal("Cooldown (minutes)"),
                        StalkerConfig.BEHIND_SCARE_COOLDOWN_MINUTES.get())
                .setDefaultValue(5).setMin(1).setMax(300)
                .setTooltip(Component.literal("Minutes between behind scares."))
                .setSaveConsumer(v -> StalkerConfig.BEHIND_SCARE_COOLDOWN_MINUTES.set(v))
                .build());

        ConfigCategory chase = builder.getOrCreateCategory(Component.literal("Chase"));
        chase.addEntry(entry.startIntField(Component.literal("Cooldown (minutes)"),
                        StalkerConfig.CHASE_COOLDOWN_MINUTES.get())
                .setDefaultValue(10).setMin(1).setMax(300)
                .setSaveConsumer(v -> StalkerConfig.CHASE_COOLDOWN_MINUTES.set(v))
                .build());
        chase.addEntry(entry.startIntField(Component.literal("Duration (seconds)"),
                        StalkerConfig.CHASE_DURATION_SECONDS.get())
                .setDefaultValue(60).setMin(5).setMax(600)
                .setSaveConsumer(v -> StalkerConfig.CHASE_DURATION_SECONDS.set(v))
                .build());
        chase.addEntry(entry.startDoubleField(Component.literal("Spawn Distance (blocks)"),
                        StalkerConfig.CHASE_SPAWN_DISTANCE.get())
                .setDefaultValue(20.0).setMin(5.0).setMax(100.0)
                .setSaveConsumer(v -> StalkerConfig.CHASE_SPAWN_DISTANCE.set(v))
                .build());
        chase.addEntry(entry.startDoubleField(Component.literal("Teleport Step (blocks)"),
                        StalkerConfig.CHASE_TELEPORT_STEP.get())
                .setDefaultValue(10.0).setMin(1.0).setMax(50.0)
                .setSaveConsumer(v -> StalkerConfig.CHASE_TELEPORT_STEP.set(v))
                .build());
        chase.addEntry(entry.startIntField(Component.literal("Teleport Interval (seconds)"),
                        StalkerConfig.CHASE_TELEPORT_INTERVAL_SECONDS.get())
                .setDefaultValue(2).setMin(1).setMax(30)
                .setSaveConsumer(v -> StalkerConfig.CHASE_TELEPORT_INTERVAL_SECONDS.set(v))
                .build());
        chase.addEntry(entry.startDoubleField(Component.literal("Attack Range (blocks)"),
                        StalkerConfig.CHASE_ATTACK_RANGE.get())
                .setDefaultValue(2.0).setMin(0.5).setMax(10.0)
                .setSaveConsumer(v -> StalkerConfig.CHASE_ATTACK_RANGE.set(v))
                .build());
        chase.addEntry(entry.startDoubleField(Component.literal("Damage"),
                        StalkerConfig.CHASE_DAMAGE.get())
                .setDefaultValue(10.0).setMin(0.0).setMax(100.0)
                .setSaveConsumer(v -> StalkerConfig.CHASE_DAMAGE.set(v))
                .build());
        chase.addEntry(entry.startDoubleField(Component.literal("FOV Threshold (degrees)"),
                        StalkerConfig.CHASE_FOV_THRESHOLD.get())
                .setDefaultValue(120.0).setMin(10.0).setMax(180.0)
                .setSaveConsumer(v -> StalkerConfig.CHASE_FOV_THRESHOLD.set(v))
                .build());

        ConfigCategory breakEvents = builder.getOrCreateCategory(Component.literal("Break Events"));
        breakEvents.addEntry(entry.startIntField(Component.literal("Interval (minutes)"),
                        StalkerConfig.BREAK_EVENT_INTERVAL_MINUTES.get())
                .setDefaultValue(3).setMin(1).setMax(120)
                .setSaveConsumer(v -> StalkerConfig.BREAK_EVENT_INTERVAL_MINUTES.set(v))
                .build());

        return builder.build();
    }

    private static void save() {
        StalkerConfig.SPEC.save();
    }
}
