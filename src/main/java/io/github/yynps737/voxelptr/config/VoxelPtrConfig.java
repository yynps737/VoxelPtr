package io.github.yynps737.voxelptr.config;

/**
 * VoxelPtr 配置类
 * 存储所有可配置的选项
 */
public class VoxelPtrConfig {

    // ========== 基础配置 ==========

    /**
     * 是否启用 Mod
     */
    private boolean enabled = true;

    /**
     * 当前使用的预设
     */
    private String currentPreset = "diamond_finder";

    // ========== 扫描配置 ==========

    /**
     * 扫描半径（区块）
     * 2 = 5x5 区块（80x80 方块）
     */
    private int scanRadiusChunks = 2;

    /**
     * 扫描间隔（tick）
     * 20 tick = 1 秒
     */
    private int scanIntervalTicks = 20;

    /**
     * 是否启用异步扫描
     */
    private boolean asyncScan = true;

    // ========== HUD 配置 ==========

    /**
     * 是否启用 HUD
     */
    private boolean hudEnabled = true;

    /**
     * HUD 位置
     */
    private String hudPosition = "top_left";

    /**
     * HUD 显示的最大目标数
     */
    private int maxHudTargets = 10;

    // ========== 过滤器配置 ==========

    /**
     * 最小距离过滤（格）
     */
    private float minDistance = 0.0f;

    /**
     * 最大距离过滤（格）
     */
    private float maxDistanceFilter = 64.0f;

    // ========== Getters and Setters ==========

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCurrentPreset() {
        return currentPreset;
    }

    public void setCurrentPreset(String currentPreset) {
        this.currentPreset = currentPreset;
    }

    public int getScanRadiusChunks() {
        return scanRadiusChunks;
    }

    public void setScanRadiusChunks(int scanRadiusChunks) {
        this.scanRadiusChunks = scanRadiusChunks;
    }

    public int getScanIntervalTicks() {
        return scanIntervalTicks;
    }

    public void setScanIntervalTicks(int scanIntervalTicks) {
        this.scanIntervalTicks = scanIntervalTicks;
    }

    public boolean isAsyncScan() {
        return asyncScan;
    }

    public void setAsyncScan(boolean asyncScan) {
        this.asyncScan = asyncScan;
    }

    public boolean isHudEnabled() {
        return hudEnabled;
    }

    public void setHudEnabled(boolean hudEnabled) {
        this.hudEnabled = hudEnabled;
    }

    public String getHudPosition() {
        return hudPosition;
    }

    public void setHudPosition(String hudPosition) {
        this.hudPosition = hudPosition;
    }

    public int getMaxHudTargets() {
        return maxHudTargets;
    }

    public void setMaxHudTargets(int maxHudTargets) {
        this.maxHudTargets = maxHudTargets;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public float getMaxDistanceFilter() {
        return maxDistanceFilter;
    }

    public void setMaxDistanceFilter(float maxDistanceFilter) {
        this.maxDistanceFilter = maxDistanceFilter;
    }
}
