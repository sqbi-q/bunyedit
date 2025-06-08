package fun.raccoon.bunyedit.command.action;

import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.CommandSource;

public interface IAction {
    public boolean apply(I18n i18n, CommandSource cmdSource, List<String> argv) 
        throws CommandSyntaxException;
}