package com.thestalker.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class StalkerBarricadeBreaker {

    public static final int SCAN_X = 2;
    public static final int SCAN_Y = 1;
    public static final int SCAN_Z = 2;

    private static final int BREAK_COOLDOWN = 10;

    private int cooldown = 0;
    private BlockPos targetDoor = null;
    private boolean doorBroken = false;

    public void tick(StalkerEntity stalker) {
        if (!(stalker.level() instanceof ServerLevel level)) return;
        if (targetDoor == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        BlockPos barricade = findBarricade(level, targetDoor);
        if (barricade != null) {
            level.destroyBlock(barricade, true, stalker);
            level.playSound(null, barricade,
                    com.thestalker.init.ModSounds.BREAK_EVENT_2.get(),
                    SoundSource.HOSTILE, 1.0f, 0.9f);
            cooldown = BREAK_COOLDOWN;
            return;
        }

        if (!doorBroken) {
            BlockState doorState = level.getBlockState(targetDoor);
            if (doorState.getBlock() instanceof DoorBlock) {
                level.playSound(null, targetDoor,
                        com.thestalker.init.ModSounds.BREAK_EVENT_4.get(),
                        SoundSource.HOSTILE, 1.0f, 0.8f);
                level.destroyBlock(targetDoor, true, stalker);
                level.playSound(null, targetDoor,
                        com.thestalker.init.ModSounds.BREAK_EVENT_3.get(),
                        SoundSource.HOSTILE, 1.0f, 0.9f);
                doorBroken = true;
                cooldown = BREAK_COOLDOWN;
            }
        }
    }

    private BlockPos findBarricade(ServerLevel level, BlockPos door) {
        for (BlockPos p : BlockPos.betweenClosed(
                door.offset(-SCAN_X, -SCAN_Y, -SCAN_Z),
                door.offset( SCAN_X,  SCAN_Y,  SCAN_Z))) {

            BlockState state = level.getBlockState(p);
            if (state.isAir()) continue;
            if (state.is(Blocks.BEDROCK)) continue;
            if (state.getBlock() instanceof DoorBlock) continue;

            return p.immutable();
        }
        return null;
    }

    public void setTargetDoor(BlockPos pos) {
        this.targetDoor = pos;
        this.doorBroken = false;
        this.cooldown = 0;
    }

    public void clearTarget() {
        this.targetDoor = null;
        this.doorBroken = false;
        this.cooldown = 0;
    }

    public boolean hasTarget() {
        return targetDoor != null;
    }

    public boolean isDoorBroken() {
        return doorBroken;
    }
}