package io.github.yynps737.voxelptr.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.client.hud.HudManager;
import io.github.yynps737.voxelptr.client.render.PointerRenderer;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.scanner.impl.EntityScanner;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * 客户端核心管理器
 * 管理所有客户端专属组件
 */
public class VoxelPtrClientCore {

    private final VoxelPtrCore serverCore;
    private PointerRenderer pointerRenderer;
    private HudManager hudManager;
    private KeyBindingManager keyBindingManager;

    // 实体扫描计数器（每30 tick扫描一次，约1.5秒）
    private int entityScanTicker = 0;
    private static final int ENTITY_SCAN_INTERVAL = 30;

    public VoxelPtrClientCore() {
        this.serverCore = VoxelPtr.getCore();
    }

    /**
     * 初始化客户端组件
     */
    public void initialize() {
        VoxelPtr.LOGGER.info("初始化客户端组件...");

        // 初始化指针渲染器
        this.pointerRenderer = new PointerRenderer(serverCore);

        // 初始化 HUD 管理器
        this.hudManager = new HudManager(serverCore);

        // 初始化按键绑定管理器
        this.keyBindingManager = new KeyBindingManager(serverCore);

        // 注册客户端tick事件（用于实体扫描）
        registerClientTick();

        VoxelPtr.LOGGER.info("客户端组件初始化完成");
    }

    /**
     * 注册客户端tick事件
     */
    private void registerClientTick() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    /**
     * 客户端tick处理
     */
    private void onClientTick(MinecraftClient client) {
        if (!serverCore.getConfig().isEnabled()) {
            return;
        }

        if (client.world == null || client.player == null) {
            return;
        }

        // 定期扫描实体（每30 tick = 1.5秒）
        entityScanTicker++;
        if (entityScanTicker >= ENTITY_SCAN_INTERVAL) {
            entityScanTicker = 0;
            tickEntityScanning(client);
        }
    }

    /**
     * 实体扫描tick
     * 当实体扫描器启用时，定期扫描附近的实体
     */
    private void tickEntityScanning(MinecraftClient client) {
        var scannerManager = serverCore.getScannerManager();
        if (scannerManager == null) {
            return;
        }

        EntityScanner entityScanner = scannerManager.getEntityScanner();
        if (entityScanner == null || !entityScanner.isEnabled()) {
            return; // 实体扫描器未启用
        }

        // 扫描玩家周围的实体（排除玩家自己）
        int scanRadius = serverCore.getConfig().getScanRadiusChunks();
        var targets = entityScanner.scanSyncWithExclusion(
            client.world,
            client.player.getBlockPos(),
            scanRadius,
            client.player  // 排除玩家自己
        );

        // 添加到目标追踪器
        if (!targets.isEmpty()) {
            serverCore.getTargetTracker().addTargets(targets);
            VoxelPtr.LOGGER.debug("实体扫描完成，找到 {} 个目标", targets.size());
        }
    }

    /**
     * 获取指针渲染器
     */
    public PointerRenderer getPointerRenderer() {
        return pointerRenderer;
    }

    /**
     * 获取 HUD 管理器
     */
    public HudManager getHudManager() {
        return hudManager;
    }

    /**
     * 获取按键绑定管理器
     */
    public KeyBindingManager getKeyBindingManager() {
        return keyBindingManager;
    }
}
