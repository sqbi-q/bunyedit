package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.command.action.arguments.ArgumentTypeCoords;
import fun.raccoon.bunyedit.command.action.arguments.Coords;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class MoveSelAction extends ICommandAction {

    public int apply(@Nonnull Player player, Coords position) 
    throws CommandSyntaxException {

        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);
        
        ChunkPosition origin = position.asAbsolute(
            selection.getPrimary(), 
            new LookDirection(player)
        );

        playerData.selection.setPrimary(player.world, origin);
        playerData.selection.setSecondary(player.world, PosMath.add(origin, PosMath.sub(selection.getSecondary(), selection.getPrimary())));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/movesel")
            .then(ArgumentBuilderRequired
                .<CommandSource, Coords>argument(
                    "position", new ArgumentTypeCoords()
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("position", Coords.class)
                    ))
                )
            )
        );
    }
}
