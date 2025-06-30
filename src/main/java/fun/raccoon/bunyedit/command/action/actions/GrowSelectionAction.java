package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.command.action.arguments.bound.ArgumentTypeBound;
import fun.raccoon.bunyedit.command.action.arguments.bound.Bound;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.chunk.ChunkPosition;

public class GrowSelectionAction extends ICommandAction {

    public int apply(@Nonnull Player player, Bound bound) 
    throws CommandSyntaxException {

        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);
        
        Pair<ChunkPosition, ChunkPosition> growBy = bound.getOffsets(
            selection, new LookDirection(player)
        );
        playerData.selection.setBound(growBy);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/growsel")
            .executes(PermissionedCommand
                .process(c -> apply(
                    c.getSource().getSender(),
                    new Bound().all(1) // "*1"
                ))
            )
            .then(ArgumentBuilderRequired
                .<CommandSource, Bound>argument(
                    "grow-bound", new ArgumentTypeBound()
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("grow-bound", Bound.class)
                    ))
                )
            )
        );
    }
}
