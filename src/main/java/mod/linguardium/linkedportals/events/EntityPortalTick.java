package mod.linguardium.linkedportals.events;

import mod.linguardium.linkedportals.interfaces.PortalUser;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public class EntityPortalTick implements TickEvents.TickEvent<Entity> {

    @Override
    public void tick(Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld)) return;
        if (entity instanceof PortalUser portalUser) PortalEvents.tickPortalTime(entity, portalUser);

    }
}
