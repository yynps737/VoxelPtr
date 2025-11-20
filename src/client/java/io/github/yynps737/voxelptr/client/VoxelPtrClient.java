package io.github.yynps737.voxelptr.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class VoxelPtrClient implements ClientModInitializer {

    private static VoxelPtrClientCore clientCore;

    @Override
    public void onInitializeClient() {
        VoxelPtr.LOGGER.info("VoxelPtr 客户端正在初始化...");

        // 初始化客户端核心
        clientCore = new VoxelPtrClientCore();
        clientCore.initialize();

        // 注册客户端 Tick 事件
        registerClientTickEvent();

        // 区块加载和方块更新事件通过 Mixin 注入
        // 见 MixinClientChunkManager.java 和 MixinClientWorld.java

        // 渲染和 HUD 通过 Mixin 注入
        // 见 MixinWorldRenderer.java 和 MixinInGameHud.java

        VoxelPtr.LOGGER.info("VoxelPtr 客户端初始化完成！");
    }

    /**
     * 注册客户端 Tick 事件
     * 用于调用 VoxelPtrCore.tick() 清理过期目标
     */
    private void registerClientTickEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VoxelPtrCore core = VoxelPtr.getCore();
            if (core != null && client.world != null) {
                core.tick(client.world);
            }
        });

        VoxelPtr.LOGGER.debug("客户端 Tick 事件已注册");
    }

    /**
     * 获取客户端核心实例
     */
    public static VoxelPtrClientCore getClientCore() {
        return clientCore;
    }
}
