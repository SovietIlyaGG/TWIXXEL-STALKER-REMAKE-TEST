package com.thestalker.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

public class StalkerPeekHelper {

    private static final int PEEK_RADIUS = 3;
    private static final int PEEK_COOLDOWN = 200;

    private int peekCooldown = 0;
    private boolean peeking = false;
    private BlockPos peekBlock = null;

    public void tick(StalkerEntity stalker, ServerLevel level) {
        if (peekCooldown > 0) {
            peekCooldown--;
            if (peekCooldown == 0) {
                peeking = false;
                peekBlock = null;
            }
            return;
        }

        StalkerKind kind = stalker.getStalkerKind();

        if (kind.canPeekFromTrees()) {
            tryPeekFromTree(stalker, level);
            return;
        }

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
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                peeking = true;
                peekBlock = p.immutable();
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

            peeking = true;
            peekBlock = p.immutable();
            peekCooldown = PEEK_COOLDOWN;
            return;
        }
    }

    public boolean isPeeking() {
        return peeking;
    }

    public BlockPos getPeekBlock() {
        return peekBlock;
    }

    public void reset() {
        peeking = false;
        peekBlock = null;
        peekCooldown = 0;
    }
}
