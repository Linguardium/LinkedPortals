package mod.linguardium.linkedportals.mixin;

import mod.linguardium.linkedportals.interfaces.PortalUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mod.linguardium.linkedportals.events.TickEvents.ENTITY_TICK_EVENT;

@Mixin(Entity.class)
public abstract class EntityMixin implements PortalUser {
    @Shadow public abstract int getDefaultPortalCooldown();

    @Shadow public abstract int getMaxNetherPortalTime();

    @Unique
    long inPortalTime = 0;
    @Unique
    boolean isInPortal = false;
    @Unique
    long portalCooldown=0;
    @Unique
    long portalDamageTimer=0;
    @Unique
    int portalDamageIterations=0;
    @Unique
    long maxTimeInPortal = -1;


    @Inject(method="tick",at=@At("RETURN"))
    private void invokeTickEvent(CallbackInfo ci) {
        ENTITY_TICK_EVENT.invoker().tick((Entity) (Object) this);
    }

    @Override
    public long linkedportals$getTimeInPortal() {
        return inPortalTime;
    }

    @Override
    public void linkedportals$setTimeInPortal(long ticks) {
        inPortalTime = ticks;
    }

    @Override
    public boolean linkedportals$isInPortal() {
        return isInPortal;
    }

    @Override
    public void linkedportals$setIsInPortal(boolean inPortal) {
        if (!inPortal) {
            linkedportals$setTimeInPortal(0);
        }
        isInPortal = inPortal;
    }

    @Override
    public boolean linkedportals$hasPortalCooldown() {
        return portalCooldown > 0;
    }

    @Override
    public long linkedportals$getPortalCooldown() {
        return portalCooldown;
    }

    @Override
    public void linkedportals$resetPortalCooldown() {
        linkedportals$setPortalCooldown(getDefaultPortalCooldown());
    }

    @Override
    public void linkedportals$setPortalCooldown(long ticks) {
        portalCooldown = ticks;
    }

    @Override
    public boolean linkedportals$incrementTimeInPortal() {
        linkedportals$setTimeInPortal(linkedportals$getTimeInPortal()+1);
        return inPortalTime >= linkedportals$getMaxTimeInPortal();
    }

    @Override
    public void linkedportals$setMaxTimeInPortal(long ticks) {
        maxTimeInPortal = ticks;
    }

    @Override
    public long linkedportals$getMaxTimeInPortal() {
        if (maxTimeInPortal < 0) linkedportals$resetMaxTimeInPortal();
        if (maxTimeInPortal < 0) maxTimeInPortal = Long.MAX_VALUE;
        return maxTimeInPortal;
    }

    @Override
    public void linkedportals$resetTimeInPortal() {
        linkedportals$setTimeInPortal(0);
    }

    @Override
    public void linkedportals$decrementPortalCooldown() {
        linkedportals$setPortalCooldown(linkedportals$getPortalCooldown()-1);
    }

    @Override
    public boolean linkedportals$decrementPortalDamageTimer() {
        linkedportals$setPortalDamageTimer(linkedportals$getPortalDamageTimer()-1);
        return linkedportals$getPortalDamageTimer() <= 0;
    }

    @Override
    public long linkedportals$getPortalDamageTimer() {
        return portalDamageTimer;
    }

    @Override
    public void linkedportals$setPortalDamageTimer(long ticks) {
        portalDamageTimer = ticks;
    }

    @Override
    public void linkedportals$setPortalDamageIterations(int iterations) {
        this.portalDamageIterations = iterations;
        linkedportals$resetMaxTimeInPortal();
    }

    @Override
    public long linkedportals$resetMaxTimeInPortal() {
        if (((Entity)(Object)this) instanceof PlayerEntity && linkedportals$getPortalDamageTimer() == 0) {
            maxTimeInPortal = Math.min(10,getMaxNetherPortalTime());
        }else {
            maxTimeInPortal = getMaxNetherPortalTime();
        }
        return maxTimeInPortal;
    }

    @Override
    public int linkedportals$getPortalDamageIterations() {
        return portalDamageIterations;
    }

}
