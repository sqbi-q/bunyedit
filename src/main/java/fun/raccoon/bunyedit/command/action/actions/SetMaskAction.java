package fun.raccoon.bunyedit.command.action.actions;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeString;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
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

    public int apply(@Nonnull Player player, String listOrType, String maskArg) 
    throws CommandSyntaxException {

        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);

        // TODO listOrType = <mask-type> | "list"
        if (listOrType.equals("list")) {
            player.sendMessage(String.format("%s:",
                i18n.translateKey("bunyedit.cmd.mask.list.header")));
            
            for (Entry<String, IMaskCommand> entry : Masks.MASKS.entrySet()) {
                player.sendMessage(String.format("%s %s",
                    entry.getKey(),
                    entry.getValue().usage()));
            }
            
            return Command.SINGLE_SUCCESS;
        }

        // TODO add flag argument to every mask (mask as subcommand)
        
        @Nullable IMaskCommand maskCmd = Masks.MASKS.get(listOrType);
        if (maskCmd == null) {
            throw CommandExceptions.NO_SUCH_MASK.formatAndCreate(listOrType);
        }

        String[] maskArgv = {};
        /*
        if (argv.size() >= 1) {
            maskArgv = argv.subList(1, argv.size()).toArray(new String[0]);
        }
        */
        if (maskArg != null) {
            maskArgv = maskArg.split(" ");
        }

        BiPredicate<ValidSelection, ChunkPosition> mask = maskCmd.build(maskArgv);

        playerData.selection.setMask(listOrType+" "+Arrays.stream(maskArgv).collect(Collectors.joining(" ")), mask);

        player.sendMessage(i18n.translateKey("bunyedit.cmd.mask.success"));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        // TODO neater way to handle argument layers

        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/mask")
            .executes(PermissionedCommand
                .process(
                    c -> sendCurrentMask(c.getSource().getSender())
                )
            )
            .then(ArgumentBuilderRequired
                .<CommandSource, String>argument(
                    "list-or-type", ArgumentTypeString.string()
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("list-or-type", String.class),
                        null
                    ))
                )
                .then(ArgumentBuilderRequired
                    .<CommandSource, String>argument(
                        "mask-arg", ArgumentTypeString.greedyString()
                    )
                    .executes(PermissionedCommand
                        .process(c -> apply(
                            c.getSource().getSender(),
                            c.getArgument("list-or-type", String.class),
                            c.getArgument("mask-arg", String.class)
                        ))
                    )
                )
            )
        );
    }
}
