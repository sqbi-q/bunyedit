package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ISelectionAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import fun.raccoon.bunyedit.util.parsers.RelCoords;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class MoveSelAction implements ISelectionAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, ValidSelection selection, List<String> argv
    ) throws CommandSyntaxException {
        ChunkPosition origin;
        switch (argv.size()) {
            case 0:
                throw CommandExceptions.TOO_FEW_ARGS.create();
            case 1:
                origin = RelCoords.from(selection.getPrimary(), new LookDirection(player), argv.get(0));
                break;
            default:
                throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        playerData.selection.setPrimary(player.world, origin);
        playerData.selection.setSecondary(player.world, PosMath.add(origin, PosMath.sub(selection.getSecondary(), selection.getPrimary())));

        return true;
    }
}
