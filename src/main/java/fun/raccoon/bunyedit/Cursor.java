package fun.raccoon.bunyedit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.Selection;
import fun.raccoon.bunyedit.mixin.WorldAccessor;
import fun.raccoon.bunyedit.util.ChatString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.util.phys.HitResult.HitType;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.net.packet.PacketChat;
import net.minecraft.core.util.phys.Vec3;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkPosition;
import net.minecraft.server.entity.player.PlayerServer;
import turniplabs.halplibe.helper.EnvironmentHelper;

/**
 * Tool to assist players with inputting selection coordinates.
 */
public class Cursor {
    /** 
     * maximum distance from the player that blocks can be selected from
     */
    private static final int TRACE_DISTANCE = 256;

    /**
     * default item used for the cursor, deliberately chosen to be reasonably unique
     * for editor plugins i've used: worldedit prefers the wooden axe, voxelsniper
     * prefers arrow and gunpowder.
     * <p>
     * i chose a feather because it looks vaguely like a mouse pointer if you squint.
     */
    private static final Item DEFAULT_CURSOR_ICON = Items.FEATHER_CHICKEN;

    /**
     * get {@link ItemStack} for the default cursor
     */
    public static ItemStack getCursorItem() {
        ItemStack cursor = new ItemStack(DEFAULT_CURSOR_ICON);
        cursor.setCustomName("Cursor");
        cursor.getData().putBoolean("bunyedit.cursor", true);

        return cursor;
    }

    /**
     * does existing {@link ItemStack} carry our cursor tag?
     * <p>
     * for flexibility, deliberately choosing to not be strict about the {@link Item}
     */
    public static boolean isCursorItem(@Nullable ItemStack stack) {
        return stack != null && stack.getData().getBoolean("bunyedit.cursor");
    }

    /**
     * CommandSender provides sendMessage, which unifies sending raw chat messages
     * to an individual player in the context of both singleplayer and multiplayer.
     * <p>
     * for some reason, {@link Player} does not provide this except in the special
     * case of one plain constant translation key. so, we provide
     * {@link net.minecraft.core.net.command.CommandSender}'s API here for convenience.
     */
    private static void sendMessage(Player player, String s) {
        if (EnvironmentHelper.isServerEnvironment()) {
            ((PlayerServer)player).playerNetServerHandler.sendPacket(new PacketChat(s));
        } else if (player instanceof PlayerLocal) {
            Minecraft.getMinecraft().hudIngame.addChatMessage(s);
        }
    }

    /**
     * Trace along player's view vector to find the nearest solid block, and place its
     * coordinates in the specified selection slot. Additionally provide slot,
     * coordinate, and selected block in chat.
     */
    public static void select(@Nonnull Player player, Selection.Slot slot) {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);
        World world = player.world;

        // don't pay attention to duplicate interactions on the same tick
        if (!playerData.lastInteract.tryUpdate(((WorldAccessor) world).getRuntime()))
            return;

        Vec3 start = player.getPosition(0, false);
        // PlayerServer doesn't know about its height offset?
        if (EnvironmentHelper.isServerEnvironment() /*&& player instanceof PlayerServer*/) {
            if (player.isDwarf())
                start.y += 0.62;
            else
                start.y += 1.62;
        }
        Vec3 direction = player.getViewVector(0);
        Vec3 end = start.add(
            direction.x * TRACE_DISTANCE,
            direction.y * TRACE_DISTANCE,
            direction.z * TRACE_DISTANCE);
        @Nullable HitResult hit = world.checkBlockCollisionBetweenPoints(start, end);

        if (hit != null && hit.hitType.equals(HitType.TILE)) {
            ChunkPosition pos = new ChunkPosition(hit.x, hit.y, hit.z);
            playerData.selection.set(slot, world, pos);

            sendMessage(player, ChatString.gen_select_action(slot, world, pos));
        } else {
            sendMessage(player,
                TextFormatting.formatted(
                    i18n.translateKey("bunyedit.cursor.outofrange"),
                    TextFormatting.RED));
        }
    }
}

