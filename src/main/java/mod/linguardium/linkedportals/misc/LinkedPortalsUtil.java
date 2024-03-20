package mod.linguardium.linkedportals.misc;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LinkedPortalsUtil {

    public static UUID getUUIDfromItemStack(ItemStack stack) {
        if (stack.isEmpty()) return Util.NIL_UUID;
        StringBuilder uuidSourceStringBuilder = new StringBuilder();
        // component values
        uuidSourceStringBuilder.append(Registries.ITEM.getId(stack.getItem()));
        uuidSourceStringBuilder.append(stack.getName().getString());
        // Nbt values
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            // TODO: add book page handling
//            uuidSourceStringBuilder.append(
//                    nbt.getList(PAGES_KEY, NbtElement.STRING_TYPE).stream()
//                    .map(NbtElement::asString)
//                    .map(Text.Serialization::fromLenientJson)
//                    .filter(Objects::nonNull)
//                    .map(Text::getString)
//                    .collect(Collectors.joining("\n"))
//            );
            if (nbt.contains("CustomModelData",NbtElement.NUMBER_TYPE)) {
                uuidSourceStringBuilder.append(nbt.getInt("CustomModelData"));
            }
        }
        return UUID.nameUUIDFromBytes(uuidSourceStringBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param world The world that the item will be spawned into
     * @param stack The stack to spawn into the world
     * @param side The ejection side of the block source. If null, will be calculated ignoring any other block.
     * @param source The source block position that the item is being ejected from.
     * @param target The target block position that the item is being ejected towards.
     * @return The item entity that is spawned into the world
     */
    public static ItemEntity ejectItemTowardsPosition(World world, ItemStack stack, @Nullable Direction side, BlockPos source, BlockPos target) {
        Vec3d ejectVecStart = target.toCenterPos().subtract(source.toCenterPos());
        Direction offsetDir = side;
        float horizontalRotation = (float)(Math.toRadians(90) - MathHelper.atan2(ejectVecStart.getZ(),ejectVecStart.getX()));
        float verticalRotation = (float)-MathHelper.atan2(ejectVecStart.getY(),MathHelper.hypot(ejectVecStart.getX(),ejectVecStart.getZ()));
        Vec3d ejectVec = new Vec3d(MathHelper.sin(horizontalRotation) * MathHelper.cos(verticalRotation), -MathHelper.sin(verticalRotation), MathHelper.cos(horizontalRotation) * MathHelper.cos(verticalRotation)).multiply(0.2);
        if (offsetDir == null) {
            offsetDir = Direction.getFacing(ejectVec.getX(),ejectVec.getY(),ejectVec.getZ());
        }
        Vec3d ejectStart = source.toCenterPos().offset(offsetDir,1.5);
        ItemEntity itemEntity = new ItemEntity(world, ejectStart.getX(),ejectStart.getY(),ejectStart.getZ(), stack);
        itemEntity.setVelocity(ejectVec);
        world.spawnEntity(itemEntity);
        return itemEntity;
    }
}
