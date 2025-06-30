package fun.raccoon.bunyedit.command.action.arguments.block;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import fun.raccoon.bunyedit.data.buffer.BlockData;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.net.command.util.CommandHelper;

public class BunyBlockArgumentParser /*extends BlockArgumentParser*/ {
    private StringReader reader;
    private final char METADATA_DELIM = ':';
    
    // From BlockArgumentParser
    private static final SimpleCommandExceptionType INVALID_BLOCK = new SimpleCommandExceptionType(() -> {
      return I18n.getInstance().translateKey("command.argument_types.block.invalid_block");
   });

    public BunyBlockArgumentParser(StringReader reader) {
        this.reader = reader;
    }

    //// Component parse methods
    // meta after colon, tag-less
    public BlockData parsePattern() throws CommandSyntaxException {
        // TODO suggestions
        @Nullable Block<?> block = parseBlock();
        int meta = 0;

        if (block != null && reader.canRead() && reader.peek() == METADATA_DELIM) {
            meta = parseMetadata();
        }

        return new BlockData(((block == null) ? 0 : block.id()), meta, null);
    }

    public Predicate<BlockData> parseFilter() throws CommandSyntaxException {
        // TODO suggestions
        // #<material> handling
        boolean hasMaterial = (reader.canRead() && reader.peek() == '#');
        if (hasMaterial) {
            return parseMaterial();
        }

        Predicate<Block<?>> blocks = parseFuzzyBlock();

        boolean hasMetadata = (reader.canRead() && reader.peek() == METADATA_DELIM);
        Predicate<Integer> metas = (hasMetadata) 
            ? parseFuzzyMetadata() 
            : meta -> true;

        return block -> blocks.test(block.getBlock()) && metas.test(block.meta);
    }
    ////


    //// Block
    private @Nullable Block<?> parseBlock() throws CommandSyntaxException {
        // input is numerical id
        if (reader.canRead() && !isValidKeyStringCharacter(reader.peek())) {
            int id = reader.readInt();
            if (id < 0)  throw INVALID_BLOCK.createWithContext(reader);
            if (id == 0) return null; // air
            
            // TODO on overflow it will use default exception message, consider throwing custom one
            return Blocks.getBlock(id);
        }

        // translation key of block 
        String blockInputKey = parseBlockKey();
        
        if (CommandHelper.matchesKeyString("air", blockInputKey)) {
            return null;
        }

        for (Block<?> block : Blocks.blocksList) {
            if (block == null) continue; // air was already handled

            // may be problematic because function reduces dots from checkedString
            // this way it deletes the block.getKey()'s 'tile.' part!
            if (CommandHelper.matchesKeyString(block.getKey(), blockInputKey)) {
                return block;
            }
        }

        throw INVALID_BLOCK.createWithContext(reader);
    }


