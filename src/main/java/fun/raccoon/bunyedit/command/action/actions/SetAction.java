package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ISelectionAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.parsers.Filter;
import fun.raccoon.bunyedit.util.parsers.Pattern;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetAction implements ISelectionAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, ValidSelection selection, List<String> argv
    ) throws CommandSyntaxException {
        String patternStr;
        String filterStr;
        switch (argv.size()) {
            case 0:
                throw CommandExceptions.TOO_FEW_ARGS.create();
            case 1:
                filterStr = null;
                patternStr = argv.get(0);
                break;
            case 2:
                filterStr = argv.get(0);
                patternStr = argv.get(1);
                break;
            default:
                throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        Player sender = cmdSource.getSender();

        Function<BlockData, BlockData> pattern = Pattern.fromString(sender, patternStr);
        if (pattern == null) {
            throw CommandExceptions.INVALID_PATTERN.create();
        }

        Stream<ChunkPosition> stream = selection.coordStream();

        if (filterStr != null) {
            Predicate<BlockData> filter = Filter.fromString(filterStr);
            if (filter == null) {
                throw CommandExceptions.INVALID_FILTER.create();
            }
            stream = stream
                .filter(pos -> filter.test(new BlockData(player.world, pos)))
                // this might look silly. but we need the filter to be greedy
                //
                // consider //set sugarcane <...>: if we aren't greedy here,
                // the sugarcane will break before we can find it
                .collect(Collectors.toList()).stream();
        }
        
        BlockBuffer before = selection.copy(false);
        BlockBuffer after = new BlockBuffer();

        stream.forEach(pos -> {
            BlockData blockData = pattern.apply(new BlockData(player.world, pos));
            after.placeRaw(player.world, pos, blockData);
        });
        after.finalize(player.world);
        
        playerData.getUndoTape(player.world).push(before, after);

        return true;
    }
}
