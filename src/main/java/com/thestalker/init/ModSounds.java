package com.thestalker.init;

import com.thestalker.TheStalker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StalkerMod.MOD_ID);

    public static final RegistryObject<SoundEvent> BREAK_EVENT_1 = register("break_event_1");
    public static final RegistryObject<SoundEvent> BREAK_EVENT_2 = register("break_event_2");
    public static final RegistryObject<SoundEvent> BREAK_EVENT_3 = register("break_event_3");
    public static final RegistryObject<SoundEvent> BREAK_EVENT_4 = register("break_event_4");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () ->
                SoundEvent.createVariableRangeEvent(
                        new ResourceLocation(StalkerMod.MOD_ID, name)));
    }
}

