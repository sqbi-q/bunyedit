package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ISelectionAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.parsers.Bound;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.chunk.ChunkPosition;

public class GrowSelectionAction implements ISelectionAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, ValidSelection selection, List<String> argv
    ) throws CommandSyntaxException {
        Pair<ChunkPosition, ChunkPosition> growBy;
        switch (argv.size()) {
            case 0:
                growBy = Bound.fromString(selection, player, "*1");
                break;
            case 1:
                growBy = Bound.fromString(selection, player, argv.get(0));
                break;
            default:
                throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        if (growBy == null) {
            throw CommandExceptions.INVALID_BOUND.create();
        }

        playerData.selection.setBound(growBy);

        return true;
    }
}
