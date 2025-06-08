package fun.raccoon.bunyedit.command.action.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fun.raccoon.bunyedit.command.CommandExceptions;
import fun.raccoon.bunyedit.command.action.IPlayerAction;
import fun.raccoon.bunyedit.data.PlayerData;
import fun.raccoon.bunyedit.data.mask.IMaskCommand;
import fun.raccoon.bunyedit.data.mask.Masks;
import fun.raccoon.bunyedit.data.selection.ValidSelection;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.world.chunk.ChunkPosition;

public class SetMaskAction implements IPlayerAction {
    @Override
    public boolean apply(
        I18n i18n, CommandSource cmdSource, @Nonnull Player player,
        PlayerData playerData, List<String> argv
    ) throws CommandSyntaxException {
        String maskName;

        Player sender = cmdSource.getSender();

        switch (argv.size()) {
            case 0:
                sender.sendMessage(String.format("%s: %s",
                    i18n.translateKey("bunyedit.cmd.mask.current"),
                    playerData.selection.getMaskName()));
                return true;
            default:
                maskName = argv.get(0);
                break;
        }

        if (maskName.equals("list")) {
            sender.sendMessage(String.format("%s:",
                i18n.translateKey("bunyedit.cmd.mask.list.header")));
            
            for (Entry<String, IMaskCommand> entry : Masks.MASKS.entrySet()) {
                sender.sendMessage(String.format("%s %s",
                    entry.getKey(),
                    entry.getValue().usage()));
            }
            
            return true;
        }

        @Nullable IMaskCommand maskCmd = Masks.MASKS.get(maskName);
        if (maskCmd == null) {
            throw CommandExceptions.NO_SUCH_MASK.formatAndCreate(maskName);
        }

        String[] maskArgv = {};
        if (argv.size() >= 1) {
            maskArgv = argv.subList(1, argv.size()).toArray(new String[0]);
        }

        BiPredicate<ValidSelection, ChunkPosition> mask = maskCmd.build(maskArgv);

        playerData.selection.setMask(maskName+" "+Arrays.stream(maskArgv).collect(Collectors.joining(" ")), mask);

        sender.sendMessage(i18n.translateKey("bunyedit.cmd.mask.success"));

        return true;
    }
}
