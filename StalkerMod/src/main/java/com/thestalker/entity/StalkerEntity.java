package com.thestalker.entity;

import com.thestalker.StalkerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    // НАШИ ХЕЛПЕРЫ
    private final StalkerDashHelper dashHelper = new StalkerDashHelper();
    private final StalkerBarricadeBreaker barricadeBreaker = new StalkerBarricadeBreaker();
    private final StalkerPeekHelper peekHelper = new StalkerPeekHelper();

    public StalkerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(false);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IMAGE_INDEX, (byte) 1);
        this.entityData.define(IS_BEHIND_SCARE, false);
        this.entityData.define(IS_CHASE, false);
    }

    public int getImageIndex() {
        return this.entityData.get(IMAGE_INDEX);
    }

    public boolean isBehindScare() {
        return this.entityData.get(IS_BEHIND_SCARE);
    }

    public boolean isChase() {
        return this.entityData.get(IS_CHASE);
    }

    public StalkerKind getStalkerKind() {
        return StalkerKind.fromImageIndex(getImageIndex());
    }

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
        this.entityData.set(IMAGE_INDEX, (byte) 5);
        this.entityData.set(IS_BEHIND_SCARE, false);
        this.entityData.set(IS_CHASE, true);
        this.chaseTarget = target;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData data,
                                        @Nullable CompoundTag tag) {
        return super.finalizeSpawn(level, difficulty, spawnType, data, tag);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (isChase()) {
            tickChase();
            return;
        }
        if (isBehindScare()) {
            tickBehindScare();
            return;
        }
        tickStalk();
    }

    private void tickStalk() {
        // Ближайший игрок в радиусе 32 блоков — цель для AI
        Player nearby = this.level().getNearestPlayer(this, 32.0);

        // Despawn если игрок ближе 3 блоков
        Player close = this.level().getNearestPlayer(this, StalkerConfig.STALK_PROXIMITY_DESPAWN.get());
        if (close != null) {
            close.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
            this.discard();
            return;
        }

        // AI хелперы
        if (nearby != null) {
            dashHelper.tick(this, nearby);
            if (this.level() instanceof ServerLevel sl) {
                peekHelper.tick(this, sl);
            }
        }

        // Ломание баррикад — если рядом дверь
        barricadeBreaker.tick(this);

        ++this.timer;
        if (this.timer >= STALK_LIFETIME_TICKS) {
            this.discard();
        }
    }

    private void tickBehindScare() {
        ++this.timer;
        if (this.timer >= BEHIND_SCARE_TICKS) {
            this.discard();
        }
    }

    private void tickChase() {
        if (this.chaseTarget == null || !this.chaseTarget.isAlive()) {
            this.discard();
            return;
        }
        ++this.timer;
        if (this.timer >= StalkerConfig.chaseDurationTicks()) {
            this.discard();
            return;
        }
        double dist = this.distanceTo(this.chaseTarget);
        if (dist <= StalkerConfig.CHASE_ATTACK_RANGE.get()) {
            this.chaseTarget.hurt(this.chaseTarget.damageSources().mobAttack(this),
                    StalkerConfig.CHASE_DAMAGE.get().floatValue());
        }
        ++this.chaseTeleportTimer;
        if (this.chaseTeleportTimer >= StalkerConfig.chaseIntervalTicks()) {
            this.chaseTeleportTimer = 0;
            if (!isInPlayerView(this.chaseTarget)) {
                Vec3 toPlayer = this.chaseTarget.position().subtract(this.position()).normalize();
                double newDist = Math.max(0.0, dist - StalkerConfig.CHASE_TELEPORT_STEP.get());
                Vec3 newPos = this.chaseTarget.position().subtract(toPlayer.scale(newDist));
                this.teleportTo(newPos.x, newPos.y, newPos.z);
                this.level().playSound(null, this.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.2f);
            }
        }
    }

    private boolean isInPlayerView(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 entityCenter = this.position().add(0, this.getBbHeight() * 0.5, 0);
        Vec3 lookVec = player.getViewVector(1.0f).normalize();
        Vec3 toEntity = entityCenter.subtract(eyePos).normalize();
        double dot = lookVec.dot(toEntity);
        double angleDeg = Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
        return angleDeg < StalkerConfig.CHASE_FOV_THRESHOLD.get();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide) {
            ALIVE_UUIDS.add(this.getUUID());
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        ALIVE_UUIDS.remove(this.getUUID());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void push(double strength, double x, double z) {
    }

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
        if (tag.contains("ImageIndex")) {
            this.entityData.set(IMAGE_INDEX, tag.getByte("ImageIndex"));
        }
        if (tag.contains("IsBehindScare")) {
            this.entityData.set(IS_BEHIND_SCARE, tag.getBoolean("IsBehindScare"));
        }
        if (tag.contains("IsChase")) {
            this.entityData.set(IS_CHASE, tag.getBoolean("IsChase"));
        }
    }

    @Override
    protected void dropExperience() {
    }
}

