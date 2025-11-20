package io.github.yynps737.voxelptr;

import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoxelPtr implements ModInitializer {

    public static final String MOD_ID = "voxelptr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * 核心管理器实例
     */
    private static VoxelPtrCore core;

    @Override
    public void onInitialize() {
        LOGGER.info("VoxelPtr 正在初始化...");

        // 初始化核心管理器
        core = new VoxelPtrCore();
        core.initialize();

        LOGGER.info("VoxelPtr 初始化完成！");
    }

    /**
     * 获取核心管理器实例
     */
    public static VoxelPtrCore getCore() {
        return core;
    }
}
