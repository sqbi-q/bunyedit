package fun.raccoon.bunyedit.command.action.actions;

import java.util.Map;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeInteger;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.command.action.arguments.ArgumentTypeBunyDirection;
import fun.raccoon.bunyedit.command.action.arguments.ArgumentTypeCoords;
import fun.raccoon.bunyedit.command.action.arguments.BunyDirection;
import fun.raccoon.bunyedit.command.action.arguments.Coords;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.chunk.ChunkPosition;

public class StackAction extends ICommandAction {

    public int apply(
        @Nonnull Player player, int times, BunyDirection directionInput, Coords offsetInput
    ) throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);
        LookDirection lookDir = new LookDirection(player);

        Direction direction = directionInput.getDirection(lookDir);

        ChunkPosition offset = PosMath.all(0);

        if (offsetInput != null) {
            // Will treat absolute and relative coords the same, is this intended?
            offset = offsetInput.asAbsolute(PosMath.all(0), lookDir);
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
                    1, BunyDirection.playerDirection(), null
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
                        BunyDirection.playerDirection(), null
                    ))
                )
                .then(ArgumentBuilderRequired
                    .<CommandSource, BunyDirection>argument(
                        "direction", new ArgumentTypeBunyDirection()
                    )
                    .executes(PermissionedCommand
                        .process(c -> apply(
                            c.getSource().getSender(),
                            c.getArgument("times", Integer.class),
                            c.getArgument("direction", BunyDirection.class),
                            null
                        ))
                    )
                    .then(ArgumentBuilderRequired
                        .<CommandSource, Coords>argument(
                            "offset", new ArgumentTypeCoords()
                        )
                        .executes(PermissionedCommand
                            .process(c -> apply(
                                c.getSource().getSender(),
                                c.getArgument("times", Integer.class),
                                c.getArgument("direction", BunyDirection.class),
                                c.getArgument("offset", Coords.class)
                            ))
                        )
                    )
                )
            )
        );
    }
}