    private Predicate</*Nullable*/ Block<?>> parseFuzzyBlock() throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw CommandExceptions.incomplete().create();
        }
        
        // input is numerical id range
        if (isRangeToken(reader) || !isValidKeyStringCharacter(reader.peek())) {
            Predicate<Integer> ids = parseRange(0, Blocks.blocksList.length);
            
            return block -> (block == null)
                ? ids.test(0) // required null-guard for air
                : ids.test(block.id());
        }

        // translation key of block
        String blockInputKey = parseBlockKey();

        if (CommandHelper.matchesKeyString("air", blockInputKey)) {
            return block -> block == null; // checks if is air
        }
        
        // semicolon delimitier, e.g. `grass;` won't match `grass.retro`
        boolean isExactKeyMatch = (reader.canRead() && reader.peek() == ';');
        if (isExactKeyMatch) {
            reader.skip(); // skip semicolon
        }

        // add "tile." because all translations key of blocks have it as prefix 
        String matchInputPrefix = (blockInputKey.startsWith("tile.")) 
            ? blockInputKey
            : "tile." + blockInputKey;

        // looks at prefix, instead of by parts split by dots of key, in fuzzy search
        Set<Integer> matchedBlockIds = Arrays.stream(Blocks.blocksList)
            .filter(block -> block != null) // air was already handled, filter it out
            .filter(block -> {
                if (isExactKeyMatch) {
                    return block.getKey().equals(matchInputPrefix);
                }
                return block.getKey().startsWith(matchInputPrefix);
            })
            .map(block -> block.id())
            .collect(Collectors.toSet());
        
        if (matchedBlockIds.size() == 0) {
            // TODO rename to avoid name collision
            // TODO throw more informative exceptions (e.g. "filter doesn't include any block") 
            throw fun.raccoon.bunyedit.command.CommandExceptions
                .INVALID_FILTER.create();
        }

        return block -> (block == null)
            ? matchedBlockIds.contains(0) // required null-guard for air
            : matchedBlockIds.contains(block.id());
    }


    private String parseBlockKey() {
        String blockInputKey = new String();

        // handle translation key input
        while (reader.canRead()) {
            char ch = reader.peek();

            if (!isValidKeyStringCharacter(ch)) break;
            blockInputKey += ch;

            reader.skip();
        }

        return blockInputKey;
    }


    private boolean isValidKeyStringCharacter(char ch) {
        return ch >= 'a' && ch <= 'z' || ch == '.';
    }
    ////


    //// Metadata
    private int parseMetadata() throws CommandSyntaxException {
        reader.skip(); // skip colon
        int metadata = reader.readInt();
        int cursor = reader.getCursor();

        if (metadata < 0) {
            reader.setCursor(cursor); // Brigadier returns cursor like this...
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow()
                .createWithContext(reader, metadata, 0);
        }
        
        return metadata;
    }


    private Predicate<Integer> parseFuzzyMetadata() throws CommandSyntaxException {
        reader.skip(); // skip colon
        return parseRange(0, Blocks.blocksList.length);
    }
    ////
    

    //// Material
    private Predicate<BlockData> parseMaterial() throws CommandSyntaxException {
        reader.skip(); // skip '#'

        String materialInput = reader.readString();

        // when liquid, just allow every liquid
        if (materialInput.equals("liquid") || materialInput.equals("fluid")) {
            return blockData -> {
                @Nullable Block<?> block = blockData.getBlock();
                if (block == null) return false;
                return block.getMaterial().isLiquid();
            };
        }

        // using reflection instead of switch case compared to original
        // skips check for MaterialColor.snow, just uses Material.snow
        Field[] materialFields = Material.class.getDeclaredFields();

        Optional<Field> foundMaterialField = Arrays.stream(materialFields)
            .filter(f -> f.getType() == Material.class)
            .filter(f -> f.getName().equals(materialInput))
            .findFirst();

        if (!foundMaterialField.isPresent()) {
            throw fun.raccoon.bunyedit.command.CommandExceptions
                .INVALID_FILTER.create();
        }

        Material material;
        try {
            material = (Material) foundMaterialField.get().get(Material.class);
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return blockData -> {
            @Nullable Block<?> block = blockData.getBlock();
            if (block == null) {
                return Material.air == material;
            }
            return material == block.getMaterial();
        };
    }
    ////


    //// Ranges
    private Predicate<Integer> parseRange(Integer assertMin, Integer assertMax)
    throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '*') {
            reader.skip(); // skip asterisk
            return id -> true; // all wildcard
        }

        boolean isLeftUnboundToken = isRangeToken(reader);

        if (isLeftUnboundToken) {
            // left unbound range
            skipRangeToken(reader);
            int maxBound = parseRangeInt(reader);
            
            if (assertMax != null && maxBound > assertMax) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh()
                    .createWithContext(reader, maxBound, assertMax);
            }

            return id -> id <= maxBound;
        }

        int minBound = parseRangeInt(reader);

        if (assertMin != null && minBound < assertMin) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow()
                .createWithContext(reader, minBound, assertMin);
        }

        if (!isRangeToken(reader)) {
            // lacking '..', it's singular integer
            return id -> id == minBound;
        }

        skipRangeToken(reader);
       
        if (reader.canRead() && StringReader.isAllowedNumber(reader.peek())) {
            // bound range
            int maxBound = parseRangeInt(reader);
            
            if (assertMax != null && maxBound > assertMax) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh()
                    .createWithContext(reader, maxBound, assertMax);
            }

            return id -> id >= minBound && id <= maxBound;
        }

        // right unbound range
        return id -> id >= minBound;
    }


    private static boolean isRangeToken(StringReader reader) {
        int start = reader.getCursor();
        String chars = reader.getString().substring(start);
        return chars.startsWith("..");
    }

    private static void skipRangeToken(StringReader reader) {
        int start = reader.getCursor();
        reader.setCursor(start+2);
    }

    private static int parseRangeInt(StringReader reader)
    throws CommandSyntaxException {
        int start = reader.getCursor();
        int len = 0;

        while (reader.canRead() && Character.isDigit(reader.peek())) {
            len++;
            reader.skip();
        }

        if (len == 0) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                .readerExpectedInt().createWithContext(reader);
        }

        String digits = reader.getString().substring(start, start+len);
        return Integer.parseInt(digits);
    }
    ////
}
