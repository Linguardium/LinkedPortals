package mod.linguardium.linkedportals.blocks;

import com.mojang.serialization.MapCodec;
import mod.linguardium.linkedportals.blocks.base.AbstractPortalStructureBlock;
import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.events.PortalEvents;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class LinkedPortalFillerBlock extends AbstractPortalStructureBlock {
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(LinkedPortalFillerBlock::new);
    }

    public LinkedPortalFillerBlock() {
        this(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL).mapColor(MapColor.BLACK));
    }

    public LinkedPortalFillerBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return LinkedPortalBlockEntities.PORTAL_STRUCTURE_BLOCK_ENTITY_TYPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.END_GATEWAY.getDefaultState();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.getControllerBlockEntity(world,pos).map(PortalControlBlockEntity::getPortalGroupId).isPresent()) {
            PortalEvents.entityEnteredPortal(entity);
            return;
        }
        world.breakBlock(pos,false);
    }



    @Override
    public @Nullable BlockEntity getPolymerBlockEntity(BlockState blockState, BlockPos pos, ServerPlayerEntity player) {
        EndGatewayBlockEntity be = new EndGatewayBlockEntity(pos.toImmutable(), getPolymerBlockState(blockState));
        be.readNbt(END_GATEWAY_BLOCKENTITY_NBT);
        return be;
    }

    @Override
    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return true;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return world.setBlockState(pos,fluidState.getBlockState(), NOTIFY_ALL);
    }

    private static final NbtCompound END_GATEWAY_BLOCKENTITY_NBT = new NbtCompound();
    static {
        END_GATEWAY_BLOCKENTITY_NBT.putLong("Age", 10000L);
    }



}
