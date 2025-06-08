package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ISelectionAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.PosMath;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class PasteAction implements ISelectionAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, ValidSelection selection, List<String> argv
    ) throws CommandSyntaxException {
        if (argv.size() > 0) {
            throw CommandExceptions.TOO_MANY_ARGS.create();
        }

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

        return true;
    }
}
