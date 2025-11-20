package io.github.yynps737.voxelptr.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定管理器
 * 管理模组的所有按键绑定
 */
public class KeyBindingManager {

    private final VoxelPtrCore core;

    // 按键绑定
    private KeyBinding toggleKey;
    private KeyBinding scanKey;
    private KeyBinding switchPresetKey;

    // 矿石预设列表
    private final String[] presets = {"diamond", "iron", "gold", "emerald", "ancient_debris"};
    private int currentPresetIndex = 0; // 默认钻石

    public KeyBindingManager(VoxelPtrCore core) {
        this.core = core;
        initializeKeyBindings();
        registerKeyHandlers();
    }

    /**
     * 初始化按键绑定
     */
    private void initializeKeyBindings() {
        // 切换启用/禁用按键 (默认: V)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.voxelptr"
        ));

        // 手动触发扫描按键 (默认: B)
        scanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.scan",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.voxelptr"
        ));

        // 切换矿石类型按键 (默认: N)
        switchPresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.switch_preset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.voxelptr"
        ));

        VoxelPtr.LOGGER.info("按键绑定已注册");
    }

    /**
     * 注册按键处理器
     */
    private void registerKeyHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 处理切换按键
            if (toggleKey.wasPressed()) {
                onTogglePressed();
            }

            // 处理扫描按键
            if (scanKey.wasPressed()) {
                onScanPressed();
            }

            // 处理切换预设按键
            if (switchPresetKey.wasPressed()) {
                onSwitchPresetPressed();
            }
        });
    }

    /**
     * 切换按键被按下
     */
    private void onTogglePressed() {
        boolean enabled = core.getConfig().isEnabled();
        core.getConfig().setEnabled(!enabled);
        core.getConfigManager().save();

        VoxelPtr.LOGGER.info("VoxelPtr 已{}", enabled ? "禁用" : "启用");

        // TODO: 发送消息给玩家
    }

    /**
     * 扫描按键被按下
     */
    private void onScanPressed() {
        if (!core.getConfig().isEnabled()) {
            return;
        }

        VoxelPtr.LOGGER.info("手动触发扫描（暂未实现）");
        // 注：当前扫描是自动触发的，手动扫描功能暂未实现
    }

    /**
     * 切换预设按键被按下
     */
    private void onSwitchPresetPressed() {
        if (!core.getConfig().isEnabled()) {
            return;
        }

        // 循环切换到下一个预设
        currentPresetIndex = (currentPresetIndex + 1) % presets.length;
        String presetName = presets[currentPresetIndex];

        // 获取新的目标方块集合
        var scannerManager = core.getScannerManager();
        if (scannerManager != null) {
            var targetBlocks = scannerManager.getPresetBlocks(presetName);

            // 清空当前目标
            if (core.getTargetTracker() != null) {
                core.getTargetTracker().clear();
            }

            // 应用新的预设
            scannerManager.setTargetBlocks(targetBlocks);

            // 清空扫描缓存
            var blockScanner = scannerManager.getBlockScanner();
            if (blockScanner != null && blockScanner.getCache() != null) {
                blockScanner.getCache().clear();
            }

            VoxelPtr.LOGGER.info("切换到预设: {} ({})", presetName, getPresetDisplayName(presetName));

            // 重新扫描当前已加载的区块
            var client = net.minecraft.client.MinecraftClient.getInstance();
            if (client != null && client.world != null && client.player != null && blockScanner != null) {
                // 扫描半径：8个区块（128格）- 足够覆盖大部分玩家周围区域
                int scanRadius = 8;
                blockScanner.rescanLoadedChunks(client.world, client.player.getBlockPos(), scanRadius);
            }

            // TODO: 给玩家发送消息
            // player.sendMessage(Text.literal("正在搜索: " + getPresetDisplayName(presetName)), false);
        }
    }

    /**
     * 获取预设的显示名称
     */
    private String getPresetDisplayName(String presetName) {
        return switch (presetName) {
            case "diamond" -> "钻石矿";
            case "iron" -> "铁矿";
            case "gold" -> "金矿";
            case "emerald" -> "绿宝石矿";
            case "ancient_debris" -> "远古残骸";
            case "coal" -> "煤矿";
            case "redstone" -> "红石矿";
            case "lapis" -> "青金石矿";
            case "copper" -> "铜矿";
            case "quartz" -> "石英矿";
            default -> "未知";
        };
    }

    /**
     * 获取切换按键
     */
    public KeyBinding getToggleKey() {
        return toggleKey;
    }

    /**
     * 获取扫描按键
     */
    public KeyBinding getScanKey() {
        return scanKey;
    }
}
