package fun.raccoon.bunyedit.data.mask.masks;

import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.world.chunk.ChunkPosition;

public class Cuboid implements IMaskCommand {
    public String usage() {
        return "[h]";
    }

    public @Nonnull BiPredicate<ValidSelection, ChunkPosition> build(String[] argv) 
        throws CommandSyntaxException {
        
        switch (argv.length) {
            case 0:
                return (selection, pos) -> true;
            case 1:
                if (!argv[0].equals("h")) {
                    throw CommandExceptions.INVALID_HOLLOW.create();
                }
                return (selection, pos) -> {
                    ChunkPosition s1 = selection.getPrimary();
                    ChunkPosition s2 = selection.getSecondary();

                    return pos.x == s1.x || pos.x == s2.x || pos.y == s1.y || pos.y == s2.y || pos.z == s1.z || pos.z == s2.z;
                };
            default:
                throw CommandExceptions.TOO_MANY_ARGS.create();
        }
    }
}
