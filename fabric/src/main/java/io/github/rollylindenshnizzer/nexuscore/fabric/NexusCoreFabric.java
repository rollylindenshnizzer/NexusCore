package io.github.rollylindenshnizzer.nexuscore.fabric;

import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.hunger.NexusHungerValues;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.fabricmc.loader.api.FabricLoader;

public final class NexusCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        NexusCore.init();
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(item -> item.components().has(DataComponents.FOOD), (builder, item) -> {
            FoodProperties original = item.components().get(DataComponents.FOOD);
            FoodProperties modified = NexusHungerValues.apply(item, original);
            if (modified != original && !modified.equals(original)) {
                builder.set(DataComponents.FOOD, modified);
            }
        }));
        NexusIntegrations.logDependentMods(FabricLoader.getInstance().getAllMods().stream().filter(mod -> mod.getMetadata().getDependencies().stream().anyMatch(dependency -> NexusCore.MOD_ID.equals(dependency.getModId()))).map(mod -> mod.getMetadata().getId()).toList());
    }
}
