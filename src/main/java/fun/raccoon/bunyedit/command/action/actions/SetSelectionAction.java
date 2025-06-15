package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.Selection.Slot;
import fun.raccoon.bunyedit.util.ChatString;
import fun.raccoon.bunyedit.util.parsers.RelCoords;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.arguments.ArgumentTypeIntegerCoordinates;
import net.minecraft.core.net.command.helpers.IntegerCoordinates;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetSelectionAction extends ICommandAction {

    public int apply(@Nonnull Player player, Slot slot, ChunkPosition position) {
        PlayerData playerData = PlayerData.get(player);
        playerData.selection.set(slot, player.world, position);

        player.sendMessage(ChatString.gen_select_action(slot, player.world, position));

        return Command.SINGLE_SUCCESS;
    }

    private int applyCommandOnSlot(CommandContext<CommandSource> context, Slot slot)
    throws CommandSyntaxException {
        assertPermission(context);

        Player player = context.getSource().getSender();
        ChunkPosition pos = RelCoords.playerPos(player, false);
        
        return apply(player, slot, pos);
    }

    private int applyCommandOnSlot(
        CommandContext<CommandSource> context, Slot slot, IntegerCoordinates position
    ) throws CommandSyntaxException {
        assertPermission(context);

        Player player = context.getSource().getSender();

        // TODO port local coordinates (^) for type coords
        ChunkPosition pos = new ChunkPosition(
            position.getX(player.x),
            position.getY(player.y),
            position.getZ(player.z)
        );

        return apply(player, slot, pos);
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/1")
            .executes(
                c -> applyCommandOnSlot(c, Slot.PRIMARY)
            )
            .then(
                ArgumentBuilderRequired
                .<CommandSource, IntegerCoordinates>argument(
                    "position", ArgumentTypeIntegerCoordinates.intCoordinates()
                )
                .executes(c -> applyCommandOnSlot(c, Slot.PRIMARY,
                    c.getArgument("position", IntegerCoordinates.class))
                )
            )
        );

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/2")
            .executes(
                c -> applyCommandOnSlot(c, Slot.SECONDARY)
            )
            .then(
                ArgumentBuilderRequired
                .<CommandSource, IntegerCoordinates>argument(
                    "position", ArgumentTypeIntegerCoordinates.intCoordinates()
                )
                .executes(c -> applyCommandOnSlot(c, Slot.SECONDARY,
                    c.getArgument("position", IntegerCoordinates.class))
                )
            )
        );
    }
}
