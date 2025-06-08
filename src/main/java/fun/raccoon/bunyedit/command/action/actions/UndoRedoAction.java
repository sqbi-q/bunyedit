package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.IPlayerAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.EntityBuffer;
import fun.raccoon.bunyedit.data.buffer.UndoPage;
import fun.raccoon.bunyedit.data.buffer.UndoTape;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class UndoRedoAction implements IPlayerAction {
    public enum Which {
        UNDO,
        REDO
    }

    private Which which;

    public UndoRedoAction(Which which) {
        this.which = which;
    }

    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, List<String> argv
    ) throws CommandSyntaxException {
        if (argv.size() > 0) {
            throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        UndoTape undoTape = playerData.getUndoTape(player.world);
        UndoPage page = this.which.equals(Which.UNDO)
            ? undoTape.undo()
            : undoTape.redo();

        if (page == null) {
            String whichI18n = i18n.translateKey(this.which.equals(Which.UNDO)
                ? "bunyedit.cmd.undoredo.undo"
                : "bunyedit.cmd.undoredo.redo");

            throw CommandExceptions.NO_PAGES.formatAndCreate(whichI18n);
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

        return true;
    }
}
