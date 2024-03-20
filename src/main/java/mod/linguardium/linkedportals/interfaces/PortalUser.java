package mod.linguardium.linkedportals.interfaces;

public interface PortalUser {

    long linkedportals$getTimeInPortal();
    void linkedportals$setTimeInPortal(long time);
    long linkedportals$getMaxTimeInPortal();
    void linkedportals$setMaxTimeInPortal(long ticks);
    long linkedportals$resetMaxTimeInPortal();
    /**
     * @return Returns true if the entity has reached the required time in the portal
     */
    boolean linkedportals$incrementTimeInPortal();

    void linkedportals$resetTimeInPortal();
    boolean linkedportals$isInPortal();
    void linkedportals$setIsInPortal(boolean inPortal);
    boolean linkedportals$hasPortalCooldown();

    /**
     * @return Returns the current time in ticks until portals can be used
     */
    long linkedportals$getPortalCooldown();

    /**
     *  sets the time until portal usage is allowed to the default
     */
    void linkedportals$resetPortalCooldown();
    void linkedportals$setPortalCooldown(long ticks);
    void linkedportals$decrementPortalCooldown();

    /**
     * @return returns true if the damage timer has elapsed
     */
    boolean linkedportals$decrementPortalDamageTimer();
    long linkedportals$getPortalDamageTimer();
    void linkedportals$setPortalDamageTimer(long ticks);

    void linkedportals$setPortalDamageIterations(int ticks);
    int linkedportals$getPortalDamageIterations();
}
