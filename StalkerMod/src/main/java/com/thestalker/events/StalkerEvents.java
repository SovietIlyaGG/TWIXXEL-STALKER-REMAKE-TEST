package com.thestalker.events;

import com.thestalker.StalkerConfig;
import com.thestalker.entity.StalkerEntity;
import com.thestalker.init.ModEntities;
import com.thestalker.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

public class StalkerEvents {
    private long serverStartTick = -1L;
    private long lastStalkTick = Long.MIN_VALUE;
    private UUID spawnedStalkUUID = null;
    private boolean stalkActive = false;
    private long lastBehindScareTick = Long.MIN_VALUE;
    private long lastChaseTick = Long.MIN_VALUE;
    private UUID chaseEntityUUID = null;
    private long lastBreakEventTick = Long.MIN_VALUE;

    private boolean isBadWeather(ServerLevel level) {
        return level.isThundering() || level.isRaining();
    }

    private long effectiveCooldown(long base, ServerLevel level) {
        return isBadWeather(level) ? base / 2L : base;
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        long now = event.getServer().overworld().getGameTime();
        this.serverStartTick = now;
        this.lastStalkTick = now;
        this.lastBehindScareTick = now;
        this.lastChaseTick = now;
        this.lastBreakEventTick = now;
        this.spawnedStalkUUID = null;
        this.chaseEntityUUID = null;
        this.stalkActive = false;
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        this.serverStartTick = -1L;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.overworld();
        long now = overworld.getGameTime();
        if (this.serverStartTick < 0L) return;

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        if (now - this.lastBreakEventTick >= StalkerConfig.breakEventIntervalTicks()) {
            playBreakEventToAll(players, overworld);
            this.lastBreakEventTick = now;
        }

        if (now - this.lastBehindScareTick >=
                effectiveCooldown(StalkerConfig.behindScareCooldwonTicks(), overworld)) {
            ServerPlayer target = randomPlayer(players, server);
            if (target != null) triggerBehindScare(target, getPlayerLevel(target, server));
            this.lastBehindScareTick = now;
        }

        if (this.chaseEntityUUID != null) {
            if (!StalkerEntity.ALIVE_UUIDS.contains(this.chaseEntityUUID)) {
                this.chaseEntityUUID = null;
                this.lastChaseTick = now;
            }
        } else if (now - this.lastChaseTick >=
                effectiveCooldown(StalkerConfig.chaseCooldownTicks(), overworld)) {
            ServerPlayer target = randomPlayer(players, server);
            if (target != null) {
                UUID uuid = triggerChase(target, getPlayerLevel(target, server));
                if (uuid != null) this.chaseEntityUUID = uuid;
            }
            this.lastChaseTick = now;
        }

        if (this.stalkActive && this.spawnedStalkUUID != null) {
            if (!StalkerEntity.ALIVE_UUIDS.contains(this.spawnedStalkUUID)) {
                this.spawnedStalkUUID = null;
                this.stalkActive = false;
                this.lastStalkTick = now;
            }
        } else if (this.stalkActive) {
            this.stalkActive = false;
            this.lastStalkTick = now;
        } else if (now - this.lastStalkTick >=
                effectiveCooldown(StalkerConfig.stalkRollIntervalTicks(), overworld)) {
            if (overworld.random.nextBoolean()) {
                ServerPlayer target = randomPlayer(players, server);
                if (target != null) {
                    double distance = overworld.random.nextBoolean()
                            ? StalkerConfig.DISTANT_STALK_DISTANCE.get()
                            : StalkerConfig.CLOSE_STALK_DISTANCE.get();
                    UUID uuid = spawnStalk(target, getPlayerLevel(target, server), distance);
                    if (uuid != null) {
                        this.spawnedStalkUUID = uuid;
                        this.stalkActive = true;
                    }
                }
            }
            this.lastStalkTick = now;
        }
    }

    public void triggerDistantStalk(ServerPlayer player, ServerLevel level) {
        spawnStalk(player, level, StalkerConfig.DISTANT_STALK_DISTANCE.get());
    }

    public void triggerCloseStalk(ServerPlayer player, ServerLevel level) {
        spawnStalk(player, level, StalkerConfig.CLOSE_STALK_DISTANCE.get());
    }

