package mod.linguardium.linkedportals.events;

import mod.linguardium.linkedportals.LinkedPortals;
import mod.linguardium.linkedportals.portal.LinkedPortalManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;

public class PortalLoadSaveEvent implements ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopping,ServerLifecycleEvents.BeforeSave {

    @Override
    public void onServerStarted(MinecraftServer server) {
        LinkedPortalManager.load(server);
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        LinkedPortalManager manager = LinkedPortalManager.getInstance(server);
        if (manager != null) {
            try {
                manager.save();
            } catch (IOException e) {
                LinkedPortals.LOGGER.error(e.getMessage(),e);
            }
        }
    }

    @Override
    public void onBeforeSave(MinecraftServer server, boolean flush, boolean force) {
        LinkedPortalManager manager = LinkedPortalManager.getInstance(server);
        if (manager != null) {
            try {
                manager.save();
            } catch (IOException e) {
                LinkedPortals.LOGGER.error(e.getMessage(),e);
            }
        }
    }
}
