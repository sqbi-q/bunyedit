package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.IPlayerAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.Selection;
import fun.raccoon.bunyedit.util.ChatString;
import fun.raccoon.bunyedit.util.parsers.RelCoords;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetSelectionAction implements IPlayerAction {
    private Selection.Slot slot;

    public SetSelectionAction(Selection.Slot slot) {
        this.slot = slot;
    }

    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, List<String> argv
    ) throws CommandSyntaxException {
        ChunkPosition pos;
        switch (argv.size()) {
            case 0:
                pos = RelCoords.playerPos(player, false);
                break;
            case 1:
                pos = RelCoords.from(player, argv.get(0));
                if (pos == null) {
                    throw CommandExceptions.INVALID_COORDS.create();
                }
                break;
            default:
                throw CommandExceptions.TOO_MANY_ARGS.create();
        }
        
        playerData.selection.set(slot, player.world, pos);

        cmdSource.getSender().sendMessage(ChatString.gen_select_action(slot, player.world, pos));

        return true;

    }
}