    public void triggerBehindScare(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getViewVector(1.0f);
        double spawnX = player.getX() - look.x;
        double spawnZ = player.getZ() - look.z;
        StalkerEntity stalker = ModEntities.STALKER.get().create(level);
        if (stalker == null) return;
        stalker.setupBehindScare();
        stalker.moveTo(spawnX, player.getY(), spawnZ, level.random.nextFloat() * 360.0f, 0.0f);
        stalker.finalizeSpawn(level,
                level.getCurrentDifficultyAt(new BlockPos((int) spawnX, (int) player.getY(), (int) spawnZ)),
                MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(stalker);
    }

    public UUID triggerChase(ServerPlayer player, ServerLevel level) {
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double dist = StalkerConfig.CHASE_SPAWN_DISTANCE.get();
        double spawnX = player.getX() + Math.cos(angle) * dist;
        double spawnZ = player.getZ() + Math.sin(angle) * dist;
        BlockPos pos = findGround(level, spawnX, player.getY(), spawnZ);
        if (pos == null) return null;
        StalkerEntity stalker = ModEntities.STALKER.get().create(level);
        if (stalker == null) return null;
        stalker.setupChase(player);
        stalker.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                level.random.nextFloat() * 360.0f, 0.0f);
        stalker.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(stalker);
        return stalker.getUUID();
    }

    public void triggerBreakEvent(ServerPlayer player, ServerLevel level) {
        SoundEvent[] options = {
                ModSounds.BREAK_EVENT_1.get(), ModSounds.BREAK_EVENT_2.get(),
                ModSounds.BREAK_EVENT_3.get(), ModSounds.BREAK_EVENT_4.get()
        };
        SoundEvent chosen = options[level.random.nextInt(options.length)];
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                chosen, SoundSource.AMBIENT, 1.0f, 1.0f);
    }

    private ServerPlayer randomPlayer(List<ServerPlayer> players, MinecraftServer server) {
        if (players.isEmpty()) return null;
        return players.get(server.overworld().random.nextInt(players.size()));
    }

    private ServerLevel getPlayerLevel(ServerPlayer player, MinecraftServer server) {
        Level level = player.level();
        return level instanceof ServerLevel sl ? sl : server.overworld();
    }

    private void playBreakEventToAll(List<ServerPlayer> players, ServerLevel overworld) {
        SoundEvent[] options = {
                ModSounds.BREAK_EVENT_1.get(), ModSounds.BREAK_EVENT_2.get(),
                ModSounds.BREAK_EVENT_3.get(), ModSounds.BREAK_EVENT_4.get()
        };
        SoundEvent chosen = options[overworld.random.nextInt(options.length)];
        for (ServerPlayer player : players) {
            Level level = player.level();
            ServerLevel sl = level instanceof ServerLevel s ? s : overworld;
            sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                    chosen, SoundSource.AMBIENT, 1.0f, 1.0f);
        }
    }

    private UUID spawnStalk(ServerPlayer player, ServerLevel level, double distance) {
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double spawnX = player.getX() + Math.cos(angle) * distance;
        double spawnZ = player.getZ() + Math.sin(angle) * distance;
        level.getChunkSource().getChunk((int) spawnX >> 4, (int) spawnZ >> 4, true);
        BlockPos pos = findGround(level, spawnX, player.getY(), spawnZ);
        double spawnY = pos != null ? pos.getY() : player.getY();
        double finalX = pos != null ? pos.getX() + 0.5 : spawnX;
        double finalZ = pos != null ? pos.getZ() + 0.5 : spawnZ;
        StalkerEntity stalker = ModEntities.STALKER.get().create(level);
        if (stalker == null) return null;
        stalker.setupStalk(level.random.nextInt(6) + 1);
        stalker.moveTo(finalX, spawnY, finalZ, level.random.nextFloat() * 360.0f, 0.0f);
        stalker.finalizeSpawn(level,
                level.getCurrentDifficultyAt(new BlockPos((int) finalX, (int) spawnY, (int) finalZ)),
                MobSpawnType.NATURAL, null, null);
        level.addFreshEntity(stalker);
        return stalker.getUUID();
    }

    private BlockPos findGround(ServerLevel level, double x, double startY, double z) {
        BlockPos base = new BlockPos((int) x, (int) startY, (int) z);
        for (int dy = 0; dy <= 16; dy++) {
            BlockPos pos = base.above(dy);
            if (!level.isEmptyBlock(pos.below())
                    && level.isEmptyBlock(pos)
                    && level.isEmptyBlock(pos.above())) return pos;
        }
        for (int dy = 1; dy <= 16; dy++) {
            BlockPos pos = base.below(dy);
            if (!level.isEmptyBlock(pos.below())
                    && level.isEmptyBlock(pos)
                    && level.isEmptyBlock(pos.above())) return pos;
        }
        return null;
    }
}