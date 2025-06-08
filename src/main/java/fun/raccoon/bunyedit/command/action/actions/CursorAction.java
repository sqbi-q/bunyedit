package fun.raccoon.bunyedit.command.action.actions;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.Cursor;
import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.IPlayerAction;
import fun.raccoon.bunyedit.data.PlayerData;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.CommandManager.CommandRegistry;

public class CursorAction implements CommandRegistry, IPlayerAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, List<String> argv
    ) throws CommandSyntaxException {
        if (argv.size() > 0) {
            throw CommandExceptions.TOO_MANY_ARGS.create();
        }

        player.inventory.insertItem(Cursor.getCursorItem(), true);

        return true;
    }

    @Override
    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        
        commandDispatcher.register(ArgumentBuilderLiteral
            .<CommandSource>literal("/cursor")
            .executes(c -> {
                Player player = c.getSource().getSender();

                apply(
                    I18n.getInstance(), c.getSource(), c.getSource().getSender(),
                    PlayerData.get(player), new ArrayList<>()
                );

                return 1;
            })
        );
    }
}
