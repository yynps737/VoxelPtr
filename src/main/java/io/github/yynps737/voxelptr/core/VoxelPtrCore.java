package io.github.yynps737.voxelptr.core;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.config.ConfigManager;
import io.github.yynps737.voxelptr.config.VoxelPtrConfig;
import io.github.yynps737.voxelptr.scanner.ScannerManager;
import io.github.yynps737.voxelptr.target.TargetTracker;
import net.minecraft.world.World;

/**
 * VoxelPtr 核心管理器
 * 管理所有核心组件的生命周期
 */
public class VoxelPtrCore {

    private final ConfigManager configManager;
    private ScannerManager scannerManager;
    private TargetTracker targetTracker;

    // TODO Phase 2: 添加预设管理器
    // private PresetManager presetManager;

    public VoxelPtrCore() {
        VoxelPtr.LOGGER.info("初始化 VoxelPtr 核心...");

        // 初始化配置管理器
        this.configManager = new ConfigManager();
        this.configManager.load();

        VoxelPtr.LOGGER.info("VoxelPtr 核心初始化完成");
    }

    /**
     * 初始化所有组件
     */
    public void initialize() {
        VoxelPtr.LOGGER.info("启动 VoxelPtr 组件...");

        // 初始化目标追踪器
        this.targetTracker = new TargetTracker();

        // 初始化扫描器管理器
        this.scannerManager = new ScannerManager(this);

        // TODO Phase 2: 初始化预设管理器
        // this.presetManager = new PresetManager(this);

        VoxelPtr.LOGGER.info("VoxelPtr 组件启动完成");
    }

    /**
     * 每 tick 调用
     * 需要在客户端 tick 事件中调用此方法
     *
     * @param world 当前世界对象（由客户端传入）
     */
    public void tick(World world) {
        if (!configManager.getConfig().isEnabled()) {
            return; // Mod 已禁用
        }

        if (world != null && targetTracker != null) {
            // 清理过期和无效的目标
            targetTracker.tick(world);
        }
    }

    /**
     * 关闭并保存
     */
    public void shutdown() {
        VoxelPtr.LOGGER.info("关闭 VoxelPtr...");

        // 保存配置
        configManager.save();

        // 清理扫描器资源
        if (scannerManager != null) {
            scannerManager.shutdown();
        }

        // 清理目标追踪器
        if (targetTracker != null) {
            targetTracker.clear();
        }

        VoxelPtr.LOGGER.info("VoxelPtr 已关闭");
    }

    // ========== Getters ==========

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VoxelPtrConfig getConfig() {
        return configManager.getConfig();
    }

    public ScannerManager getScannerManager() {
        return scannerManager;
    }

    public TargetTracker getTargetTracker() {
        return targetTracker;
    }

    // TODO Phase 2: 添加预设管理器的 getter
    // public PresetManager getPresetManager() {
    //     return presetManager;
    // }
}
