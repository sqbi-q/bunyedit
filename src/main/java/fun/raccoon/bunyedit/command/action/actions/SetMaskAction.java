package fun.raccoon.bunyedit.command.action.actions;

import java.util.Map.Entry;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.mask.Masks;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetMaskAction extends ICommandAction {

    public int sendCurrentMask(@Nonnull Player player) {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);

        player.sendMessage(String.format("%s: %s",
            i18n.translateKey("bunyedit.cmd.mask.current"),
            playerData.selection.getMaskName()
        ));

        return Command.SINGLE_SUCCESS;
    }

    public int sendMaskList(@Nonnull Player player) {
        I18n i18n = I18n.getInstance();

        player.sendMessage(String.format("%s:",
        i18n.translateKey("bunyedit.cmd.mask.list.header")));
            
        for (Entry<String, IMaskCommand> entry : Masks.MASKS.entrySet()) {
            player.sendMessage(String.format("%s %s",
                entry.getKey(),
                entry.getValue().usage()));
        }
        
        return Command.SINGLE_SUCCESS;
    }


    public int apply(CommandContext<CommandSource> ctx, String maskName)
    throws CommandSyntaxException {
        I18n i18n = I18n.getInstance();

        Player player = ctx.getSource().getSender();
        PlayerData playerData = PlayerData.get(player);

        @Nullable IMaskCommand maskCmd = Masks.MASKS.get(maskName);
        if (maskCmd == null) {
            throw CommandExceptions.NO_SUCH_MASK.formatAndCreate(maskName);
        }

        IMaskCommand.Arguments maskArgs = maskCmd.getArguments(ctx);
        BiPredicate<ValidSelection, ChunkPosition> mask = maskCmd.build(maskArgs);

        playerData.selection.setMask(maskName+" "+maskArgs.argsInlineName, mask);

        player.sendMessage(i18n.translateKey("bunyedit.cmd.mask.success"));

        return Command.SINGLE_SUCCESS;
    }


    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        // Handling of no arguments and `/mask list`
        ArgumentBuilderLiteral<CommandSource> maskBuilder = 
            ArgumentBuilderLiteral
                .<CommandSource>literal("/mask")
                .executes(PermissionedCommand
                    .process(
                        c -> sendCurrentMask(c.getSource().getSender())
                    )
                )
                .then(ArgumentBuilderLiteral
                    .<CommandSource>literal("list")
                    .executes(PermissionedCommand
                        .process(
                            c -> sendMaskList(c.getSource().getSender())
                        )
                    )
                );
        
        // Handling of each individual mask
        for (Entry<String, IMaskCommand> entry : Masks.MASKS.entrySet()) {
            String maskName = entry.getKey();
            IMaskCommand maskCommand = entry.getValue();

            maskBuilder = maskCommand.addToCommandBuilder(
                maskName, maskBuilder, PermissionedCommand.process(
                    c -> apply(c, maskName)
                )
            );
        }

        commandDispatcher.register(maskBuilder);
    }
}
