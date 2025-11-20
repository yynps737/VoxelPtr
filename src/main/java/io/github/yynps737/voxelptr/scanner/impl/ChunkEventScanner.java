package io.github.yynps737.voxelptr.scanner.impl;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.scanner.ChunkScanCache;
import io.github.yynps737.voxelptr.scanner.Scanner;
import io.github.yynps737.voxelptr.target.TargetType;
import io.github.yynps737.voxelptr.target.types.BlockTarget;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于区块事件的扫描器
 *
 * 核心优化策略：
 * - 每个区块只扫描一次（区块加载时）
 * - 结果缓存到 ChunkScanCache
 * - 异步执行，不阻塞主线程
 * - 查询时从缓存获取，极快
 */
public class ChunkEventScanner implements Scanner<BlockTarget> {

    /**
     * 扫描完成回调接口
     */
    public interface ScanCompleteCallback {
        void onScanComplete(List<BlockTarget> targets);
    }

    /**
     * 要扫描的目标方块
     */
    private final Set<Block> targetBlocks;

    /**
     * 扫描结果缓存
     */
    private final ChunkScanCache cache;

    /**
     * 异步扫描线程池
     */
    private final ExecutorService scanExecutor;

    /**
     * 扫描器是否启用
     */
    private boolean enabled;

    /**
     * 扫描完成回调
     */
    private ScanCompleteCallback scanCompleteCallback;

