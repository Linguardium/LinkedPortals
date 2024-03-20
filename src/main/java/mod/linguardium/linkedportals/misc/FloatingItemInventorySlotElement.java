package mod.linguardium.linkedportals.misc;

import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Random;

public class FloatingItemInventorySlotElement extends ItemDisplayElement {
    long age = 0;
    float modulus1;
    float modulus2;
    float modulus3;
    Direction facing;

    public FloatingItemInventorySlotElement(ItemStack stack, Direction facing) {
        super(stack);
        Random random = new Random();
        modulus1 = (random.nextFloat()*70f)+50f;
        modulus2 = (random.nextFloat()*70f)+50f;
        modulus3 = (random.nextFloat()*70f)+50f;
        this.facing = facing;
    }
    public void setFacing(Direction facingDirection) {
        this.facing = facingDirection;
    }
    @Override
    public void tick() {
        age++;
        applyRotations();
        super.tick();
    }
    private void applyRotations() {
        float rotation1 = MathHelper.lerp((age % modulus1)/modulus1,0f, (float)Math.PI*2f);
        float rotation2 = MathHelper.lerp((age % modulus2)/modulus2,0f, (float)Math.PI*2f);
        float rotation3 = MathHelper.lerp((age % modulus3)/modulus3,0f, (float)Math.PI*2f);
        Matrix4f rotations = new Matrix4f();
        rotations.rotate(rotation1, facing.getUnitVector());
        rotations.rotate(rotation2, (facing.getHorizontal()>0? Direction.fromHorizontal( facing.getHorizontal()+1):Direction.NORTH).getUnitVector());
        rotations.rotate(rotation3, (facing.getHorizontal()>0?Direction.UP:Direction.WEST).getUnitVector());
        this.setTransformation(rotations);

    }
}
