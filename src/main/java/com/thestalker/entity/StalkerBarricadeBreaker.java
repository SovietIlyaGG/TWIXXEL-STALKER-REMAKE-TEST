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
    private BlockPos targetBlock = null;
    private boolean blockBroken = false;

    public void tick(StalkerEntity stalker) {
        if (!(stalker.level() instanceof ServerLevel level)) return;
        if (targetBlock == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        BlockState state = level.getBlockState(targetBlock);

        // 1. Если это дверь — ломаем её
        if (state.getBlock() instanceof DoorBlock) {
            if (!blockBroken) {
                // Включаем анимацию выглядывания перед тем, как сломать
                stalker.setPeeking(true);
                
                level.playSound(null, targetBlock, ModSounds.BREAK_EVENT_4.get(), SoundSource.HOSTILE, 1.0f, 0.8f);
                level.destroyBlock(targetBlock, true, stalker);
                level.playSound(null, targetBlock, ModSounds.BREAK_EVENT_3.get(), SoundSource.HOSTILE, 1.0f, 0.9f);
                
                blockBroken = true;
                cooldown = BREAK_COOLDOWN;
                
                // Через секунду выключаем выглядывание, чтобы он зашел
                stalker.setPeeking(false); 
            }
            return;
        }

        // 2. Если это блок перед дверью (баррикада) — ломаем его
        if (!state.isAir() && !(state.getBlock() instanceof DoorBlock)) {
            if (!blockBroken) {
                // Тоже включаем анимацию, как будто он выглядывает из-за блока
                stalker.setPeeking(true);
                
                level.destroyBlock(targetBlock, true, stalker);
                level.playSound(null, targetBlock, ModSounds.BREAK_EVENT_2.get(), SoundSource.HOSTILE, 1.0f, 0.9f);
                
                blockBroken = true;
                cooldown = BREAK_COOLDOWN;
                
                stalker.setPeeking(false);
            }
        }
    }

    public void setTargetBlock(BlockPos pos) {
        this.targetBlock = pos;
        this.blockBroken = false;
        this.cooldown = 0;
    }

    public void clearTarget() {
        this.targetBlock = null;
        this.blockBroken = false;
        this.cooldown = 0;
    }
}
