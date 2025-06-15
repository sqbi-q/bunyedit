package fun.raccoon.bunyedit.command.action.actions;

import java.util.Map;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.DirectionHelper;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeIntegerCoordinates;
import net.minecraft.core.net.command.helpers.IntegerCoordinates;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.chunk.ChunkPosition;

public class StackAction extends ICommandAction {

    public int apply(
        @Nonnull Player player, int times, String directionInput, IntegerCoordinates offsetInput
    ) throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        /*
        LookDirection lookDir = new LookDirection(player);

        Direction direction = DirectionHelper.from(lookDir);
        int times = 1;
        ChunkPosition offset = PosMath.all(0);
        if (argv.size() >= 1) {
            try {
                times = Integer.parseInt(argv.get(0));
            } catch (NumberFormatException e) {
                throw CommandExceptions.INVALID_NUMBER.create();
            }
            if (times < 0) {
                throw CommandExceptions.INVALID_NUMBER.create();
            }
        }
        if (argv.size() >= 2) {
            if (!argv.get(1).equals("^")) {
                direction = DirectionHelper.fromAbbrev(argv.get(1).toUpperCase());
                if (direction == null) {
                    throw CommandExceptions.INVALID_DIRECTION.create();
                }
            }
        }
        if (argv.size() == 3) {
            offset = RelCoords.from(offset, lookDir, argv.get(2));
        }
        */

        Direction direction;

        if (directionInput == "^") {
            direction = DirectionHelper.from(new LookDirection(player));
        }
        else {
            direction = DirectionHelper.fromAbbrev(directionInput.toUpperCase());
        }
        
        if (direction == null) {
            throw CommandExceptions.INVALID_DIRECTION.create();
        }

        // TODO port offset and direction
        ChunkPosition offset = PosMath.all(0);

        if (offsetInput != null) {
            offset = new ChunkPosition(
                offsetInput.getX(0),
                offsetInput.getY(0),
                offsetInput.getZ(0)
            );
        }
        
        ChunkPosition s1 = playerData.selection.getPrimary();
        ChunkPosition s2 = playerData.selection.getSecondary();
        ChunkPosition sdim = PosMath.add(PosMath.abs(PosMath.sub(s2, s1)), PosMath.all(1));

        offset = PosMath.add(
            offset,
            PosMath.mul(PosMath.directionOffset(direction), sdim)
        );

        BlockBuffer before = selection.copy(false);
        BlockBuffer after = new BlockBuffer();

        BlockBuffer copyBuffer = selection.copy(true);
        for(int i = 0; i < times; ++i) {
            for (Map.Entry<ChunkPosition, BlockData> entry : copyBuffer.entrySet()) {
                ChunkPosition pos = entry.getKey();
                BlockData blockData = entry.getValue();

                pos = PosMath.add(pos, s1);
                pos = PosMath.add(pos, PosMath.mul(offset, PosMath.all(i+1)));

                before.put(pos, new BlockData(player.world, pos));

                after.placeRaw(player.world, pos, blockData);
            }
        }
        after.finalize(player.world);

        playerData.getUndoTape(player.world).push(before, after);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        // TODO neater way to handle argument layers

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/stack")
            .executes(PermissionedCommand
                .process(c -> apply(
                    c.getSource().getSender(),
                    1, "^", null
                ))
            )
            .then(ArgumentBuilderRequired
                .<CommandSource, Integer>argument(
                    "times", ArgumentTypeInteger.integer(0)
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("times", Integer.class),
                        "^", null
                    ))
                )
                .then(ArgumentBuilderRequired
                    .<CommandSource, String>argument(
                        "direction", ArgumentTypeString.string()
                    )
                    .executes(PermissionedCommand
                        .process(c -> apply(
                            c.getSource().getSender(),
                            c.getArgument("times", Integer.class),
                            c.getArgument("direction", String.class),
                            null
                        ))
                    )
                    .then(ArgumentBuilderRequired
                        .<CommandSource, IntegerCoordinates>argument(
                            "offset", ArgumentTypeIntegerCoordinates.intCoordinates()
                        )
                        .executes(PermissionedCommand
                            .process(c -> apply(
                                c.getSource().getSender(),
                                c.getArgument("times", Integer.class),
                                c.getArgument("direction", String.class),
                                c.getArgument("offset", IntegerCoordinates.class)
                            ))
                        )
                    )
                )
            )
        );
    }
}
