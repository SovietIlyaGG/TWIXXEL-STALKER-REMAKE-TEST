package com.thestalker;

import com.thestalker.client.StalkerConfigScreen;
import com.thestalker.command.StalkerCommand;
import com.thestalker.entity.StalkerEntity;
import com.thestalker.events.StalkerEvents;
import com.thestalker.init.ModEntities;
import com.thestalker.init.ModSounds;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TheStalker.MOD_ID)
public class TheStalker {
    public static final String MOD_ID = "stalker";

    private final StalkerEvents stalkerEvents = new StalkerEvents();

    public TheStalker() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        modBus.addListener(this::onAttributeCreate);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                StalkerConfig.SPEC, "stalker-common.toml");

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> StalkerConfigScreen.create(parent)));

        MinecraftForge.EVENT_BUS.register(this.stalkerEvents);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.STALKER.get(), StalkerEntity.createAttributes().build());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        StalkerCommand.register(event.getDispatcher(), this.stalkerEvents);
    }
}