package mod.linguardium.linkedportals.events;

import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import mod.linguardium.linkedportals.registry.LinkedPortalTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;


public class PortalControllerCreationHandler implements BlockUsageHandler{
    @Override
    public ActionResult handle(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockHitResult hitResult, ItemStack heldStack, BlockState state, @Nullable BlockEntity blockEntity) {
        if (!player.canModifyAt(world, pos)) return ActionResult.PASS;
        if (state.isIn(LinkedPortalTags.PORTAL_CONTROLLER_BASE) && heldStack.isIn(LinkedPortalTags.VALID_ACTIVATORS)) {
            heldStack.decrement(1);
            world.setBlockState(pos, LinkedPortalBlocks.PORTAL_CONTROL_BLOCK.getDefaultState());
            if (world.getBlockEntity(pos) instanceof PortalControlBlockEntity portalControlBlockEntity) {
                portalControlBlockEntity.setBaseBlock(state);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
