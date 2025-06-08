package fun.raccoon.bunyedit.util;

import fun.raccoon.bunyedit.data.buffer.BlockData;
import fun.raccoon.bunyedit.mixin.BlockSignAccessor;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.BlockLogicBed;
import net.minecraft.core.block.BlockLogicButton;
import net.minecraft.core.block.BlockLogicChest;
import net.minecraft.core.block.BlockLogicFenceGate;
import net.minecraft.core.block.BlockLogicLadder;
import net.minecraft.core.block.BlockLogicLever;
import net.minecraft.core.block.BlockLogicPumpkin;
import net.minecraft.core.block.BlockLogicRail;
import net.minecraft.core.block.BlockLogicRepeater;
import net.minecraft.core.block.BlockLogicRotatable;
import net.minecraft.core.block.BlockLogicSign;
import net.minecraft.core.block.BlockLogicSlab;
import net.minecraft.core.block.BlockLogicStairs;
import net.minecraft.core.block.BlockLogicTorch;
import net.minecraft.core.block.BlockLogicTrapDoor;
import net.minecraft.core.block.piston.BlockLogicPistonBase;
import net.minecraft.core.block.piston.BlockLogicPistonHead;
import net.minecraft.core.util.helper.Axis;

// TODO check this code later because it smells - qf

public class Reorient {
    /**
     * @param blockData block to flip
     * @param axis axis to mirror along
     * @return
     * @throws NullPointerException if blockData is null
     */
    public static BlockData flipped(BlockData blockData, Axis axis) {
        Block<?> block = Blocks.getBlock(blockData.id);
        int meta = blockData.meta;

        if (block.getLogic() instanceof BlockLogicStairs) {
            if (axis.equals(Axis.X) && (meta&2)==0)
                meta ^= 1;
            if (axis.equals(Axis.Y))
                meta ^= 8;
            if (axis.equals(Axis.Z) && (meta&2)==2)
                meta ^= 1;
        } else if (block.getLogic() instanceof BlockLogicTrapDoor) {
            if (axis.equals(Axis.X) && (meta&2)==2)
                meta ^= 1;
            if (axis.equals(Axis.Y))
                meta ^= 8;
            if (axis.equals(Axis.Z) && (meta&2)==0)
                meta ^= 1;
        } else if (block.getLogic() instanceof BlockLogicSlab) {
            if (axis.equals(Axis.Y) && (meta&1)==0)
                meta ^= 2;
        } else if (
            block.getLogic() instanceof BlockLogicLadder
            || block.getLogic() instanceof BlockLogicPumpkin
            || block.getLogic() instanceof BlockLogicRotatable
        ) {
            if (axis.equals(Axis.X) && (meta&2)==0)
                meta ^= 1;
            if (axis.equals(Axis.Z) && (meta&2)==2)
                meta ^= 1;
        } else if (block.getLogic() instanceof BlockLogicFenceGate
            || block.getLogic() instanceof BlockLogicRepeater
            || block.getLogic() instanceof BlockLogicBed
            || block.getLogic() instanceof BlockLogicChest
        ) {
            if (axis.equals(Axis.X) && (meta&1)==1)
                meta ^= 2;
            if (axis.equals(Axis.Z) && (meta&1)==0)
                meta ^= 2;
        } else if (
            block.getLogic() instanceof BlockLogicPistonBase
            || block.getLogic() instanceof BlockLogicPistonHead
        ) {
            if (axis.equals(Axis.X) && (meta&6)==4)
                meta ^= 1;
            if (axis.equals(Axis.Y) && (meta&6)==0)
                meta ^= 1;
            if (axis.equals(Axis.Z) && (meta&6)==2)
                meta ^= 1;
        } else if (
            block.getLogic() instanceof BlockLogicTorch
            || block.getLogic() instanceof BlockLogicButton
            || block.getLogic() instanceof BlockLogicLever
        ) {
            switch (meta&7) {
                case 1:
                case 2:
                    if (axis.equals(Axis.X)) meta ^= 3; break;
                case 3:
                case 4:
                    if (axis.equals(Axis.Z)) meta ^= 7; break;
            }
        } else if (block.getLogic() instanceof BlockLogicRail) {
            if (axis.equals(Axis.X) && (meta&14)==2)
                meta ^= 1;
            if (axis.equals(Axis.Z) && (meta&14)==4)
                meta ^= 1;
            if (axis.equals(Axis.X) && (meta&14)>=6)
                meta ^= 1;
            if (axis.equals(Axis.Z) && (meta&14)>=6) {
                meta ^= 14;
                meta ^= 1;
            }
        } else if (block.getLogic() instanceof BlockLogicSign) {
            // the final boss
            if (((BlockSignAccessor)block.getLogic()).getIsFreestanding()) {
                if (axis.equals(Axis.X))
                    meta = meta&~15 | (16-(meta&15));
                if (axis.equals(Axis.Z))
                    meta = meta&~15 | ((24-(meta&15))&15);
            } else {
                if (axis.equals(Axis.X) && (meta&4)==4)
                    meta ^= 1;
                if (axis.equals(Axis.Z) && (meta&4)==0)
                    meta ^= 1;
            }
        } else {
            return blockData;
        }

        return new BlockData(blockData.id, meta, blockData.nbt);
    }
}
