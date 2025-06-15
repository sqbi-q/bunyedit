package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.util.ChatString;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class GetSelectionAction extends ICommandAction {

    public int apply(@Nonnull Player player) throws CommandSyntaxException {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);

        player.sendMessage(String.format("%s: %s",
            i18n.translateKey("bunyedit.selection"),
            ChatString.gen(playerData.selection)));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        final LiteralCommandNode<CommandSource> selectionNode = commandDispatcher
            .register(ArgumentBuilderLiteral
                .<CommandSource>literal("/selection")
                .executes(PermissionedCommand
                    .process(
                        c -> apply(c.getSource().getSender())
                    )
                )
            );

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/sel")
            /* Brigadier will only redirect command nodes with arguments (Issue #46) */
            .executes(selectionNode.getCommand())
            .redirect(selectionNode)
        );
    }
}
