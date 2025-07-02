package fun.raccoon.bunyedit.command.action.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.command.action.arguments.block.BlockFilter;
import fun.raccoon.bunyedit.command.action.arguments.block.BlockPattern;
import fun.raccoon.bunyedit.command.action.arguments.block.WeightedBlock;
import fun.raccoon.bunyedit.command.action.arguments.bound.ArgumentTypeBound;
import fun.raccoon.bunyedit.command.action.arguments.bound.Bound;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.ChunkPosition;

public class FillAction extends ICommandAction {
    // TODO OutOfMemory on bound bigger than `*8` and filter `*`
    private void recurse(
        World world,
        Set<ChunkPosition> set,
        Predicate<BlockData> filter,
        Pair<ChunkPosition, ChunkPosition> bound,
        ChunkPosition origin,
        ChunkPosition pos
    ) {
        if (
            PosMath.inside(bound.getLeft(), bound.getRight(), PosMath.sub(pos, origin))
            && filter.test(new BlockData(world, pos))
        ) {
            set.add(pos);
            for (Direction dir : Direction.directions) {
                ChunkPosition newPos = PosMath.add(pos, PosMath.directionOffset(dir));

                if (!set.contains(newPos))
                    recurse(world, set, filter, bound, origin, newPos);
            }
        }        
    }

    public int apply(
        @Nonnull Player player,
        Predicate<BlockData> filterInput,
        WeightedBlock[] patternInput,
        Bound boundInput
    ) throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        Pair<ChunkPosition, ChunkPosition> maxSelectionGrowBy = boundInput.getOffsets(
            selection, new LookDirection(player)
        );

        Set<ChunkPosition> set = new HashSet<>();
        for (ChunkPosition pos : selection.coordStream().collect(Collectors.toSet())) {
            recurse(player.world, set, filterInput, maxSelectionGrowBy, pos, pos);
        }
        
        BlockBuffer before = new BlockBuffer();
        BlockBuffer after = new BlockBuffer();

        set.forEach(pos -> {
            BlockData blockData = new BlockData(player.world, pos);
            before.put(pos, blockData);
            
            WeightedBlock chosenBlock = BlockPattern.getRandomBlock(patternInput);
            after.placeRaw(player.world, pos, (BlockData)chosenBlock);
        });
        after.finalize(player.world);
        
        playerData.getUndoTape(player.world).push(before, after);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        // TODO neater way to handle argument layers
        
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/fill")
            .then(ArgumentBuilderRequired
                .<CommandSource, Predicate<BlockData>>argument(
                    "filter", BlockFilter.blocksPredicate()
                )
                .then(ArgumentBuilderRequired
                    .<CommandSource, WeightedBlock[]>argument(
                        "pattern", BlockPattern.weightedBlocks()
                    )
                    .executes(PermissionedCommand
                        .process(c -> apply(
                            c.getSource().getSender(),
                            c.getArgument("filter", Predicate.class),
                            c.getArgument("pattern", WeightedBlock[].class),
                            new Bound().all(16) // `*16`
                        ))
                    )
                    .then(ArgumentBuilderRequired
                        .<CommandSource, Bound>argument(
                            "bound", new ArgumentTypeBound()
                        )
                        .executes(PermissionedCommand
                            .process(c -> apply(
                                c.getSource().getSender(),
                                c.getArgument("filter", Predicate.class),
                                c.getArgument("pattern", WeightedBlock[].class),

                                new Bound() // `*16, ...` bound
                                    .all(16)
                                    .set(c.getArgument("bound", Bound.class))
                            ))
                        )
                    )
                )
            )
        );
    }
}
