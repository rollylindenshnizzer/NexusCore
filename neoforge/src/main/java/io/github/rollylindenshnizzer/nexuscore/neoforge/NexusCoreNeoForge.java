package io.github.rollylindenshnizzer.nexuscore.neoforge;

import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.hunger.NexusHungerValues;
import io.github.rollylindenshnizzer.nexuscore.api.integration.NexusIntegrations;
import io.github.rollylindenshnizzer.nexuscore.neoforge.client.NexusCoreNeoForgeClient;
import io.github.rollylindenshnizzer.nexuscore.neoforge.integration.NexusNeoForgeIntegrations;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@Mod(NexusCore.MOD_ID)
public final class NexusCoreNeoForge {
    public NexusCoreNeoForge(IEventBus modEventBus) {
        // Run our common setup.
        NexusCore.init();
        modEventBus.addListener(EventPriority.NORMAL, NexusCoreNeoForge::modifyDefaultComponents);
        NexusNeoForgeIntegrations.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NexusCoreNeoForgeClient.register(modEventBus);
        }
        NexusIntegrations.logDependentMods(ModList.get().getMods().stream().filter(mod -> mod.getDependencies().stream().anyMatch(dependency -> NexusCore.MOD_ID.equals(dependency.getModId()))).map(mod -> mod.getModId()).toList());
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.getAllItems().forEach(item -> {
            FoodProperties original = item.components().get(DataComponents.FOOD);
            if (original == null) {
                return;
            }
            FoodProperties modified = NexusHungerValues.apply(item, original);
            if (modified != original && !modified.equals(original)) {
                event.modify(item, builder -> builder.set(DataComponents.FOOD, modified));
            }
        });
    }

}
