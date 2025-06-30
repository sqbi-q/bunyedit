package fun.raccoon.bunyedit.data.mask.masks;

import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import fun.raccoon.bunyedit.command.action.ICommandAction.PermissionedCommand;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.selection.ValidSelection;

import com.mojang.brigadier.arguments.ArgumentTypeBool;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class Cuboid implements IMaskCommand {

    private class CuboidArguments extends Arguments {
        @Override
        public Arguments handleContext(CommandContext<CommandSource> ctx) {
            // Can't simply check if there is argument in context so need to use try-catch
            boolean isHollow = false;
            try {
                isHollow = ctx.getArgument("is-hollow", Boolean.class);
            } 
            catch (IllegalArgumentException e) {} // losing one meaningful exception about wrong class casting this way...
            
            argsMap.put("is-hollow", isHollow);
            if (isHollow) {
                argsInlineName += "hollow ";
            }

            return this;
        }
    };
    
    @Override
    public Arguments getArguments(CommandContext<CommandSource> ctx) {
        return new CuboidArguments().handleContext(ctx);
    }


    @Override
    public ArgumentBuilderLiteral<CommandSource> addToCommandBuilder(
        String literalName,
        ArgumentBuilderLiteral<CommandSource> builder,
        PermissionedCommand onExecute
    ) {
        ArgumentBuilderLiteral<CommandSource> cuboidBuilder = builder
            .then(getCommandLiteral(literalName)
                .executes(onExecute)
                .then(
                    ArgumentBuilderRequired
                    .<CommandSource, Boolean>argument(
                        "is-hollow", ArgumentTypeBool.bool()
                    )
                    .executes(onExecute)
                )
            );

        return cuboidBuilder;
    }


    public String usage() {
        return "[h]";
    }

    
    @Override
    public @Nonnull BiPredicate<ValidSelection, ChunkPosition> build(
        Arguments args
    ) throws CommandSyntaxException {

        Boolean isHollow = (Boolean) args.argsMap.getOrDefault(
            "is-hollow", false
        );

        if (!isHollow) {
            return (selection, pos) -> true; // every position is good for cube
        }

        return (selection, pos) -> {
            ChunkPosition s1 = selection.getPrimary();
            ChunkPosition s2 = selection.getSecondary();

            return pos.x == s1.x || pos.x == s2.x || pos.y == s1.y || pos.y == s2.y || pos.z == s1.z || pos.z == s2.z;
        };
    }
}
