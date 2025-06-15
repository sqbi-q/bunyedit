package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class PasteAction extends ICommandAction {

    public int apply(@Nonnull Player player) throws CommandSyntaxException {
        
        PlayerData playerData = PlayerData.get(player);
        ValidSelection selection = validSelectionFrom(player);

        ChunkPosition origin = selection.getPrimary();
            
        BlockBuffer before = new BlockBuffer();
        playerData.copyBuffer.forEach((pos, blockData) -> {
            pos = PosMath.add(pos, origin);
            before.put(pos, new BlockData(player.world, pos));
        });

        BlockBuffer after = new BlockBuffer();
        playerData.copyBuffer.forEach((pos, blockData) -> {
            pos = PosMath.add(pos, origin);
            after.placeRaw(player.world, pos, blockData);
        });
        after.finalize(player.world);
        
        playerData.getUndoTape(player.world).push(before, after);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/paste")
            .executes(PermissionedCommand
                .process(
                    c -> apply(c.getSource().getSender()
                ))
            )
        );
    }
}
