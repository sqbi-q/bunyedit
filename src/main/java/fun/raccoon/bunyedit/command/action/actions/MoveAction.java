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
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.buffer.WorldBuffer;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

    // TODO: actually implement entity copying
public class MoveAction extends ICommandAction {

    public int apply(@Nonnull Player player, Coords position)
    throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        ChunkPosition copyOrigin = selection.getPrimary();
        ChunkPosition pasteOrigin = position.asAbsolute(copyOrigin, new LookDirection(player));

        BlockData air = new BlockData();

        WorldBuffer before = selection.copyWorld(false);
        WorldBuffer after = new WorldBuffer();

        

        // three seperate loops to obviate previous bug where copy and paste positions overlap

        // populate undo
        BlockBuffer copyBuffer = selection.copy(true);
        copyBuffer.forEach((pos, blockData) -> {
            ChunkPosition copyPos = PosMath.add(pos, copyOrigin);
            ChunkPosition pastePos = PosMath.add(pos, pasteOrigin);

            before.blocks.put(copyPos, blockData);
            before.blocks.put(pastePos, new BlockData(player.world, pastePos));
        });

        // clear
        copyBuffer.forEach((pos, blockData) -> {
            ChunkPosition copyPos = PosMath.add(pos, copyOrigin);

            after.blocks.placeRaw(player.world, copyPos, air);
        });

        // paste
        copyBuffer.forEach((pos, blockData) -> {
            ChunkPosition pastePos = PosMath.add(pos, pasteOrigin);

            after.blocks.placeRaw(player.world, pastePos, blockData);
        });
        after.blocks.finalize(player.world);
        
        playerData.getUndoTape(player.world).push(before, after);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/move")
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
