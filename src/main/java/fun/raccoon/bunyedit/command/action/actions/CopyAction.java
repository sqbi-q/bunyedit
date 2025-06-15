package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class CopyAction extends ICommandAction {

    public int apply(@Nonnull Player player) throws CommandSyntaxException {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        playerData.copyBuffer = selection.copy(true);

        player.sendMessage(i18n.translateKey("bunyedit.cmd.copy.success"));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/copy")
            .executes(PermissionedCommand
                .process(
                    c -> apply(c.getSource().getSender())
                )
            )
        );
    }
}
