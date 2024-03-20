package mod.linguardium.linkedportals.registry;

import mod.linguardium.linkedportals.config.LinkedPortalType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;

import static mod.linguardium.linkedportals.LinkedPortals.id;

public class LinkedPortalRegistries {
    public static final RegistryKey<Registry<LinkedPortalType>> REGISTRY_LINKED_PORTAL_TYPE_KEY = RegistryKey.ofRegistry(id("types"));
    public static final RegistryKey<LinkedPortalType> ENTRY_LINKED_PORTAL_TYPE_DEFAULT_KEY = RegistryKey.of(REGISTRY_LINKED_PORTAL_TYPE_KEY,id("default"));

    private static LinkedPortalRegistries instance;
    private final DynamicRegistryManager registryManager;

    private LinkedPortalRegistries(DynamicRegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    public static Registry<LinkedPortalType> portalTypes() {
        return instance.registryManager.get(REGISTRY_LINKED_PORTAL_TYPE_KEY);
    }
    public static void init() {
        DynamicRegistries.register(REGISTRY_LINKED_PORTAL_TYPE_KEY, LinkedPortalType.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(LinkedPortalRegistries::initializeRegistries);
    }

    public static void initializeRegistries(MinecraftServer server) {
        instance = new LinkedPortalRegistries(server.getRegistryManager());
    }
}
