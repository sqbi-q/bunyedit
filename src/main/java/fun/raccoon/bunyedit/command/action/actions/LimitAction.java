package fun.raccoon.bunyedit.command.action.actions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentTypeLong;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.action.ICommandAction;
import fun.raccoon.bunyedit.data.PlayerData;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public class LimitAction extends ICommandAction {

    public int sendCurrentLimit(@Nonnull Player player) {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);

        // TODO message when selectionLimit == null
        player.sendMessage(i18n.translateKeyAndFormat(
            "bunyedit.cmd.limit.print", playerData.selectionLimit
        ));

        return Command.SINGLE_SUCCESS;
    }

    public int apply(@Nonnull Player player, @Nullable Long newLimit) throws CommandSyntaxException {
        I18n i18n = I18n.getInstance();
        PlayerData playerData = PlayerData.get(player);

        playerData.selectionLimit = newLimit;

        player.sendMessage(i18n.translateKey(
            "bunyedit.cmd.limit.success"
        ));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/limit")
            .executes(PermissionedCommand
                .process(
                    c -> sendCurrentLimit(c.getSource().getSender())
                )
            )
            .then(ArgumentBuilderRequired
                .<CommandSource, Long>argument(
                    "new-limit-or-no", ArgumentTypeLong.longArg(0)
                )
                .executes(PermissionedCommand
                    .process(c -> apply(
                        c.getSource().getSender(),
                        c.getArgument("new-limit-or-no", Long.class)
                    ))
                )
            )
            .then(ArgumentBuilderLiteral
                .<CommandSource>literal("no")
                .executes(PermissionedCommand
                    .process(
                        c -> apply(
                            c.getSource().getSender(),
                            null
                        )
                    )
                )
            )
        );
    }
}
