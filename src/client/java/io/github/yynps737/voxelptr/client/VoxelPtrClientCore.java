package io.github.yynps737.voxelptr.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.client.hud.HudManager;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;

/**
 * 客户端核心管理器
 * 管理所有客户端专属组件
 */
public class VoxelPtrClientCore {

    private final VoxelPtrCore serverCore;
    private HudManager hudManager;
    private KeyBindingManager keyBindingManager;

    public VoxelPtrClientCore() {
        this.serverCore = VoxelPtr.getCore();
    }

    /**
     * 初始化客户端组件
     */
    public void initialize() {
        VoxelPtr.LOGGER.info("初始化客户端组件...");

        // 初始化 HUD 管理器
        this.hudManager = new HudManager(serverCore);

        // 初始化按键绑定管理器
        this.keyBindingManager = new KeyBindingManager(serverCore);

        VoxelPtr.LOGGER.info("客户端组件初始化完成");
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

    /**
     * 获取服务端核心（用于访问配置等共享组件）
     */
    public VoxelPtrCore getServerCore() {
        return serverCore;
    }
}
