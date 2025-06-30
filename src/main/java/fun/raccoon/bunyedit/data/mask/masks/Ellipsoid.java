package fun.raccoon.bunyedit.data.mask.masks;

import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.mojang.brigadier.arguments.ArgumentTypeBool;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction.PermissionedCommand;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class Ellipsoid implements IMaskCommand {

    private class EllipsoidArguments extends Arguments {
        @Override
        public Arguments handleContext(CommandContext<CommandSource> ctx) {
            boolean isHollow = false;
            try {
                isHollow = ctx.getArgument("is-hollow", Boolean.class);
            } 
            catch (IllegalArgumentException e) {}
            
            argsMap.put("is-hollow", isHollow);
            if (isHollow) argsInlineName += "hollow ";

            return this;
        }
    }
    @Override
    public Arguments getArguments(CommandContext<CommandSource> ctx) {
        return new EllipsoidArguments().handleContext(ctx);
    }


    @Override
    public ArgumentBuilderLiteral<CommandSource> addToCommandBuilder(
        String literalName,
        ArgumentBuilderLiteral<CommandSource> builder,
        PermissionedCommand onExecute
    ) {
        return builder
            .then(getCommandLiteral(literalName)
                .executes(onExecute)
                .then(ArgumentBuilderRequired
                    .<CommandSource, Boolean>argument(
                        "is-hollow", ArgumentTypeBool.bool()
                    )
                    .executes(onExecute)
                )
            );
    }


    @Override
    public @Nonnull BiPredicate<ValidSelection, ChunkPosition> build(
        Arguments args
    ) throws CommandSyntaxException {

        boolean isHollow = (Boolean) args.argsMap.getOrDefault(
            "is-hollow", false
        );

        return p(isHollow);
    }


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
