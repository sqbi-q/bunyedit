package fun.raccoon.bunyedit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fun.raccoon.bunyedit.Cursor;
import fun.raccoon.bunyedit.data.selection.Selection;
import net.minecraft.client.entity.player.PlayerLocalMultiplayer;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.world.World;

@Mixin(value = Item.class, remap = false)
public abstract class CursorUseMixin {
    @Inject(method = "onUseItem", at = @At("TAIL"))
    private void onUseItemCheckCursor(ItemStack itemstack, World world, Player Player, CallbackInfoReturnable<ItemStack> cir) {
        // serverside has this covered in multiplayer
        if (Player != null && !(Player instanceof PlayerLocalMultiplayer)
                && Cursor.isCursorItem(itemstack))
            Cursor.select(Player, Selection.Slot.SECONDARY);
    }
}
