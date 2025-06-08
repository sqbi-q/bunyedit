package fun.raccoon.bunyedit.command.action.actions;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ISelectionAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.buffer.BlockBuffer;
import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.data.look.LookDirection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import fun.raccoon.bunyedit.util.DirectionHelper;
import fun.raccoon.bunyedit.util.PosMath;
import fun.raccoon.bunyedit.util.parsers.RelCoords;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.util.helper.Direction;
import net.minecraft.core.world.chunk.ChunkPosition;

public class StackAction implements ISelectionAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, ValidSelection selection, List<String> argv
    ) throws CommandSyntaxException {
        if (argv.size() > 3) {
            throw CommandExceptions.TOO_MANY_ARGS.create();
        }

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

        return true;
    }
}
