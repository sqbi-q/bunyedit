package fun.raccoon.bunyedit.command;

import net.minecraft.core.lang.I18n;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import com.mojang.brigadier.LiteralMessage;

public final class CommandExceptions {

    public static final SimpleCommandExceptionType TOO_MANY_ARGS 
        = fromI18n("bunyedit.cmd.err.toomanyargs");
    public static final SimpleCommandExceptionType INVALID_HOLLOW
        = fromI18n("bunyedit.cmd.err.invalidhollow");
    public static final SimpleCommandExceptionType NOT_A_PLAYER
        = fromI18n("bunyedit.cmd.err.notaplayer");
    public static final SimpleCommandExceptionType INSUFFICIENT_PERMS
        = fromI18n("bunyedit.cmd.err.insufficientperms");
    public static final SimpleCommandExceptionType INCOMPLETE_SELECTION
        = fromI18n("bunyedit.cmd.err.incompleteselection");
    public static final SimpleCommandExceptionType TOO_FEW_ARGS
        = fromI18n("bunyedit.cmd.err.toofewargs");
    public static final SimpleCommandExceptionType INVALID_DIRECTION
        = fromI18n("bunyedit.cmd.err.invaliddirection");
    public static final SimpleCommandExceptionType INVALID_AXIS
        = fromI18n("bunyedit.cmd.err.invalidaxis");
    public static final SimpleCommandExceptionType INVALID_FILTER
        = fromI18n("bunyedit.cmd.err.invalidfilter");
    public static final SimpleCommandExceptionType INVALID_PATTERN
        = fromI18n("bunyedit.cmd.err.invalidpattern");
    public static final SimpleCommandExceptionType INVALID_BOUND
        = fromI18n("bunyedit.cmd.err.invalidbound");
    public static final SimpleCommandExceptionType INVALID_NUMBER
        = fromI18n("bunyedit.cmd.err.invalidnumber");
    public static final SimpleCommandExceptionType INVALID_COORDS
        = fromI18n("bunyedit.cmd.err.invalidcoords");

    public static final FormatCommandExceptionType SELECTION_TOO_LARGE = (Object[] args) -> 
        { return fromI18nWithFormat("bunyedit.cmd.err.selectiontoolarge", args).create(); }; 
    public static final FormatCommandExceptionType NO_SUCH_MASK = (Object[] args) -> 
        { return fromI18nWithFormat("bunyedit.cmd.mask.err.nosuchmask", args).create(); };
    public static final FormatCommandExceptionType NO_PAGES = (Object[] args) ->
        { return fromI18nWithFormat("bunyedit.cmd.undoredo.err.nopages", args).create(); };

    @FunctionalInterface
    public interface FormatCommandExceptionType {
        CommandSyntaxException formatAndCreate(Object... args);
    };


    public static SimpleCommandExceptionType fromString(String raw) {
        LiteralMessage msg = new LiteralMessage(raw);
        return new SimpleCommandExceptionType(msg);
    }

    public static SimpleCommandExceptionType fromI18n(String key) {
        I18n i18n = I18n.getInstance();
        String str = i18n.translateKey(key);
        LiteralMessage msg = new LiteralMessage(str);

        return new SimpleCommandExceptionType(msg);
    }

    public static SimpleCommandExceptionType fromI18nWithFormat(String key, Object... args) {
        I18n i18n = I18n.getInstance();
        String str = i18n.translateKeyAndFormat(key, args);
        LiteralMessage msg = new LiteralMessage(str);

        return new SimpleCommandExceptionType(msg);
    }
};