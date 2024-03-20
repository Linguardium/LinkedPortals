package mod.linguardium.linkedportals.blocks;

import com.mojang.serialization.MapCodec;
import mod.linguardium.linkedportals.blocks.base.AbstractPortalStructureBlock;
import mod.linguardium.linkedportals.blocks.blockentity.PortalControlBlockEntity;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockEntities;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class PortalControlBlock extends AbstractPortalStructureBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(PortalControlBlock::new);
    }

    public PortalControlBlock() {
        this(AbstractBlock.Settings.copy(Blocks.LODESTONE).mapColor(MapColor.GRAY).luminance(PortalControlBlock::getLuminance));
    }

    public PortalControlBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(ACTIVE,false).with(Properties.LOCKED,false));
    }

    public static int getLuminance(BlockState state) {
        int light = 5;
        if (state.getOrEmpty(ACTIVE).orElse(false)) {
            light = 15;
        }
        if (state.getOrEmpty(Properties.LOCKED).orElse(false)) {
            return light / 2;
        }
        return light;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ACTIVE,Properties.LOCKED);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PortalControlBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return LinkedPortalBlockEntities.PORTAL_CONTROL_BLOCKENTITY_TYPE;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        if (state.getOrEmpty(Properties.LOCKED).orElse(false)) {
            return Blocks.SPAWNER.getDefaultState();
        }
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public void onStateReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(oldState.getBlock()) && world.getBlockEntity(pos) instanceof PortalControlBlockEntity portalControlBlockEntity) {
            portalControlBlockEntity.destroy(true);
        }
        super.onStateReplaced(oldState, world, pos, newState, moved);
    }

    @Override
    public boolean canFillWithFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof PortalControlBlockEntity portalControlBlockEntity) {
            return portalControlBlockEntity.getBaseBlock().calcBlockBreakingDelta(player, world, pos);
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockState blockState = super.onBreak(world, pos, state, player);
        if (world.getBlockEntity(pos) instanceof PortalControlBlockEntity portalControlBlockEntity) {
            blockState = portalControlBlockEntity.getBaseBlock();
        }
        return blockState;
    }
}
