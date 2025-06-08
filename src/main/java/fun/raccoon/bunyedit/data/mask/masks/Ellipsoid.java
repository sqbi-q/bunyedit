package fun.raccoon.bunyedit.data.mask.masks;

import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.world.chunk.ChunkPosition;

public class Ellipsoid implements IMaskCommand {
    private static boolean inside(
        double a, double b, double c,
        double x, double y, double z
    ) {
        return (x*x)/(a*a) + (y*y)/(b*b) + (z*z)/(c*c) <= 1;
    }
    
    private static Stream<Integer> rangeClosed(int from, int to) {
        return IntStream.rangeClosed(Math.min(from, to), Math.max(from, to)).boxed();
    }

    public String usage() {
        return "[h]";
    }

    public @Nonnull BiPredicate<ValidSelection, ChunkPosition> build(String[] argv) throws CommandSyntaxException {
        switch (argv.length) {
            case 0:
                return p(false);
            case 1:
                if (!argv[0].equals("h")) {
                    throw CommandExceptions.INVALID_HOLLOW.create();
                    
                }
                return p(true);
            default:
                throw CommandExceptions.TOO_MANY_ARGS.create();
        }
    }

    public static @Nonnull BiPredicate<ValidSelection, ChunkPosition> p(boolean hollow) {
        return (selection, pos) -> {
            ChunkPosition s1 = selection.getPrimary();
            ChunkPosition s2 = selection.getSecondary();

            double a = Math.abs(s2.x - s1.x)/2.0+0.5;
            double b = Math.abs(s2.y - s1.y)/2.0+0.5;
            double c = Math.abs(s2.z - s1.z)/2.0+0.5;

            double cx = (s1.x + s2.x)/2.0;
            double cy = (s1.y + s2.y)/2.0;
            double cz = (s1.z + s2.z)/2.0;

            double x = pos.x - cx;
            double y = pos.y - cy;
            double z = pos.z - cz;

            if (!hollow) {
                return inside(a, b, c, x, y, z);
            } else {
                boolean exposed = rangeClosed(-1, 1)
                    .flatMap(dx -> rangeClosed(-1, 1)
                        .flatMap(dy -> rangeClosed(-1, 1)
                            .filter(dz -> dx == 0 || dy == 0 || dz == 0)
                            .filter(dz -> !inside(a, b, c, x+dx, y+dy, z+dz))))
                    .anyMatch(p -> true);

                return inside(a, b, c, x, y, z) && exposed;
            }
        };
    }
}
