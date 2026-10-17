package com.thestalker.entity;

import com.thestalker.StalkerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap; // ТУТ ДОБАВЛЕН ИМПОРТ

public class StalkerEntity extends PathfinderMob {

    public static final Set<UUID> ALIVE_UUIDS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final int STALK_LIFETIME_TICKS = 1560;
    public static final int BEHIND_SCARE_TICKS = 100;

    private static final EntityDataAccessor<Byte> IMAGE_INDEX =
            SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_BEHIND_SCARE =
            SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHASE =
            SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private ServerPlayer chaseTarget = null;
    private int timer = 0;
    private int chaseTeleportTimer = 0;
    private int actionCooldown = 0;

    @Nullable
    private Vec3 lastKnownPlayerPos = null;
    private int memoryTimer = 0;

    private final StalkerDashHelper dashHelper = new StalkerDashHelper();
    private final StalkerBarricadeBreaker barricadeBreaker = new StalkerBarricadeBreaker();
    private final StalkerPeekHelper peekHelper = new StalkerPeekHelper();

    private static final String[] WHISPERS = {
            "я вижу тебя...", "не убегай...", "ты мой...", "иди сюда...", "я рядом...", "обернись..."
    };

    public StalkerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(false);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IMAGE_INDEX, (byte) 1);
        this.entityData.define(IS_BEHIND_SCARE, false);
        this.entityData.define(IS_CHASE, false);
    }

    public int getImageIndex() { return this.entityData.get(IMAGE_INDEX); }
    public boolean isBehindScare() { return this.entityData.get(IS_BEHIND_SCARE); }
    public boolean isChase() { return this.entityData.get(IS_CHASE); }
    public StalkerKind getStalkerKind() { return StalkerKind.fromImageIndex(getImageIndex()); }

    public void setupStalk(int imageIndex) {
        this.entityData.set(IMAGE_INDEX, (byte) imageIndex);
        this.entityData.set(IS_BEHIND_SCARE, false);
        this.entityData.set(IS_CHASE, false);
    }

    public void setupBehindScare() {
        this.entityData.set(IMAGE_INDEX, (byte) 0);
        this.entityData.set(IS_BEHIND_SCARE, true);
        this.entityData.set(IS_CHASE, false);
    }

    public void setupChase(ServerPlayer target) {
        int[] chaseIndices = { 4, 6 }; // Только Торментор
        this.entityData.set(IMAGE_INDEX, (byte) chaseIndices[this.random.nextInt(2)]);
        this.entityData.set(IS_BEHIND_SCARE, false);
        this.entityData.set(IS_CHASE, true);
        this.chaseTarget = target;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (isChase()) { tickChase(); return; }
        if (isBehindScare()) { tickBehindScare(); return; }
        tickStalk();
    }

    private void tickStalk() {
        Player nearby = this.level().getNearestPlayer(this, 32.0);

        Player close = this.level().getNearestPlayer(this, StalkerConfig.STALK_PROXIMITY_DESPAWN.get());
        if (close != null) {
            close.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            this.discard();
            return;
        }

        if (actionCooldown > 0) actionCooldown--;

        if (nearby != null) {
            updateMemory(nearby);
            if (lastKnownPlayerPos != null) {
                this.getNavigation().moveTo(lastKnownPlayerPos.x, lastKnownPlayerPos.y, lastKnownPlayerPos.z, 1.0D);
            } else {
                wanderAround();
            }

            if (this.level().isNight()) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
            } else {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D);
            }

            dashHelper.tick(this, nearby);
            if (this.level() instanceof ServerLevel sl) peekHelper.tick(this, sl);

            if (actionCooldown == 0 && this.distanceTo(nearby) < 15) {
                doCreepyActions(nearby);
                actionCooldown = 200;
            }
        }

        BlockPos doorPos = findNearbyDoor();
        if (doorPos != null) barricadeBreaker.setTargetDoor(doorPos);
        barricadeBreaker.tick(this);

        ++this.timer;
        if (this.timer >= STALK_LIFETIME_TICKS) this.discard();
    }

    private void updateMemory(Player player) {
        if (this.hasLineOfSight(player)) {
            lastKnownPlayerPos = player.position();
            memoryTimer = 200;
        } else if (memoryTimer > 0) {
            memoryTimer--;
            if (memoryTimer == 0) lastKnownPlayerPos = null;
        }
    }

    private void wanderAround() {
        if (this.getNavigation().isDone() && this.random.nextInt(100) == 0) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 10;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 10;
            this.getNavigation().moveTo(x, this.getY(), z, 1.0D);
        }
    }

    private void doCreepyActions(Player player) {
        int action = this.random.nextInt(3);
        if (action == 0) {
            for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-5, -2, -5), this.blockPosition().offset(5, 2, 5))) {
                BlockState state = this.level().getBlockState(p);
                if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.getBlock() instanceof CampfireBlock) {
                    this.level().removeBlock(p, false);
                    this.level().playSound(null, p, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }
        } else if (action == 1 && player instanceof ServerPlayer sp) {
            if (!sp.getInventory().isEmpty()) {
                int slot = this.random.nextInt(sp.getInventory().getContainerSize());
                ItemStack stack = sp.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    ItemStack stolen = stack.copyWithCount(1);
                    stack.shrink(1);
                    ItemEntity drop = new ItemEntity(this.level(), this.getX(), this.getY() + 1, this.getZ(), stolen);
                    this.level().addFreshEntity(drop);
                }
            }
        } else {
            String msg = WHISPERS[this.random.nextInt(WHISPERS.length)];
            player.displayClientMessage(Component.literal("§4§o" + msg), false);
            this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0f, 0.5f);
        }
    }

    private void tickBehindScare() {
        ++this.timer;
        if (this.timer >= BEHIND_SCARE_TICKS) this.discard();
    }

    private void tickChase() {
        if (this.chaseTarget == null || !this.chaseTarget.isAlive()) {
            this.discard(); return;
        }
        ++this.timer;
        if (this.timer >= StalkerConfig.chaseDurationTicks()) {
            this.discard(); return;
        }

        double dist = this.distanceTo(this.chaseTarget);
        if (dist <= StalkerConfig.CHASE_ATTACK_RANGE.get()) {
            this.chaseTarget.hurt(this.chaseTarget.damageSources().mobAttack(this), StalkerConfig.CHASE_DAMAGE.get().floatValue());
            this.chaseTarget.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
            this.chaseTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        }

        ++this.chaseTeleportTimer;
        if (this.chaseTeleportTimer >= StalkerConfig.chaseIntervalTicks()) {
            this.chaseTeleportTimer = 0;
            if (!this.isInPlayerView(this.chaseTarget)) {
                Vec3 toPlayer = this.chaseTarget.position().subtract(this.position()).normalize();
                double newDist = Math.max(0.0, dist - StalkerConfig.CHASE_TELEPORT_STEP.get());
                Vec3 newPos = this.chaseTarget.position().subtract(toPlayer.scale(newDist));
                this.teleportTo(newPos.x, newPos.y, newPos.z);
                this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.2f);
            }
        }
    }

    private boolean isInPlayerView(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 entityCenter = this.position().add(0, this.getBbHeight() * 0.5, 0);
        Vec3 lookVec = player.getViewVector(1.0f).normalize();
        Vec3 toEntity = entityCenter.subtract(eyePos).normalize();
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, lookVec.dot(toEntity))))) < StalkerConfig.CHASE_FOV_THRESHOLD.get();
    }

    @Nullable
    private BlockPos findNearbyDoor() {
        BlockPos center = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 2, 3))) {
            if (this.level().getBlockState(p).getBlock() instanceof DoorBlock) return p.immutable();
        }
        return null;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide) ALIVE_UUIDS.add(this.getUUID());
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        ALIVE_UUIDS.remove(this.getUUID());
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) { return false; }
    @Override
    public void push(double strength, double x, double z) {}

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("ImageIndex", (byte) getImageIndex());
        tag.putBoolean("IsBehindScare", isBehindScare());
        tag.putBoolean("IsChase", isChase());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ImageIndex")) this.entityData.set(IMAGE_INDEX, tag.getByte("ImageIndex"));
        if (tag.contains("IsBehindScare")) this.entityData.set(IS_BEHIND_SCARE, tag.getBoolean("IsBehindScare"));
        if (tag.contains("IsChase")) this.entityData.set(IS_CHASE, tag.getBoolean("IsChase"));
    }
}
