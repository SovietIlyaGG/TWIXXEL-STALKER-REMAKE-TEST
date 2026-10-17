package com.thestalker.entity;

import com.thestalker.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class StalkerBarricadeBreaker {

    private static final int BREAK_COOLDOWN = 10;

    private int cooldown = 0;
    private BlockPos targetDoor = null;
    private boolean doorBroken = false;

    public void tick(StalkerEntity stalker) {
        if (!(stalker.level() instanceof ServerLevel level)) return;
        if (targetDoor == null) return;

        if (cooldown > 0) { cooldown--; return; }

        BlockState doorState = level.getBlockState(targetDoor);
        if (!(doorState.getBlock() instanceof DoorBlock)) { clearTarget(); return; }

        Direction stalkerDir = stalker.getDirection();
        BlockPos blockInFront = targetDoor.relative(stalkerDir);
        BlockState frontState = level.getBlockState(blockInFront);

        if (!frontState.isAir() && !(frontState.getBlock() instanceof DoorBlock)) {
            level.destroyBlock(blockInFront, true, stalker);
            level.playSound(null, blockInFront, ModSounds.BREAK_EVENT_2.get(), SoundSource.HOSTILE, 1.0f, 0.9f);
            cooldown = BREAK_COOLDOWN;
            return;
        }

        if (!doorBroken) {
            level.playSound(null, targetDoor, ModSounds.BREAK_EVENT_4.get(), SoundSource.HOSTILE, 1.0f, 0.8f);
            level.destroyBlock(targetDoor, true, stalker);
            level.playSound(null, targetDoor, ModSounds.BREAK_EVENT_3.get(), SoundSource.HOSTILE, 1.0f, 0.9f);
            doorBroken = true;
            cooldown = BREAK_COOLDOWN;
        }
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
}
