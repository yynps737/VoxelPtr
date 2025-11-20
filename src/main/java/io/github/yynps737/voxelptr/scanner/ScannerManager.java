package io.github.yynps737.voxelptr.scanner;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.scanner.impl.ChunkEventScanner;
import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetType;
import io.github.yynps737.voxelptr.target.types.BlockTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 扫描器管理器
 * 负责管理所有扫描器实例，提供统一的扫描接口
 *
 * 核心功能：
 * - 管理不同类型的扫描器（Block、Entity、Structure）
 * - 提供统一的扫描调用接口
 * - 管理扫描器生命周期
 */
public class ScannerManager {

    private final VoxelPtrCore core;

    /**
     * 扫描器映射
     * Key: TargetType（目标类型）
     * Value: 对应的扫描器实例
     */
    private final Map<TargetType, Scanner<?>> scanners;

    /**
     * 方块扫描器（专门用于方块目标）
     */
    private ChunkEventScanner blockScanner;

    public ScannerManager(VoxelPtrCore core) {
        this.core = core;
        this.scanners = new HashMap<>();

        initializeScanners();
    }

    /**
     * 初始化所有扫描器
     */
    private void initializeScanners() {
        VoxelPtr.LOGGER.info("初始化扫描器...");

        // 初始化方块扫描器
        Set<Block> defaultTargetBlocks = getDefaultTargetBlocks();
        blockScanner = new ChunkEventScanner(defaultTargetBlocks);

        // 设置扫描完成回调，将结果添加到 TargetTracker
        blockScanner.setScanCompleteCallback(targets -> {
            if (core.getTargetTracker() != null) {
                core.getTargetTracker().addTargets(targets);
            }
        });

        scanners.put(TargetType.BLOCK, blockScanner);

        VoxelPtr.LOGGER.info("扫描器初始化完成（已注册 {} 个扫描器）", scanners.size());
    }

    /**
     * 获取默认的目标方块集合
     * 默认只扫描钻石矿（性能优化 + 用户选择模式）
     *
     * @return 目标方块集合
     */
    private Set<Block> getDefaultTargetBlocks() {
        Set<Block> blocks = new HashSet<>();

        // 默认只扫描钻石矿（最重要的矿物）
        blocks.add(Blocks.DIAMOND_ORE);
        blocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);

        VoxelPtr.LOGGER.info("默认扫描模式：钻石矿");
        return blocks;
    }

    /**
     * 获取所有可用的矿石预设
     * 用于用户切换不同的扫描目标
     */
    public Set<Block> getPresetBlocks(String presetName) {
        Set<Block> blocks = new HashSet<>();

        switch (presetName.toLowerCase()) {
            case "diamond":
                blocks.add(Blocks.DIAMOND_ORE);
                blocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
                break;

            case "iron":
                blocks.add(Blocks.IRON_ORE);
                blocks.add(Blocks.DEEPSLATE_IRON_ORE);
                break;

            case "gold":
                blocks.add(Blocks.GOLD_ORE);
                blocks.add(Blocks.DEEPSLATE_GOLD_ORE);
                blocks.add(Blocks.NETHER_GOLD_ORE);
                break;

            case "emerald":
                blocks.add(Blocks.EMERALD_ORE);
                blocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
                break;

            case "coal":
                blocks.add(Blocks.COAL_ORE);
                blocks.add(Blocks.DEEPSLATE_COAL_ORE);
                break;

            case "redstone":
                blocks.add(Blocks.REDSTONE_ORE);
                blocks.add(Blocks.DEEPSLATE_REDSTONE_ORE);
                break;

            case "lapis":
                blocks.add(Blocks.LAPIS_ORE);
                blocks.add(Blocks.DEEPSLATE_LAPIS_ORE);
                break;

            case "copper":
                blocks.add(Blocks.COPPER_ORE);
                blocks.add(Blocks.DEEPSLATE_COPPER_ORE);
                break;

            case "ancient_debris":
                blocks.add(Blocks.ANCIENT_DEBRIS);
                break;

            case "quartz":
                blocks.add(Blocks.NETHER_QUARTZ_ORE);
                break;

            default:
                // 默认返回钻石
                blocks.add(Blocks.DIAMOND_ORE);
                blocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
                break;
        }

        return blocks;
    }


    /**
     * 执行异步扫描
     *
     * @param world 世界对象
     * @param center 扫描中心
     * @param radiusChunks 扫描半径（区块）
     * @param targetType 目标类型
     * @return 扫描结果的 Future
     */
    @SuppressWarnings("unchecked")
    public CompletableFuture<List<? extends Target>> scanAsync(
            World world,
            BlockPos center,
            int radiusChunks,
            TargetType targetType
    ) {
        Scanner<?> scanner = scanners.get(targetType);

        if (scanner == null) {
            VoxelPtr.LOGGER.warn("未找到目标类型为 {} 的扫描器", targetType);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (!scanner.isEnabled()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return (CompletableFuture<List<? extends Target>>) (CompletableFuture<?>) scanner.scanAsync(world, center, radiusChunks);
    }

    /**
     * 执行同步扫描
     *
     * @param world 世界对象
     * @param center 扫描中心
     * @param radiusChunks 扫描半径（区块）
     * @param targetType 目标类型
     * @return 扫描结果
     */
    public List<? extends Target> scanSync(
            World world,
            BlockPos center,
            int radiusChunks,
            TargetType targetType
    ) {
        Scanner<?> scanner = scanners.get(targetType);

        if (scanner == null) {
            VoxelPtr.LOGGER.warn("未找到目标类型为 {} 的扫描器", targetType);
            return Collections.emptyList();
        }

        if (!scanner.isEnabled()) {
            return Collections.emptyList();
        }

        return scanner.scanSync(world, center, radiusChunks);
    }

    /**
     * 获取指定类型的扫描器
     *
     * @param type 目标类型
     * @return 扫描器实例，如果不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends Target> Scanner<T> getScanner(TargetType type) {
        return (Scanner<T>) scanners.get(type);
    }

    /**
     * 获取方块扫描器（方便直接访问）
     *
     * @return 方块扫描器实例
     */
    public ChunkEventScanner getBlockScanner() {
        return blockScanner;
    }

    /**
     * 设置方块扫描器的目标方块
     *
     * @param blocks 新的目标方块集合
     */
    public void setTargetBlocks(Set<Block> blocks) {
        if (blockScanner != null) {
            blockScanner.setTargetBlocks(blocks);
            VoxelPtr.LOGGER.info("更新目标方块集合（{} 种方块）", blocks.size());
        }
    }

    /**
     * 启用或禁用指定类型的扫描器
     *
     * @param type 目标类型
     * @param enabled 是否启用
     */
    public void setEnabled(TargetType type, boolean enabled) {
        Scanner<?> scanner = scanners.get(type);
        if (scanner != null) {
            scanner.setEnabled(enabled);
            VoxelPtr.LOGGER.info("扫描器 {} 已{}", type, enabled ? "启用" : "禁用");
        }
    }

    /**
     * 获取所有已注册的扫描器
     *
     * @return 扫描器集合
     */
    public Collection<Scanner<?>> getAllScanners() {
        return scanners.values();
    }

    /**
     * 关闭所有扫描器，释放资源
     */
    public void shutdown() {
        VoxelPtr.LOGGER.info("关闭所有扫描器...");

        if (blockScanner != null) {
            blockScanner.shutdown();
        }

        scanners.clear();
        VoxelPtr.LOGGER.info("扫描器已全部关闭");
    }
}
