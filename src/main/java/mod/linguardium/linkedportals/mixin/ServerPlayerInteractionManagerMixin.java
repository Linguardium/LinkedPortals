package mod.linguardium.linkedportals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @WrapOperation(method="tryBreakBlock", at= @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;canMine(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean swapControllerBlockStateCanMine(Item instance, BlockState state, World world, BlockPos pos, PlayerEntity miner, Operation<Boolean> original) {
        BlockState blockState = state;
        if (state.isOf(LinkedPortalBlocks.PORTAL_CONTROL_BLOCK) && world.getBlockEntity(pos) instanceof PortalControlBlockEntity portalControlBlockEntity) {
            blockState = portalControlBlockEntity.getBaseBlock();
            if (blockState.isAir()) return true;
        }
        return original.call(instance,blockState,world,pos,miner);
    }
}
