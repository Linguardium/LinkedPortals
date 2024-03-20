package mod.linguardium.linkedportals.blocks.blockentity;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import mod.linguardium.linkedportals.blocks.PortalControlBlock;
import mod.linguardium.linkedportals.interfaces.ElementHolderHolder;
import mod.linguardium.linkedportals.interfaces.PortalStructurePart;
import mod.linguardium.linkedportals.interfaces.ReportsToController;
import mod.linguardium.linkedportals.interfaces.Tickable;
import mod.linguardium.linkedportals.misc.FloatingItemInventorySlotElement;
import mod.linguardium.linkedportals.misc.LinkedPortalsUtil;
import mod.linguardium.linkedportals.portal.LinkedPortalManager;
import mod.linguardium.linkedportals.portal.PortalBuilder;
import mod.linguardium.linkedportals.portal.PortalStructure;
import mod.linguardium.linkedportals.registry.LinkedPortalBlockEntities;
import mod.linguardium.linkedportals.registry.LinkedPortalBlocks;
import mod.linguardium.linkedportals.registry.LinkedPortalRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PortalControlBlockEntity extends BlockEntity implements PortalStructurePart, ElementHolderHolder, SingleStackInventory, Tickable {

    private PortalStructure structure;
    private ItemStack keyStack = ItemStack.EMPTY;
    private ElementHolder keyItemElementHolder = new ElementHolder();
    private FloatingItemInventorySlotElement keyItemDisplay;
    private UUID portalGroupID = Util.NIL_UUID;
    private GlobalPos globalPos;
    private boolean structureDirty = true;
    private boolean firstTick = true;
    private BlockState baseBlock = Blocks.AIR.getDefaultState();

    public PortalControlBlockEntity(BlockPos pos, BlockState state) {
        super(LinkedPortalBlockEntities.PORTAL_CONTROL_BLOCKENTITY_TYPE, pos, state);
    }

    public void setBaseBlock(BlockState state) {
        this.baseBlock = state;
        this.markDirty();
    }

    public BlockState getBaseBlock() {
        return baseBlock;
    }

    public Optional<PortalStructure> getPortalStructure() {
        return Optional.ofNullable(this.structure);
    }

    public Optional<UUID> getPortalGroupId() {
        if (portalGroupID == null || portalGroupID.equals(Util.NIL_UUID)) return Optional.empty();
        return Optional.of(portalGroupID);
    }

    public ItemStack getKeyStackCopy() {
        return keyStack.copy();
    }

    /**
     * This method initializes the portal using the passed in itemstack as a keying/identifying agent
     *
     *  Removes the existing structure
     *  Removes the existing keying item
     *  Unregisters the current controller from the portal manager
     *  Wipes the current portal id
     *  Attempts to build the portal or returns
     *  Sets the current item as the key stack
     *  Generates the new portal identifier from the itemstack
     *  Builds the portal structure
     *  sets the keying item slot
     *  drops any remaining items from the stack
     *  sets the underlying blockstate to active or not active
     *
     * @param portalKey ItemStack to set as the portal keying item
     * @return true if the structure was created or the keying stack was empty
     */
    public boolean setPortalKey(ItemStack portalKey) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return false;

        destroy(false);

        if (!portalKey.isEmpty()) {
            ItemStack newStack = portalKey.copyWithCount(1);
            Optional<PortalStructure> maybeStructure = PortalBuilder.findPortal(serverWorld,this.getPos(),LinkedPortalRegistries.portalTypes().streamEntries().collect(Collectors.toList()));
            if (maybeStructure.isEmpty()) return false;
            UUID portalId = LinkedPortalsUtil.getUUIDfromItemStack(newStack);
            setPortal(serverWorld,portalId,maybeStructure.get());
            setKeyStack(newStack);
            portalKey.decrement(1);
            if (!portalKey.isEmpty()) ItemScatterer.spawn(serverWorld,this.getPos(),new SimpleInventory(portalKey));
        }
        serverWorld.setBlockState(this.getPos(),this.getCachedState().with(PortalControlBlock.ACTIVE, this.getPortalGroupId().isPresent()));
        this.markDirty();
        return true;
    }

    // set fields
    // activate portal with manager
    public void setPortal(ServerWorld serverWorld, UUID portalId, PortalStructure structure) {
        setPortalGroupID(portalId);
        setPortalStructure(structure);
        if (getPortalGroupId().isPresent() && getPortalStructure().isPresent()) {
            LinkedPortalManager.activateController(portalId, serverWorld, this.getPos());
            fillTeleportationBlocks(serverWorld);
        }
        this.markDirty();
    }

    private void fillTeleportationBlocks(ServerWorld serverWorld) {
        Optional<PortalStructure> maybePortalStructure = getPortalStructure();
        if (maybePortalStructure.isEmpty()) return;

        maybePortalStructure.get().teleporters().forEach(blockPos -> {
            serverWorld.setBlockState(blockPos, LinkedPortalBlocks.PORTAL_FILL_BLOCK.getDefaultState().with(Properties.FACING, getCachedState().getOrEmpty(Properties.FACING).orElse(Direction.NORTH)));
            if (serverWorld.getBlockEntity(blockPos) instanceof ReportsToController reporter)
                reporter.setControllerPos(this.getPos());
        });

    }

    /**
     * set field, mark dirty
     */
    public void setPortalStructure(PortalStructure structure) {
        this.structure = structure;
        this.markDirty();
    }

    // set field
    // set up item display
    public void setKeyStack(ItemStack key) {
        this.keyStack = key;
        setKeyItemDisplay(key);
        this.markDirty();
    }

    // remove existing key item from element holder
    // make a new key item display
    // add the item display to the holder
    public void setKeyItemDisplay(ItemStack key) {
        keyItemElementHolder.removeElement(keyItemDisplay);
        createKeyItemDisplay(key.copyWithCount(1));
        keyItemElementHolder.addElement(keyItemDisplay);
    }

    /**
     * set field, no side effects
     */
    public void setPortalGroupID(UUID id) {
        this.portalGroupID = id;
        this.markDirty();
    }

    // validates with existing structure, if it exists
    // destroys if stucture exists and fails validation
    public void validateOrDestroy() {
        if (getKeyStackCopy().isEmpty()) {
            destroy(false);
            return;
        }
        if (!getPortalStructure().map(portalStructure->portalStructure.validate(this.getWorld())).orElse(true)) {
            this.destroy(false);
        }
    }

    public void markStructureDirty() {
        this.structureDirty = true;
    }

    public void destroyStructure(ServerWorld serverWorld) {
        getPortalStructure().ifPresent(portalStructure -> portalStructure.teleporters().forEach(pos->serverWorld.breakBlock(pos, false)));
        this.setPortalStructure(null);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (world instanceof ServerWorld) {
            getElementHolder().destroy();
            if (this.keyItemDisplay == null) {
                createKeyItemDisplay(getStack());
            }
            getElementHolder().addElement(keyItemDisplay);
            this.globalPos = GlobalPos.create(world.getRegistryKey(), this.getPos());
        }
    }

    private void createKeyItemDisplay(ItemStack stack) {
        if (stack == null) return;
        this.keyItemDisplay = new FloatingItemInventorySlotElement(stack, this.getCachedState().getOrEmpty(Properties.FACING).orElse(Direction.NORTH));
        this.keyItemDisplay.setOffset(Vec3d.ZERO);
        this.keyItemDisplay.setModelTransformation(ModelTransformationMode.FIXED);
        this.keyItemDisplay.setShadowRadius(0f);
        this.keyItemDisplay.setShadowStrength(0f);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld serverWorld && this.keyItemElementHolder.getAttachment() == null) {
            ChunkAttachment.of(getElementHolder(), serverWorld, this.getPos());
        }
        if (this.structureDirty || world.getTime() % 100 == 0) {
            validateOrDestroy();
            this.structureDirty = false;
        }
        keyItemElementHolder.tick();
    }

    public void destroy(boolean removal) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        dropKeyItem(serverWorld);
        destroyStructure(serverWorld);
        setPortalGroupID(Util.NIL_UUID);
        if (removal) {
            if (this.keyItemElementHolder != null) keyItemElementHolder.destroy();
        }
        LinkedPortalManager.deactivateController(serverWorld, this.getPos());
    }

    /**
     * Called when the block is removed or replaced to drop the key stack from the inventory
     * @param serverWorld The world to drop the item into
     */
    private void dropKeyItem(ServerWorld serverWorld) {
        if (!this.keyStack.isEmpty()) {
            ItemScatterer.spawn(serverWorld, this.getPos().offset(this.getCachedState().getOrEmpty(Properties.FACING).orElse(Direction.UP)), new SimpleInventory(keyStack));
            keyStack = ItemStack.EMPTY;
            setKeyItemDisplay(keyStack);
        }
        this.markDirty();
    }

    @Override
    public @Nullable BlockPos getControllerPos() {
        return this.getPos();
    }

    @Override
    public ElementHolder getElementHolder() {
        return keyItemElementHolder;
    }

    @Override
    public void setElementHolder(ElementHolder holder) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        if (holder != null) {
            if (this.keyItemElementHolder != null) keyItemElementHolder.destroy();
            keyItemElementHolder = holder;
            holder.addElement(keyItemDisplay);
            ChunkAttachment.of(holder,serverWorld,this.getPos());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.keyStack = ItemStack.EMPTY;
        this.portalGroupID = Util.NIL_UUID;
        this.structure = null;
        this.baseBlock = Blocks.AIR.getDefaultState();

        if (nbt.contains("KeyItem", NbtElement.COMPOUND_TYPE)) {
            this.keyStack = ItemStack.fromNbt(nbt.getCompound("KeyItem"));
        }
        setKeyItemDisplay(this.keyStack);

        if (nbt.contains("PortalStructure",NbtElement.COMPOUND_TYPE)) {
            try {
                this.structure = Util.getResult(PortalStructure.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("PortalStructure")), RuntimeException::new);
            }catch (RuntimeException ignored) {}
        }

        if (nbt.contains("PortalGroupId", NbtElement.INT_ARRAY_TYPE)) {
            try {
                this.portalGroupID = nbt.getUuid("PortalGroupId");
            }catch(IllegalArgumentException ignored) { }

        }
        if (nbt.contains("BaseBlock")) {
            this.baseBlock = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(),nbt.getCompound("BaseBlock"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("KeyItem", getStack().writeNbt(new NbtCompound()));
        getPortalStructure().ifPresent(portalStructure -> {
                    try {
                        nbt.put("PortalStructure", Util.getResult(PortalStructure.CODEC.encodeStart(NbtOps.INSTANCE, this.structure), RuntimeException::new));
                    } catch (RuntimeException ignored) {}
                });
        getPortalGroupId().ifPresent(portalId->nbt.putUuid("PortalGroupId",portalId));
        if (!baseBlock.isOf(Blocks.AIR)) {
            nbt.put("BaseBlock", NbtHelper.fromBlockState(baseBlock));
        }
    }

    @Override
    public ItemStack getStack() {
        return keyStack;
    }

    @Override
    public ItemStack decreaseStack(int count) {
        ItemStack oldStack = keyStack.copy();
        ItemStack returnedStack = oldStack.split(count);
        setStack(oldStack);
        return returnedStack;
    }

    @Override
    public void setStack(ItemStack stack) {
        ItemStack droppedStack = stack.copy();
        setPortalKey(droppedStack.split(1));
        if (getStack().isEmpty()) droppedStack = stack;
        ItemScatterer.spawn(this.getWorld(),this.getPos(), new SimpleInventory(droppedStack));
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

}
