package mod.linguardium.linkedportals.events;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface BlockUsageHandler {

    ActionResult handle(ServerWorld world, ServerPlayerEntity player, BlockPos pos, BlockHitResult hitResult, ItemStack heldStack, BlockState state, @Nullable BlockEntity blockEntity);

    Multimap<Block, BlockUsageHandler> handlers = LinkedListMultimap.create();
    List<BlockUsageHandler> genericHandlers = new ArrayList<>();
    static void register(Block block, BlockUsageHandler handler) {
        handlers.put(block, handler);
    }
    static void registerGeneric(BlockUsageHandler handler) {
        genericHandlers.add(handler);
    }
    static ActionResult handleBlockInteractions(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!hand.equals(Hand.MAIN_HAND)) return ActionResult.PASS;
        if (player.isSpectator()) return ActionResult.PASS;
        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
        ItemStack heldItemStack = player.getStackInHand(hand);
        for (BlockUsageHandler callback : handlers.get(blockState.getBlock())) {
            if (world.isClient()) return ActionResult.SUCCESS;
            ActionResult ret = callback.handle(serverWorld, serverPlayer, pos, hitResult, heldItemStack, blockState, blockEntity);
            if (ret.isAccepted()) return ret;
        }
        for (BlockUsageHandler callback : genericHandlers) {
            ActionResult ret = callback.handle(serverWorld, serverPlayer, pos, hitResult, heldItemStack, blockState, blockEntity);
            if (ret.isAccepted()) return ret;
        }

        return ActionResult.PASS;
    }

    @SuppressWarnings("unchecked")
    default <T extends BlockEntity> Optional<T> getBlockEntity(ServerWorld world, BlockPos pos, BlockEntityType<T> type) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity.getType().equals(type)) return Optional.of((T)blockEntity);
        return Optional.empty();
    }
}
