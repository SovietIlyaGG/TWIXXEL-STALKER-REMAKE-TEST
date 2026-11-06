package com.thestalker.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

public class StalkerPeekHelper {

    private static final int PEEK_RADIUS = 3;
    private static final int PEEK_COOLDOWN = 200;

    private int peekCooldown = 0;

    public void tick(StalkerEntity stalker, ServerLevel level) {
        // Если таймер выглядывания еще идет
        if (peekCooldown > 0) {
            peekCooldown--;
            if (peekCooldown == 0) {
                // Время вышло, выключаем анимацию
                stalker.setPeeking(false);
            }
            return;
        }

        StalkerKind kind = stalker.getStalkerKind();

        // Если это Bipedal или Tormentor (умеют выглядывать из деревьев/дверей)
        if (kind.canPeekFromTrees()) {
            tryPeekFromTree(stalker, level);
            return;
        }

        // Если это Quadrupedal (умеет выглядывать из дырок)
        if (kind.canPeekFromHoles()) {
            tryPeekFromHole(stalker, level);
        }
    }

    private void tryPeekFromTree(StalkerEntity stalker, ServerLevel level) {
        BlockPos center = stalker.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-PEEK_RADIUS, -2, -PEEK_RADIUS),
                center.offset( PEEK_RADIUS,  2,  PEEK_RADIUS))) {

            BlockState state = level.getBlockState(p);
            // Ищем бревна или листву (деревья)
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                
                // ВКЛЮЧАЕМ АНИМАЦИЮ
                stalker.setPeeking(true);
                
                // Телепортируем его за дерево (чтобы он реально прятался)
                stalker.teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                
                peekCooldown = PEEK_COOLDOWN;
                return;
            }
        }
    }

    private void tryPeekFromHole(StalkerEntity stalker, ServerLevel level) {
        BlockPos center = stalker.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-PEEK_RADIUS, -1, -PEEK_RADIUS),
                center.offset( PEEK_RADIUS,  1,  PEEK_RADIUS))) {

            if (!level.getBlockState(p).isAir()) continue;
            if (!level.getBlockState(p.above()).isAir()) continue;
            if (level.getBlockState(p.below()).isAir()) continue;

            // ВКЛЮЧАЕМ АНИМАЦИЮ ДЛЯ QUADRUPEDAL
            stalker.setPeeking(true);
            peekCooldown = PEEK_COOLDOWN;
            return;
        }
    }
}
