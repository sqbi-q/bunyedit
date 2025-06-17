package fun.raccoon.bunyedit.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fun.raccoon.bunyedit.Cursor;
import fun.raccoon.bunyedit.data.selection.Selection;
import net.minecraft.core.entity.player.Player;
import turniplabs.halplibe.helper.EnvironmentHelper;


@Mixin(value = Player.class, remap = false)
public abstract class CursorSwingMixin {
    @Inject(method = "swingItem", at = @At("TAIL"))
    private void swingItemCheckCursor(CallbackInfo ci) {
        Player player = (Player)(Object)this;
        
        // serverside has this covered in multiplayer
        if (!EnvironmentHelper.isServerEnvironment()
            && Cursor.isCursorItem(player.inventory.getCurrentItem())
        )
            Cursor.select(player, Selection.Slot.PRIMARY);
    }
}
