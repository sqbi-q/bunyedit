package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.IPlayerAction;
import fun.raccoon.bunyedit.data.PlayerData;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class LimitAction implements IPlayerAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, List<String> argv
    ) throws CommandSyntaxException {
        switch (argv.size()) {
            case 0:
                cmdSource.getSender()
                    .sendMessage(i18n.translateKeyAndFormat(
                        "bunyedit.cmd.limit.print", playerData.selectionLimit
                    ));
                break;

            case 1:
                if (argv.get(0).equals("no")) {
                    playerData.selectionLimit = null;
                } else {
                    long newLimit;
                    try {
                        newLimit = Long.parseLong(argv.get(0));
                        if (newLimit < 0)
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        throw CommandExceptions.INVALID_NUMBER.create();
                    }
                    playerData.selectionLimit = newLimit;
                }

                cmdSource.getSender().sendMessage(i18n.translateKey("bunyedit.cmd.limit.success"));
                break;
        }

        return true;
    }
}
