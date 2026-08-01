package fun.raccoon.bunyedit.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fun.raccoon.bunyedit.Cursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.controller.PlayerControllerSP;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.Side;
import net.minecraft.core.world.pos.TilePosc;

@Mixin(value = PlayerControllerSP.class, remap = false)
public abstract class CursorCancelBlockBreakMixin {
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void destroyBlockCancel(
        @NotNull TilePosc tilePos, @NotNull Side side,
        // int x, int y, int z, Side side, Player player,
        CallbackInfoReturnable<Boolean> cir
    ) {
        Player player = Minecraft.getMinecraft().thePlayer;
        if (Cursor.isCursorItem(player.inventory.getCurrentItem())) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
