package io.github.rollylindenshnizzer.nexuscore.fabric.client;

import io.github.rollylindenshnizzer.nexuscore.NexusCore;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextClientTooltip;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextTooltip;
import io.github.rollylindenshnizzer.nexuscore.client.NexusClient;
import io.github.rollylindenshnizzer.nexuscore.client.NexusClientResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class NexusCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NexusClient.init();
        TooltipComponentCallback.EVENT.register(data -> data instanceof NexusTextTooltip tooltip ? new NexusTextClientTooltip(tooltip) : null);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(NexusCore.MOD_ID, "client_resources");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                NexusClientResources.reload(resourceManager);
            }
        });
    }
}
