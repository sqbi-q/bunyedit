package fun.raccoon.bunyedit.command.action.actions;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.command.action.arguments.block.BlockFilter;
import fun.raccoon.bunyedit.command.action.arguments.block.BlockPattern;
import fun.raccoon.bunyedit.command.action.arguments.block.WeightedBlock;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetAction extends ICommandAction {

    public int apply(
        @Nonnull Player player,
        @Nullable Predicate<BlockData> filterInput,
        WeightedBlock[] patternInput
    ) throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        Stream<ChunkPosition> stream = selection.coordStream();

        if (filterInput != null) {
            stream = stream
                .filter(pos -> filterInput.test(new BlockData(player.world, pos)))
                // this might look silly. but we need the filter to be greedy
                //
                // consider //set sugarcane <...>: if we aren't greedy here,
                // the sugarcane will break before we can find it
                .collect(Collectors.toList()).stream();
        }
        
        BlockBuffer before = selection.copy(false);
        BlockBuffer after = new BlockBuffer();

        stream.forEach(pos -> {
            // BlockData blockData = pattern.apply(new BlockData(player.world, pos));
            WeightedBlock chosenBlock = BlockPattern.getRandomBlock(patternInput);
            after.placeRaw(player.world, pos, (BlockData)chosenBlock);
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
                    .<CommandSource, WeightedBlock[]>argument(
                        "pattern", BlockPattern.weightedBlocks()
                    )
                    .executes(PermissionedCommand
                        .process(c -> apply(
                            c.getSource().getSender(),
                            null,
                            c.getArgument("pattern", WeightedBlock[].class)
                        ))
                    )
                )
                .then(
                    ArgumentBuilderRequired
                    .<CommandSource, Predicate<BlockData>>argument(
                        "filter", BlockFilter.blocksPredicate()
                    )
                    .then(
                        ArgumentBuilderRequired
                        .<CommandSource, WeightedBlock[]>argument(
                            "pattern", BlockPattern.weightedBlocks()
                        )
                        .executes(PermissionedCommand
                            .process(c -> apply(
                                c.getSource().getSender(),
                                c.getArgument("filter", Predicate.class),
                                c.getArgument("pattern", WeightedBlock[].class)
                            ))
                        )
                    )
                )
            );

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/replace")
            .redirect(setNode)
        );
    }
}
