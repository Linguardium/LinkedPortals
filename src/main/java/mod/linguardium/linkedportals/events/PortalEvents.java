package mod.linguardium.linkedportals.events;

import mod.linguardium.linkedportals.blocks.base.AbstractPortalStructureBlock;
import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.interfaces.PortalUser;
import mod.linguardium.linkedportals.interfaces.ReportsToController;
import mod.linguardium.linkedportals.portal.PortalStructure;
import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import mod.linguardium.linkedportals.portal.LinkedPortalManager;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.TeleportTarget;

import java.util.Optional;

public class PortalEvents {

    public static void entityEnteredPortal(Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        if (entity instanceof EnderPearlEntity enderPearl) {
            if (enderPearl.getOwner() != null) {
                handleEnderPearl(serverWorld, enderPearl);
            }
            return;
        }
        if (!(entity instanceof PortalUser portalUser)) return;
        portalUser.linkedportals$setIsInPortal(true);
    }
    public static void handleEnderPearl(ServerWorld serverWorld, EnderPearlEntity enderPearl) {
        getControllerFromPositionOrEmpty(serverWorld,enderPearl).ifPresent(controller->
            handleTeleportationHandler(serverWorld,controller,enderPearl.getOwner())
        );
    }
    public static Optional<PortalControlBlockEntity> getControllerFromPositionOrEmpty(ServerWorld serverWorld, Entity entity) {
        Optional<BlockPos> maybePos = BlockPos.stream(entity.getBoundingBox()).filter(pos->serverWorld.getBlockState(pos).isOf(LinkedPortalBlocks.PORTAL_FILL_BLOCK)).findAny();
        if (maybePos.isEmpty()) return Optional.empty();
        BlockPos pos = maybePos.get();
        return ((AbstractPortalStructureBlock)serverWorld.getBlockState(pos).getBlock()).getControllerBlockEntity(serverWorld, pos);
    }
    public static void tickPortalTime(Entity entity, PortalUser portalUser) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        Optional<BlockPos> maybePos = BlockPos.stream(entity.getBoundingBox()).filter(pos->serverWorld.getBlockState(pos).isOf(LinkedPortalBlocks.PORTAL_FILL_BLOCK)).findAny();
        PortalControlBlockEntity controller = getControllerFromPositionOrEmpty(serverWorld,entity).orElse(null);
        boolean inPortal = maybePos.isPresent();
        if (inPortal && entity instanceof EnderPearlEntity enderPearl) {
            handleEnderPearl(serverWorld, enderPearl);
            return;
        }
        if (inPortal != portalUser.linkedportals$isInPortal()) {
            portalUser.linkedportals$setIsInPortal(inPortal);
        }

        if (!inPortal && (portalUser.linkedportals$getPortalDamageTimer() > 0 || portalUser.linkedportals$getPortalDamageIterations() > 0)) {
            portalUser.linkedportals$setPortalDamageTimer(0);
            portalUser.linkedportals$setPortalDamageIterations(0);
        }

        if (portalUser.linkedportals$hasPortalCooldown()) {
            if (!inPortal) portalUser.linkedportals$decrementPortalCooldown();
            else portalUser.linkedportals$resetPortalCooldown();
            return;
        }

        if (inPortal && controller != null && portalUser.linkedportals$incrementTimeInPortal()) {
            handleTeleportationHandler(serverWorld, controller, entity);
        }
    }
    public static Optional<GlobalPos> getNextControllerLocation(ServerWorld serverWorld, PortalControlBlockEntity controller) {
        return controller.getPortalGroupId().flatMap(portalId->LinkedPortalManager.getNextPortal(portalId,serverWorld,controller.getPos()));
    }
    public static void handleTeleportationHandler(ServerWorld serverWorld, PortalControlBlockEntity controller, Entity entity) {
        Optional<GlobalPos> nextPortalControllerLocation = getNextControllerLocation(serverWorld, controller);
        nextPortalControllerLocation.ifPresentOrElse(
                nextPortal->attemptTeleport(serverWorld, entity, nextPortal),
                ()->handleMissingOutputPortal(serverWorld, entity)
        );

    }
    public static void handleMissingOutputPortal(ServerWorld serverWorld, Entity entity) {
        if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
            PortalUser user = (PortalUser)entity;
            if (serverPlayerEntity.isCreative() || serverPlayerEntity.isSpectator()) {
                user.linkedportals$setPortalDamageTimer(0);
                user.linkedportals$setPortalDamageIterations(0);
                return;
            }
            if (user.linkedportals$decrementPortalDamageTimer()) {
                int iterations = user.linkedportals$getPortalDamageIterations();
                float screamVolume = 1.0f + (float)Math.floor(0.3f*iterations);
                SoundEvents.AMBIENT_CAVE.value();
                SoundEvent soundEvent = switch (iterations) {
                    case 2 -> SoundEvents.ENTITY_ENDERMAN_SCREAM;
                    case 3 -> SoundEvents.ENTITY_ENDERMAN_STARE;
                    default -> SoundEvents.AMBIENT_CAVE.value();
                };
                serverWorld.playSoundFromEntity(null, entity,soundEvent, SoundCategory.PLAYERS, screamVolume, 1.0f);
                serverPlayerEntity.sendMessage(Text.literal("The void calls..." + (3 - iterations)), true);
                if (iterations > 0) {
                    float damageDivisor = 3.0f / iterations; //  health/3  health/1.5  health/1
                    serverPlayerEntity.damage(serverWorld.getDamageSources().outOfWorld(), serverPlayerEntity.getHealth() / damageDivisor);
                }
                user.linkedportals$setPortalDamageTimer(Math.max(entity.getMaxNetherPortalTime(), 60));
                user.linkedportals$setPortalDamageIterations(iterations+1);
                user.linkedportals$setTimeInPortal(0);
            }
        }
    }
    public static void attemptTeleport(ServerWorld serverWorld, Entity entity, GlobalPos targetControllerPos) {
        ServerWorld targetWorld = serverWorld.getServer().getWorld(targetControllerPos.getDimension());
        ((PortalUser)entity).linkedportals$resetPortalCooldown();
        if (targetWorld != null && (targetWorld.getBlockEntity(targetControllerPos.getPos()) instanceof PortalControlBlockEntity controller)) {
            Optional<PortalStructure> maybeStructure= controller.getPortalStructure();
            if (maybeStructure.isPresent()) {
                Vec3d lowestBlockPos = maybeStructure.get().getLowestTeleportPos();
                if (lowestBlockPos == null) return;

                FabricDimensions.teleport(
                        entity,
                        targetWorld,
                        new TeleportTarget(
                                lowestBlockPos,
                                Vec3d.ZERO,
                                maybeStructure.get().facing().asRotation(),
                                0
                        )
                );
            }
        }
    }
}
