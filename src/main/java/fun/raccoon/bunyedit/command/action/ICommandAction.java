package fun.raccoon.bunyedit.command.action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.BunyEdit;
import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.selection.Selection;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.player.gamemode.Gamemode;
import net.minecraft.core.net.command.CommandManager.CommandRegistry;

public abstract class ICommandAction implements CommandRegistry {

    // public abstract int apply(...);
    
    /** Assert permissions before command execution, without using `.requires()`. */
    public interface PermissionedCommand extends Command<CommandSource> {
        static PermissionedCommand process(Command<CommandSource> func) {
            return (context) -> {
                ICommandAction.assertPermission(context);
                return func.run(context);
            };
        }
    }


    static public boolean isPermitted(CommandSource cmdSource) 
    throws CommandSyntaxException {
        @Nullable Player sender = cmdSource.getSender();

        if (sender == null) {
            throw CommandExceptions.NOT_A_PLAYER.create();
        }

        final boolean permsOnCreative = 
            sender.gamemode.equals(Gamemode.creative) 
            && BunyEdit.ALLOWED_CREATIVE;
        
        final boolean permsOnSurvival =
            sender.gamemode.equals(Gamemode.survival)
            && BunyEdit.ALLOWED_SURVIVAL;

        return cmdSource.hasAdmin() || permsOnCreative || permsOnSurvival;
    }

    static public void assertPermission(CommandContext<CommandSource> context) 
    throws CommandSyntaxException {
        if (!isPermitted(context.getSource())) {
            throw CommandExceptions.INSUFFICIENT_PERMS.create();
        }
    }


    static public ValidSelection validSelectionFrom(@Nonnull Player selector) 
    throws CommandSyntaxException {
        final PlayerData playerData = PlayerData.get(selector);
        final Selection selection = playerData.selection;
        final Long limit = playerData.selectionLimit;

        ValidSelection validSelection = ValidSelection.fromSelection(selection);
        if (validSelection == null || !selection.getWorld().equals(selector.world)) {
            throw CommandExceptions.INCOMPLETE_SELECTION.create();
        }

        if (limit != null) {
            long vol = validSelection.coordStream().count();
            if (vol > limit) {
                throw CommandExceptions.SELECTION_TOO_LARGE.formatAndCreate(vol);
            }
        }

        return validSelection;
    }
    
    
    @Override
    public abstract void register(CommandDispatcher<CommandSource> commandDispatcher);
}