package mod.linguardium.linkedportals.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

public class TickEvents {
    public static final Event<TickEvent<Entity>> ENTITY_TICK_EVENT = EventFactory.createArrayBacked(TickEvent.class,
            listeners -> (Entity caller) -> {
                for (TickEvent<Entity> tickEvent : listeners) {
                    tickEvent.tick(caller);
                }
            }
    );
    @FunctionalInterface
    public interface TickEvent<T> {
        void tick(T callingObject);
    }
}
