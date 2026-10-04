package io.github.rollylindenshnizzer.nexuscore.client;

import io.github.rollylindenshnizzer.nexuscore.api.client.particle.NexusGuiParticles;
import io.github.rollylindenshnizzer.nexuscore.api.client.text.NexusTextEffects;
import io.github.rollylindenshnizzer.nexuscore.api.client.texture.NexusProceduralTextures;
import net.minecraft.server.packs.resources.ResourceManager;

public final class NexusClientResources {
    private NexusClientResources() {
    }

    public static void reload(ResourceManager manager) {
        NexusGuiParticles.reload(manager);
        NexusTextEffects.reload(manager);
        NexusProceduralTextures.regenerateAll();
    }
}
