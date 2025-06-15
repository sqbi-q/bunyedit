package fun.raccoon.bunyedit.command.action.actions;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeBlock;
import net.minecraft.core.net.command.helpers.BlockInput;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetAction extends ICommandAction {

    public int apply(@Nonnull Player player, BlockInput patternInput) 
    throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        // TODO port pattern and filter parameters 
        /*
        String patternStr;
        String filterStr;
            case 1:
                filterStr = null;
                patternStr = argv.get(0);
                break;
            case 2:
                filterStr = argv.get(0);
                patternStr = argv.get(1);
                break;

        Function<BlockData, BlockData> pattern = Pattern.fromString(player, patternStr);
        if (pattern == null) {
            throw CommandExceptions.INVALID_PATTERN.create();
        }
        */

        Stream<ChunkPosition> stream = selection.coordStream();

        /*
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
        */
        
        BlockBuffer before = selection.copy(false);
        BlockBuffer after = new BlockBuffer();

        stream.forEach(pos -> {
            // BlockData blockData = pattern.apply(new BlockData(player.world, pos));
            BlockData blockData = new BlockData(patternInput.getBlock());
            after.placeRaw(player.world, pos, blockData);
        });
        after.finalize(player.world);
        
        playerData.getUndoTape(player.world).push(before, after);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {

        final LiteralCommandNode<CommandSource> setNode = commandDispatcher
            .register(ArgumentBuilderLiteral
                .<CommandSource>literal("/set")
                .then(
                    ArgumentBuilderRequired
                    .<CommandSource, BlockInput>argument(
                        "pattern", ArgumentTypeBlock.block()
                    )
                    .executes(PermissionedCommand
                        .process(c -> apply(
                            c.getSource().getSender(),
                            c.getArgument("pattern", BlockInput.class)
                        ))
                    )
                )
            );

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/replace")
            .redirect(setNode)
        );
    }
}
