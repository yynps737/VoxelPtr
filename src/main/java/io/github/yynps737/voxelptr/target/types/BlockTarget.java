package io.github.yynps737.voxelptr.target.types;

import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * 方块目标
 * 表示一个特定位置的方块（如矿物、资源等）
 */
public class BlockTarget extends Target {

    /**
     * 方块颜色映射表（性能优化：静态 HashMap，O(1) 查询）
     */
    private static final Map<Block, Integer> BLOCK_COLORS = new HashMap<>();

    static {
        // 钻石矿 - 青色
        BLOCK_COLORS.put(Blocks.DIAMOND_ORE, 0xFF00FFFF);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_DIAMOND_ORE, 0xFF00FFFF);

        // 铁矿 - 浅灰色
        BLOCK_COLORS.put(Blocks.IRON_ORE, 0xFFD8D8D8);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_IRON_ORE, 0xFFD8D8D8);

        // 金矿 - 金色
        BLOCK_COLORS.put(Blocks.GOLD_ORE, 0xFFFFD700);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_GOLD_ORE, 0xFFFFD700);
        BLOCK_COLORS.put(Blocks.NETHER_GOLD_ORE, 0xFFFFD700);

        // 绿宝石矿 - 绿色
        BLOCK_COLORS.put(Blocks.EMERALD_ORE, 0xFF00FF00);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_EMERALD_ORE, 0xFF00FF00);

        // 煤矿 - 深灰色
        BLOCK_COLORS.put(Blocks.COAL_ORE, 0xFF4A4A4A);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_COAL_ORE, 0xFF4A4A4A);

        // 红石矿 - 红色
        BLOCK_COLORS.put(Blocks.REDSTONE_ORE, 0xFFFF0000);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_REDSTONE_ORE, 0xFFFF0000);

        // 青金石矿 - 蓝色
        BLOCK_COLORS.put(Blocks.LAPIS_ORE, 0xFF0000FF);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_LAPIS_ORE, 0xFF0000FF);

        // 铜矿 - 橙色
        BLOCK_COLORS.put(Blocks.COPPER_ORE, 0xFFFF8C00);
        BLOCK_COLORS.put(Blocks.DEEPSLATE_COPPER_ORE, 0xFFFF8C00);

        // 远古残骸 - 棕色
        BLOCK_COLORS.put(Blocks.ANCIENT_DEBRIS, 0xFF8B4513);

        // 石英矿 - 白色
        BLOCK_COLORS.put(Blocks.NETHER_QUARTZ_ORE, 0xFFFFFFFF);
    }

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
        // O(1) 查询，性能优化：从 O(n) if-else 链改为 HashMap 查询
        return BLOCK_COLORS.getOrDefault(expectedState.getBlock(), 0xFFFFFFFF);
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
