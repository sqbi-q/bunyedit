package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ISelectionAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class CopyAction implements ISelectionAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, ValidSelection selection, List<String> argv
    ) throws CommandSyntaxException {
        if (argv.size() > 0) {
            throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        playerData.copyBuffer = selection.copy(true);

        // TODO Check if `cmdSource.getSender()` is equiv. to `player`
        cmdSource.getSender().sendMessage(i18n.translateKey("bunyedit.cmd.copy.success"));

        return true;
    }
}