    /**
     * 构造函数
     *
     * @param targetBlocks 要搜索的方块集合（如钻石矿）
     */
    public ChunkEventScanner(Set<Block> targetBlocks) {
        this.targetBlocks = targetBlocks;
        this.cache = new ChunkScanCache();
        this.scanExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r, "VoxelPtr-Scanner");
            thread.setDaemon(true); // 守护线程，游戏关闭时自动停止
            return thread;
        });
        this.enabled = true;
    }

    /**
     * 设置扫描完成回调
     *
     * @param callback 回调函数
     */
    public void setScanCompleteCallback(ScanCompleteCallback callback) {
        this.scanCompleteCallback = callback;
    }

    /**
     * 扫描单个区块（核心方法）
     *
     * @param chunk 要扫描的区块
     * @return 找到的目标列表
     */
    public List<BlockTarget> scanChunk(WorldChunk chunk) {
        List<BlockTarget> targets = new ArrayList<>();
        ChunkPos chunkPos = chunk.getPos();

        // 遍历区块的所有方块
        int minY = chunk.getBottomY();
        int maxY = chunk.getTopY();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(
                            chunkPos.getStartX() + x,
                            y,
                            chunkPos.getStartZ() + z
                    );

                    BlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();

                    // 检查是否是目标方块
                    if (targetBlocks.contains(block)) {
                        targets.add(new BlockTarget(pos, state));
                    }
                }
            }
        }

        return targets;
    }

    /**
     * 异步扫描（从缓存获取）
     * 注意：这个方法假设区块已经被扫描过并缓存
     *
     * @param world 世界对象
     * @param center 中心坐标
     * @param radiusChunks 半径（区块）
     * @return 扫描结果的 Future
     */
    @Override
    public CompletableFuture<List<BlockTarget>> scanAsync(World world, BlockPos center, int radiusChunks) {
        return CompletableFuture.supplyAsync(() -> {
            return scanSync(world, center, radiusChunks);
        }, scanExecutor);
    }

    /**
     * 同步扫描（从缓存获取）
     *
     * @param world 世界对象
     * @param center 中心坐标
     * @param radiusChunks 半径（区块）
     * @return 扫描结果
     */
    @Override
    public List<BlockTarget> scanSync(World world, BlockPos center, int radiusChunks) {
        if (!enabled) {
            return new ArrayList<>();
        }

        List<BlockTarget> allTargets = new ArrayList<>();
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;

        // 遍历半径内的所有区块
        for (int x = -radiusChunks; x <= radiusChunks; x++) {
            for (int z = -radiusChunks; z <= radiusChunks; z++) {
                ChunkPos chunkPos = new ChunkPos(
                        centerChunkX + x,
                        centerChunkZ + z
                );

                // 从缓存获取该区块的目标
                List<BlockTarget> chunkTargets = cache.get(chunkPos);
                if (chunkTargets != null) {
                    allTargets.addAll(chunkTargets);
                }
            }
        }

        return allTargets;
    }

    /**
     * 处理区块加载事件
     * 由客户端代码调用（在区块加载时）
     *
     * @param chunk 加载的区块
     */
    public void onChunkLoad(WorldChunk chunk) {
        if (!enabled) {
            return;
        }

        ChunkPos pos = chunk.getPos();

        // 检查是否已缓存
        if (cache.contains(pos)) {
            return; // 已扫描过，跳过
        }

        // 异步扫描该区块
        CompletableFuture.runAsync(() -> {
            try {
                List<BlockTarget> targets = scanChunk(chunk);
                cache.put(pos, targets);

                if (!targets.isEmpty()) {
                    VoxelPtr.LOGGER.info("区块 {} 扫描完成，找到 {} 个目标",
                            pos, targets.size());

                    // 调用回调通知目标发现
                    if (scanCompleteCallback != null) {
                        scanCompleteCallback.onScanComplete(targets);
                    }
                }
            } catch (Exception e) {
                VoxelPtr.LOGGER.error("扫描区块 {} 时出错", pos, e);
            }
        }, scanExecutor);
    }

    /**
     * 处理方块变化事件
     * 由客户端代码调用（在方块变化时）
     *
     * @param pos 方块位置
     * @param newState 新的方块状态
     */
    public void onBlockUpdate(BlockPos pos, BlockState newState) {
        if (!enabled) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        Block block = newState.getBlock();

        // 更新缓存
        cache.updateBlock(chunkPos, pos, newState);

        // 如果新方块是目标方块，添加到缓存
        if (targetBlocks.contains(block)) {
            cache.addTarget(chunkPos, new BlockTarget(pos, newState));
        }
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.BLOCK;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置目标方块
     *
     * @param blocks 新的目标方块集合
     */
    public void setTargetBlocks(Set<Block> blocks) {
        // 性能优化：检查是否真的需要更新（避免不必要的缓存清空）
        if (this.targetBlocks.equals(blocks)) {
            return; // 目标方块集合未变化，跳过
        }

        this.targetBlocks.clear();
        this.targetBlocks.addAll(blocks);
        // 清空缓存，因为目标改变了
        cache.clear();
    }

    /**
     * 获取缓存对象（用于调试）
     *
     * @return 缓存实例
     */
    public ChunkScanCache getCache() {
        return cache;
    }

    /**
     * 强制重新扫描所有已加载的区块
     * 用于切换目标方块类型后重新扫描
     *
     * @param world 当前世界
     * @param centerPos 中心位置（通常是玩家位置）
     * @param radiusChunks 扫描半径（区块）
     */
    public void rescanLoadedChunks(World world, BlockPos centerPos, int radiusChunks) {
        if (!enabled || world == null || centerPos == null) {
            return;
        }

        VoxelPtr.LOGGER.info("开始重新扫描周围 {}x{} 区块...", radiusChunks * 2 + 1, radiusChunks * 2 + 1);

        int centerChunkX = centerPos.getX() >> 4;
        int centerChunkZ = centerPos.getZ() >> 4;
        int scannedCount = 0;

        // 遍历玩家周围的区块
        for (int x = -radiusChunks; x <= radiusChunks; x++) {
            for (int z = -radiusChunks; z <= radiusChunks; z++) {
                int chunkX = centerChunkX + x;
                int chunkZ = centerChunkZ + z;

                // 获取区块（如果已加载）
                if (world.isChunkLoaded(chunkX, chunkZ)) {
                    WorldChunk chunk = (WorldChunk) world.getChunk(chunkX, chunkZ);
                    if (chunk != null) {
                        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                        scannedCount++;

                        // 异步重新扫描
                        CompletableFuture.runAsync(() -> {
                            try {
                                List<BlockTarget> targets = scanChunk(chunk);
                                cache.put(pos, targets);

                                if (!targets.isEmpty()) {
                                    VoxelPtr.LOGGER.info("区块 {} 重新扫描完成，找到 {} 个目标",
                                            pos, targets.size());

                                    // 调用回调通知目标发现
                                    if (scanCompleteCallback != null) {
                                        scanCompleteCallback.onScanComplete(targets);
                                    }
                                }
                            } catch (Exception e) {
                                VoxelPtr.LOGGER.error("重新扫描区块 {} 时出错", pos, e);
                            }
                        }, scanExecutor);
                    }
                }
            }
        }

        VoxelPtr.LOGGER.info("已提交 {} 个区块的重新扫描任务", scannedCount);
    }

    /**
     * 关闭扫描器，释放资源
     */
    public void shutdown() {
        scanExecutor.shutdown();
        cache.clear();
        VoxelPtr.LOGGER.info("ChunkEventScanner 已关闭");
    }
}
