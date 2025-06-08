package fun.raccoon.bunyedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.block.BlockLogicSign;

@Mixin(value = BlockLogicSign.class, remap = false)
public interface BlockSignAccessor {
    @Accessor
    public boolean getIsFreestanding();
}
