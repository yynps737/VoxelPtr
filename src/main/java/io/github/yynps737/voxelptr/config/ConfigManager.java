package io.github.yynps737.voxelptr.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.yynps737.voxelptr.VoxelPtr;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 配置管理器
 * 负责加载、保存和管理配置文件
 */
public class ConfigManager {

    private static final String CONFIG_FILE_NAME = "voxelptr.json";
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(CONFIG_FILE_NAME);

    private VoxelPtrConfig config;
    private final Gson gson;

    public ConfigManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * 加载配置文件
     * 如果文件不存在，则创建默认配置
     */
    public void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = gson.fromJson(reader, VoxelPtrConfig.class);
                VoxelPtr.LOGGER.info("配置文件加载成功: {}", CONFIG_PATH);
            } catch (IOException e) {
                VoxelPtr.LOGGER.error("加载配置文件失败，使用默认配置", e);
                config = new VoxelPtrConfig();
                save(); // 保存默认配置
            }
        } else {
            VoxelPtr.LOGGER.info("配置文件不存在，创建默认配置: {}", CONFIG_PATH);
            config = new VoxelPtrConfig();
            save();
        }
    }

    /**
     * 保存配置文件
     */
    public void save() {
        try {
            // 确保配置目录存在
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                gson.toJson(config, writer);
                VoxelPtr.LOGGER.info("配置文件保存成功: {}", CONFIG_PATH);
            }
        } catch (IOException e) {
            VoxelPtr.LOGGER.error("保存配置文件失败", e);
        }
    }

    /**
     * 重新加载配置文件
     */
    public void reload() {
        VoxelPtr.LOGGER.info("重新加载配置文件...");
        load();
    }

    /**
     * 获取配置实例
     */
    public VoxelPtrConfig getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }

    /**
     * 获取配置文件路径
     */
    public Path getConfigPath() {
        return CONFIG_PATH;
    }
}
