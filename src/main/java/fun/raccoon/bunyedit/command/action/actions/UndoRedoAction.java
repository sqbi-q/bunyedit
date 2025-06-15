package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.EntityBuffer;
import fun.raccoon.bunyedit.data.buffer.UndoPage;
import fun.raccoon.bunyedit.data.buffer.UndoTape;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class UndoRedoAction extends ICommandAction {
    public enum Which {
        UNDO,
        REDO
    }

    public int apply(@Nonnull Player player, Which whichDirection) 
    throws CommandSyntaxException {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);

        UndoTape undoTape = playerData.getUndoTape(player.world);
        UndoPage page = whichDirection.equals(Which.UNDO)
            ? undoTape.undo()
            : undoTape.redo();

        if (page == null) {
            String whichDirectionI18n = i18n.translateKey(whichDirection.equals(Which.UNDO)
                ? "bunyedit.cmd.undoredo.undo"
                : "bunyedit.cmd.undoredo.redo");

            throw CommandExceptions.NO_PAGES.formatAndCreate(whichDirectionI18n);
        }

        BlockBuffer newBlocks = page.getRight().blocks;
        EntityBuffer oldEnts = page.getLeft().entities;
        EntityBuffer newEnts = page.getRight().entities;

        BlockBuffer after = new BlockBuffer();
        newBlocks.forEach((pos, blockData) -> {
            after.placeRaw(player.world, pos, blockData);
        });
        after.finalize(player.world);

        oldEnts.destroyIn(player.world);
        newEnts.createIn(player.world);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/undo")
            .executes(PermissionedCommand
                .process(
                    c -> apply(c.getSource().getSender(), Which.UNDO)
                )
            )
        );
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/redo")
            .executes(PermissionedCommand
                .process(
                    c -> apply(c.getSource().getSender(), Which.REDO)
                )
            )
        );
    }
}
