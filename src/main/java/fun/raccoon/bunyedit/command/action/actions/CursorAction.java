package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fun.raccoon.bunyedit.Cursor;
import fun.raccoon.bunyedit.command.action.ICommandAction;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;

public class CursorAction extends ICommandAction {
    
    public int apply(@Nonnull Player player) {
        player.inventory.insertItem(Cursor.getCursorItem(), true);
        
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        final LiteralCommandNode<CommandSource> cursorNode = commandDispatcher
            .register(ArgumentBuilderLiteral
                .<CommandSource>literal("/cursor")
                .executes(PermissionedCommand
                    .process(
                        c -> apply(c.getSource().getSender())
                    )
                )
            );

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/cur")
            /* Brigadier will only redirect command nodes with arguments (Issue #46) */
            .executes(cursorNode.getCommand())
            .redirect(cursorNode)
        );
    }
}
