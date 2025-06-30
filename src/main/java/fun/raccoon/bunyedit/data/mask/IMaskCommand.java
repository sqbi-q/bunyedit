package fun.raccoon.bunyedit.data.mask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction.PermissionedCommand;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public interface IMaskCommand {
    /**
     * Storage and retrieve class for arguments for masks from command context.
     */
    abstract class Arguments {
        public String argsInlineName = "";
        public Map<String, Object> argsMap = new HashMap<>();

        /**
         * Retrives map of argument to its value from command context; 
         * and string for mask argument names, used to show inline 
         * with mask name, e.g. "[cube] hollow otherarg:20 [...]".
         * <p>
         * Both retrieves are combined this way to make code more compact for 
         * overriding classes. CommandContext doesn't provide arguments map access, 
         * only getArgument method, which throws on argument not found.
         * <p>
         * As of today, there is no better way to make optional arguments using Brigadier API 
         * (https://github.com/Mojang/brigadier/issues/110).
         * 
         * @param ctx from which arguments are retrieved
         * @returns this instance for simpler access in IMaskCommand.getArguments()
         */
        public Arguments handleContext(CommandContext<CommandSource> ctx) { return this; }
    }
    public Arguments getArguments(CommandContext<CommandSource> ctx);


    public String usage();


    public ArgumentBuilderLiteral<CommandSource> addToCommandBuilder(
        String literalName,
        ArgumentBuilderLiteral<CommandSource> builder,
        PermissionedCommand onExecute
    );

    public @Nonnull BiPredicate<ValidSelection, ChunkPosition> build(
        Arguments args
    ) throws CommandSyntaxException;


    default ArgumentBuilderLiteral<CommandSource> getCommandLiteral(String literalName) {
        return ArgumentBuilderLiteral.literal(literalName);
    }
}
