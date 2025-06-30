package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.command.action.arguments.ArgumentTypeBunyAxis;
import fun.raccoon.bunyedit.command.action.arguments.BunyAxis;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import fun.raccoon.bunyedit.util.Reorient;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.helper.Axis;
import net.minecraft.core.world.chunk.ChunkPosition;

public class FlipAction extends ICommandAction {

    public int apply(@Nonnull Player player, BunyAxis bunyAxis) 
    throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);
        
        Axis axis = bunyAxis.getDirection(new LookDirection(player));

        ChunkPosition s1 = selection.getPrimary();
        ChunkPosition s2 = selection.getSecondary();

        int[] s1a = PosMath.toArray(s1);
        int[] s2a = PosMath.toArray(s2);

        int side1 = s1a[axis.ordinal()];
        int side2 = s2a[axis.ordinal()];

        BlockBuffer before = selection.copy(false);
        BlockBuffer after = new BlockBuffer();

        Axis axis_ = axis;
        BlockBuffer copyBuffer = selection.copy(false);
        copyBuffer.forEach((pos, blockData) -> {
            blockData = Reorient.flipped(blockData, axis_);

            int[] posa = PosMath.toArray(pos);
            posa[axis_.ordinal()] = side2 - (posa[axis_.ordinal()] - side1);
            pos = PosMath.fromArray(posa);

            after.placeRaw(player.world, pos, blockData);
        });
        after.finalize(player.world);
        
        playerData.getUndoTape(player.world).push(before, after);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/flip")
            .executes(PermissionedCommand
                .process(c -> apply(
                    c.getSource().getSender(),
                    BunyAxis.playerDirection()
                ))
            )
            .then(ArgumentBuilderRequired
                .<CommandSource, BunyAxis>argument(
                    "axis", new ArgumentTypeBunyAxis()
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("axis", BunyAxis.class)
                    ))
                )
            )
        );
    }
}
