package mod.linguardium.linkedportals.events;

import me.lucko.fabric.api.permissions.v0.Permissions;
import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.config.LinkedPortalType;
import mod.linguardium.linkedportals.misc.LinkedPortalsUtil;
import mod.linguardium.linkedportals.portal.PortalStructure;
import mod.linguardium.linkedportals.registry.LinkedPortalRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PortalControlBlockInteraction implements BlockUsageHandler {

    @Override
    public ActionResult handle(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockHitResult hitResult, ItemStack heldStack, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof PortalControlBlockEntity portalControlBlockEntity)) return ActionResult.PASS;
        if (!player.canModifyAt(world, pos)) return ActionResult.PASS;

        if (player.isSneaking())  {
            if (!canLock(world,player,portalControlBlockEntity)) return ActionResult.FAIL;
            world.setBlockState(pos,state.with(Properties.LOCKED, !state.get(Properties.LOCKED)));
            return ActionResult.SUCCESS;
        }
        if (state.getOrEmpty(Properties.LOCKED).orElse(false)) return ActionResult.PASS;

        if (!portalControlBlockEntity.getKeyStackCopy().isEmpty()) {
            ItemStack stack = portalControlBlockEntity.getKeyStackCopy();
            portalControlBlockEntity.setKeyStack(ItemStack.EMPTY); // remove item from being dropped
            portalControlBlockEntity.destroy(false); // destroy structure
            if (!player.isCreative()) {
                LinkedPortalsUtil.ejectItemTowardsPosition(world, stack, null, portalControlBlockEntity.getPos(), player.getBlockPos());
            }
            return ActionResult.SUCCESS;
        }

        if (!heldStack.isEmpty()) {
            if (portalControlBlockEntity.setPortalKey(heldStack.copyWithCount(1))) {
                if (!player.isCreative()) {
                    heldStack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
            player.sendMessage(Text.literal("Invalid portal structure").formatted(Formatting.RED),true);
            world.getServer().execute(()->player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, player.getInventory().selectedSlot, heldStack)));
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    boolean canLock(ServerWorld world, ServerPlayerEntity player, PortalControlBlockEntity blockEntity) {
        boolean hasOPLockPermission = Permissions.check(player, "linkedportals.masterkey",false) || world.getServer().getPlayerManager().isOperator(player.getGameProfile()) || world.getServer().isSingleplayer();
        boolean isPortalTypeLockable = blockEntity.getPortalStructure().map(PortalStructure::portalType).map(typeid->LinkedPortalRegistries.portalTypes().get(typeid)).map(LinkedPortalType::lockable).orElse(false);
        return isPortalTypeLockable || hasOPLockPermission;
    }



}
