package mod.linguardium.linkedportals.events;

import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class EventHandler {

    public static void init() {
        initBlockInteractionHandlers();
        initTickHandlers();
        initSaveLoadHandler();
    }

    public static void initBlockInteractionHandlers() {
        UseBlockCallback.EVENT.register(BlockUsageHandler::handleBlockInteractions);
        BlockUsageHandler.registerGeneric(new PortalControllerCreationHandler());
        BlockUsageHandler.register(LinkedPortalBlocks.PORTAL_CONTROL_BLOCK, new PortalControlBlockInteraction());
    }

    public static void initTickHandlers() {
        TickEvents.ENTITY_TICK_EVENT.register(new EntityPortalTick());
    }

    public static void initSaveLoadHandler() {
        PortalLoadSaveEvent saveLoadEvent = new PortalLoadSaveEvent();
        ServerLifecycleEvents.SERVER_STOPPING.register(saveLoadEvent);
        ServerLifecycleEvents.SERVER_STARTED.register(saveLoadEvent);
        ServerLifecycleEvents.BEFORE_SAVE.register(saveLoadEvent);
    }

}
