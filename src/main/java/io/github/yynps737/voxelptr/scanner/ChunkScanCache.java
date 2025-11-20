package io.github.yynps737.voxelptr.scanner;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.target.types.BlockTarget;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 区块扫描缓存
 * 线程安全的 LRU 缓存实现
 *
 * 核心优化：每个区块只扫描一次，结果缓存起来
 * - 区块加载时扫描
 * - 方块变化时更新缓存
 * - LRU 淘汰策略，防止内存无限增长
 */
public class ChunkScanCache {

    /**
     * 缓存存储（使用 LinkedHashMap 实现 LRU）
     * Key: 区块坐标
     * Value: 该区块中找到的目标列表
     */
    private final Map<ChunkPos, List<BlockTarget>> cache;

    /**
     * 最大缓存区块数
     * 1024 个区块 ≈ 占用内存 < 50MB
     */
    private static final int MAX_CACHE_SIZE = 1024;

    public ChunkScanCache() {
        // LinkedHashMap with access-order for LRU
        this.cache = new LinkedHashMap<ChunkPos, List<BlockTarget>>(
                MAX_CACHE_SIZE,
                0.75f,
                true  // accessOrder = true（LRU 模式）
        ) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ChunkPos, List<BlockTarget>> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    }

    /**
     * 存入缓存
     *
     * @param pos 区块坐标
     * @param targets 该区块找到的目标列表
     */
    public synchronized void put(ChunkPos pos, List<BlockTarget> targets) {
        // 复制一份，避免外部修改影响缓存
        cache.put(pos, new ArrayList<>(targets));
        VoxelPtr.LOGGER.debug("缓存区块 {} ({} 个目标)", pos, targets.size());
    }

    /**
     * 从缓存获取
     *
     * @param pos 区块坐标
     * @return 该区块的目标列表，如果不存在返回 null
     */
    public synchronized List<BlockTarget> get(ChunkPos pos) {
        List<BlockTarget> targets = cache.get(pos);
        if (targets != null) {
            // 返回副本，避免外部修改
            return new ArrayList<>(targets);
        }
        return null;
    }

    /**
     * 检查缓存是否包含指定区块
     *
     * @param pos 区块坐标
     * @return true 如果已缓存
     */
    public synchronized boolean contains(ChunkPos pos) {
        return cache.containsKey(pos);
    }

    /**
     * 更新单个方块
     * 当方块变化时调用，更新对应区块的缓存
     *
     * @param chunkPos 区块坐标
     * @param blockPos 方块坐标
     * @param newState 新的方块状态
     */
    public synchronized void updateBlock(ChunkPos chunkPos, BlockPos blockPos, BlockState newState) {
        List<BlockTarget> targets = cache.get(chunkPos);
        if (targets == null) {
            return; // 该区块未缓存，无需更新
        }

        // 移除该位置的旧目标
        targets.removeIf(t -> t.getBlockPos().equals(blockPos));

        VoxelPtr.LOGGER.debug("更新区块 {} 的方块 {}", chunkPos, blockPos);
    }

    /**
     * 添加单个目标到缓存
     * 用于方块变化事件
     *
     * @param chunkPos 区块坐标
     * @param target 新目标
     */
    public synchronized void addTarget(ChunkPos chunkPos, BlockTarget target) {
        List<BlockTarget> targets = cache.computeIfAbsent(chunkPos, k -> new ArrayList<>());
        targets.add(target);
    }

    /**
     * 使指定区块的缓存失效
     *
     * @param pos 区块坐标
     */
    public synchronized void invalidate(ChunkPos pos) {
        cache.remove(pos);
        VoxelPtr.LOGGER.debug("清除区块 {} 的缓存", pos);
    }

    /**
     * 清空所有缓存
     */
    public synchronized void clear() {
        int size = cache.size();
        cache.clear();
        VoxelPtr.LOGGER.info("清空缓存（已清理 {} 个区块）", size);
    }

    /**
     * 获取当前缓存的区块数
     *
     * @return 缓存大小
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * 获取缓存中所有目标的总数
     *
     * @return 目标总数
     */
    public synchronized int getTotalTargetCount() {
        return cache.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息字符串
     */
    public synchronized String getStats() {
        return String.format("缓存: %d 区块, %d 目标",
                size(), getTotalTargetCount());
    }
}
