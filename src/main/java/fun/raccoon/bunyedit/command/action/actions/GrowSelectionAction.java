package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.parsers.Bound;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.chunk.ChunkPosition;

public class GrowSelectionAction extends ICommandAction {

    public int apply(@Nonnull Player player, String bound_argument) 
    throws CommandSyntaxException {

        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);
        
        Pair<ChunkPosition, ChunkPosition> growBy = Bound.fromString(
            selection, player, bound_argument
        );

        // TODO port Bound parser
        /*
            case 1:
                growBy = Bound.fromString(selection, player, argv.get(0));
                break;
        */

        if (growBy == null) {
            throw CommandExceptions.INVALID_BOUND.create();
        }

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
                    "*1"
                ))
            )
            .then(ArgumentBuilderRequired
                .<CommandSource, String>argument(
                    "grow-bound", ArgumentTypeString.string()
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("grow-bound", String.class)
                    ))
                )
            )
        );
    }
}
