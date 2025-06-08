package fun.raccoon.bunyedit.command.action;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.BunyEdit;
import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.data.PlayerData;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.player.gamemode.Gamemode;

// TODO normalize all IAction subtypes - eg. player is already known in CommandSource

public interface IPlayerAction extends IAction {
    public boolean apply(I18n i18n, CommandSource cmdSource, @Nonnull Player player, PlayerData playerData, List<String> argv) throws CommandSyntaxException;

    @Override
    default public boolean apply(I18n i18n, CommandSource cmdSource, List<String> argv) throws CommandSyntaxException {
        @Nullable Player player = cmdSource.getSender();
        if (player == null) {
            throw CommandExceptions.NOT_A_PLAYER.create();
        }

        boolean allowed = false;
        if (cmdSource.hasAdmin()) {
            allowed = true;
        } else {
            if (player.gamemode.equals(Gamemode.creative) && BunyEdit.ALLOWED_CREATIVE) {
                allowed = true;
            } else if (player.gamemode.equals(Gamemode.survival) && BunyEdit.ALLOWED_SURVIVAL) {
                allowed = true;
            }
        }

        if (!allowed) {
            throw CommandExceptions.INSUFFICIENT_PERMS.create();
        }

        return apply(i18n, cmdSource, player, PlayerData.get(player), argv);
    }
}
