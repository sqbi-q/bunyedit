package fun.raccoon.bunyedit.command.action;

import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.mask.Masks;
import fun.raccoon.bunyedit.data.selection.Selection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.parsers.CmdArgs;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public interface ISelectionAction extends IPlayerAction {
    public boolean apply(I18n i18n, CommandSource cmdSource, @Nonnull Player player, PlayerData playerData, ValidSelection selection, List<String> argv) throws CommandSyntaxException;
    
    @Override
    default public boolean apply(I18n i18n, CommandSource cmdSource, @Nonnull Player player, PlayerData playerData, List<String> argv) throws CommandSyntaxException {
        Selection selection = playerData.selection;
        
        int i = 0;
        while (i < argv.size()) {
            switch(argv.get(i)) {
                case "-m":
                    argv.remove(i);
                    try {
                        String maskArgStr = argv.get(i);
                        List<String> maskArgs = CmdArgs.parse(maskArgStr);
                        String maskName = maskArgs.get(0);

                        @Nullable IMaskCommand maskCmd = Masks.MASKS.get(maskName);
                        if (maskCmd == null) {
                            throw CommandExceptions.NO_SUCH_MASK.formatAndCreate(maskName);
                        }

                        String[] maskArgv = {};
                        if (maskArgs.size() >= 1) {
                            maskArgv = maskArgs.subList(1, maskArgs.size()).toArray(new String[0]);
                        }

                        BiPredicate<ValidSelection, ChunkPosition> mask = maskCmd.build(maskArgv);

                        selection = new Selection(selection);
                        selection.setMask(argv.get(i), mask);
                    } catch (IndexOutOfBoundsException e) {
                        throw CommandExceptions
                            .fromString("-m: " + i18n.translateKey("bunyedit.cmd.err.toofewargs"))
                            .create();
                    }
                    argv.remove(i);
                    break;
                default:
                    ++i;
            }
        }

        ValidSelection validSelection = ValidSelection.fromSelection(selection);
        if (validSelection == null || !selection.getWorld().equals(player.world)) {
            throw CommandExceptions.INCOMPLETE_SELECTION.create();
        }

        Long limit = playerData.selectionLimit;
        if (limit != null) {
            long vol = validSelection.coordStream().count();
            if (vol > limit) {
                throw CommandExceptions.SELECTION_TOO_LARGE.formatAndCreate(vol);
            }
        }
        return apply(i18n, cmdSource, player, playerData, validSelection, argv);
    }
}
