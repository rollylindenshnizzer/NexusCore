package io.github.rollylindenshnizzer.nexuscore.internal.integration;

public final class NexusIntegrationImplementations {
    private static NexusFancyTabSectionsImplementation fancyTabSections;
    private static NexusIronsCreativeImplementation ironsCreative;

    private NexusIntegrationImplementations() {
    }

    public static NexusFancyTabSectionsImplementation fancyTabSections() {
        return fancyTabSections;
    }

    public static NexusIronsCreativeImplementation ironsCreative() {
        return ironsCreative;
    }

    public static void setFancyTabSections(NexusFancyTabSectionsImplementation implementation) {
        fancyTabSections = implementation;
    }

    public static void setIronsCreative(NexusIronsCreativeImplementation implementation) {
        ironsCreative = implementation;
    }
}
