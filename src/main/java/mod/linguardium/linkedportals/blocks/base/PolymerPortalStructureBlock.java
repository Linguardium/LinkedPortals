package mod.linguardium.linkedportals.blocks.base;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface PolymerPortalStructureBlock extends PolymerBlock {

    default @Nullable BlockEntity getPolymerBlockEntity(BlockState blockState, BlockPos pos, ServerPlayerEntity player) {
        return null;
    }

    @Override
    default Block getPolymerBlock(BlockState state) {
        return getPolymerBlockState(state).getBlock();
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state) {
        return Blocks.BEDROCK.getDefaultState();
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        return getPolymerBlockState(state);
    }

    @Override
    default void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, ServerPlayerEntity player) {
        BlockEntity blockEntity = getPolymerBlockEntity(blockState, pos, player);
        if (blockEntity != null) {
            player.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(blockEntity, BlockEntity::createNbt));

        }
    }

    @Override
    default BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return getPolymerBlockState(state);
    }

    @Override
    default boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return false;
    }

}
