package com.thestalker.init;

import com.thestalker.TheStalker;
import com.thestalker.entity.StalkerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TheStalker.MOD_ID);

    public static final RegistryObject<EntityType<StalkerEntity>> STALKER =
            ENTITIES.register("stalker", () ->
                    EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(64)
                            .build("stalker"));
}
