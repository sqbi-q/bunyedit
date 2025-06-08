package fun.raccoon.bunyedit.data.mask.masks;

import java.util.HashSet;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.mask.IterativeMask;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.world.chunk.ChunkPosition;

public class Line implements IMaskCommand {
    private class LineInner extends IterativeMask {
        @Override
        public void fillCache(HashSet<ChunkPosition> cache, ValidSelection selection) {

            // bresenham algo i took from this stackoverflow answer
            // https://stackoverflow.com/a/50516870

            ChunkPosition s1 = selection.getPrimary();
            ChunkPosition s2 = selection.getSecondary();

            float dx = s2.x - s1.x;
            float dy = s2.y - s1.y;
            float dz = s2.z - s1.z;

            float deltaErrorY = Math.abs(dy / dx);
            float deltaErrorZ = Math.abs(dz / dx);

            float errorY = 0;
            float errorZ = 0;

            int y = s1.y;
            int z = s1.z;

            for (int x = s1.x; x < s2.x; x++) { 
                cache.add(new ChunkPosition(x, y, z));
                errorY += deltaErrorY;
                while (errorY >= 0.5) {
                     y += Math.signum(dy);
                     errorY--;
                }
                errorZ += deltaErrorZ;
                while (errorZ >= 0.5) {
                    z += Math.signum(dz);
                    errorZ--;
                }
            }
        }
    }

    public String usage() {
        return "";
    }

    public @Nonnull BiPredicate<ValidSelection, ChunkPosition> build(String[] argv) throws CommandSyntaxException {
        if (argv.length > 0) {
            throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        return new LineInner();
    }
}
