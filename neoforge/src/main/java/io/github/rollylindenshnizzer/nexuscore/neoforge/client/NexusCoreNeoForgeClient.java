package io.github.rollylindenshnizzer.nexuscore.neoforge.client;

import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextClientTooltip;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextTooltip;
import io.github.rollylindenshnizzer.nexuscore.client.NexusClient;
import io.github.rollylindenshnizzer.nexuscore.client.NexusClientResources;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public final class NexusCoreNeoForgeClient {
    private NexusCoreNeoForgeClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NexusCoreNeoForgeClient::onClientSetup);
        modEventBus.addListener(NexusCoreNeoForgeClient::onRegisterReloadListeners);
        modEventBus.addListener(NexusCoreNeoForgeClient::onRegisterTooltipFactories);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NexusClient::init);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) NexusClientResources::reload);
    }

    private static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(NexusTextTooltip.class, NexusTextClientTooltip::new);
    }
}
