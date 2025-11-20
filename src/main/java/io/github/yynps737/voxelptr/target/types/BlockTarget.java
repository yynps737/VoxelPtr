package io.github.yynps737.voxelptr.target.types;

import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 方块目标
 * 表示一个特定位置的方块（如矿物、资源等）
 */
public class BlockTarget extends Target {

    private final BlockPos blockPos;
    private final BlockState expectedState;

    public BlockTarget(BlockPos pos, BlockState state) {
        super(TargetType.BLOCK, Vec3d.ofCenter(pos));
        this.blockPos = pos.toImmutable();
        this.expectedState = state;
    }

    @Override
    public String getDisplayName() {
        return expectedState.getBlock().getName().getString();
    }

    @Override
    public boolean isValid(World world) {
        // 检查区块是否已加载
        if (!world.isChunkLoaded(blockPos)) {
            return true; // 区块未加载，假设仍有效（等待重新加载）
        }

        // 检查方块是否还存在
        BlockState currentState = world.getBlockState(blockPos);
        return currentState.equals(expectedState);
    }

    @Override
    public int getColor() {
        Block block = expectedState.getBlock();

        // 根据方块类型返回颜色
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return 0xFF00FFFF; // 青色（钻石）
        } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return 0xFFD8D8D8; // 浅灰色（铁）
        } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return 0xFFFFD700; // 金色
        } else if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return 0xFF00FF00; // 绿色（绿宝石）
        } else if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return 0xFF4A4A4A; // 深灰色（煤炭）
        } else if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return 0xFFFF0000; // 红色（红石）
        } else if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return 0xFF0000FF; // 蓝色（青金石）
        } else if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return 0xFFFF8C00; // 橙色（铜）
        } else if (block == Blocks.ANCIENT_DEBRIS) {
            return 0xFF8B4513; // 棕色（远古残骸）
        }

        return 0xFFFFFFFF; // 默认白色
    }

    /**
     * 获取方块位置
     */
    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * 获取方块状态
     */
    public BlockState getExpectedState() {
        return expectedState;
    }

    @Override
    public String toString() {
        return String.format("BlockTarget{pos=%s, block=%s}",
                blockPos, expectedState.getBlock().getName().getString());
    }
}
