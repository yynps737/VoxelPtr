package io.github.yynps737.voxelptr.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.scanner.ScannerManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

/**
 * 按键绑定管理器
 * 管理模组的所有按键绑定
 */
public class KeyBindingManager {

    private final VoxelPtrCore core;

    // 按键绑定
    private KeyBinding toggleKey;          // V键：总开关
    private KeyBinding switchPresetKey;    // N键：切换预设

    // 矿物预设列表
    private final String[] blockPresets = {
        "diamond", "iron", "gold", "emerald", "ancient_debris",
        "coal", "redstone", "lapis", "copper", "quartz"
    };
    private int currentBlockPresetIndex = 0; // 默认钻石

    public KeyBindingManager(VoxelPtrCore core) {
        this.core = core;
        initializeKeyBindings();
        registerKeyHandlers();
    }

    /**
     * 初始化按键绑定
     */
    private void initializeKeyBindings() {
        // 总开关按键 (默认: V)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.voxelptr"
        ));

        // 切换预设按键 (默认: N)
        switchPresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.switch_preset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.voxelptr"
        ));

        VoxelPtr.LOGGER.info("按键绑定已注册 (V:开关 N:预设)");
    }

    /**
     * 注册按键处理器
     */
    private void registerKeyHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // V键：总开关
            if (toggleKey.wasPressed()) {
                onTogglePressed();
            }

            // N键：切换预设
            if (switchPresetKey.wasPressed()) {
                onSwitchPresetPressed();
            }
        });
    }

    /**
     * V键：总开关被按下
     */
    private void onTogglePressed() {
        boolean enabled = core.getConfig().isEnabled();
        core.getConfig().setEnabled(!enabled);
        core.getConfigManager().save();

        String statusKey = enabled ? "message.voxelptr.disabled" : "message.voxelptr.enabled";
        String statusText = I18n.translate(statusKey);
        VoxelPtr.LOGGER.info("VoxelPtr {}", statusText);

        // 发送消息给玩家
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[VoxelPtr] §f" + statusText), false);
        }
    }

    /**
     * N键：切换预设被按下
     */
    private void onSwitchPresetPressed() {
        if (!core.getConfig().isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                String hintText = I18n.translate("message.voxelptr.disabled_hint");
                client.player.sendMessage(Text.literal("§6[VoxelPtr] §c" + hintText), false);
            }
            return;
        }

        var scannerManager = core.getScannerManager();
        if (scannerManager == null) {
            return;
        }

        // 切换矿物预设
        currentBlockPresetIndex = (currentBlockPresetIndex + 1) % blockPresets.length;

        // 清空当前目标
        if (core.getTargetTracker() != null) {
            core.getTargetTracker().clear();
        }

        // 应用新预设
        applyCurrentPreset();

        // 给玩家发送消息
        String displayName = getCurrentPresetDisplayName();
        VoxelPtr.LOGGER.info("切换到预设: {}", displayName);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String searchingText = I18n.translate("message.voxelptr.searching", displayName);
            client.player.sendMessage(Text.literal("§6[VoxelPtr] §f" + searchingText), false);
        }
    }

    /**
     * 应用当前预设
     */
    private void applyCurrentPreset() {
        var scannerManager = core.getScannerManager();
        if (scannerManager == null) {
            return;
        }

        // 应用矿物预设
        String presetName = blockPresets[currentBlockPresetIndex];
        applyBlockPreset(scannerManager, presetName);
    }

    /**
     * 获取当前预设的显示名称
     */
    private String getCurrentPresetDisplayName() {
        String presetName = blockPresets[currentBlockPresetIndex];
        return getPresetDisplayName(presetName);
    }

    /**
     * 获取当前扫描模式
     */
    public String getCurrentModeName() {
        return I18n.translate("hud.voxelptr.mode");
    }

    /**
     * 获取当前预设名称（用于HUD显示）
     */
    public String getCurrentPresetName() {
        return getCurrentPresetDisplayName();
    }

    /**
     * 应用方块预设
     */
    private void applyBlockPreset(ScannerManager scannerManager, String presetName) {
        // 启用方块扫描器
        var blockScanner = scannerManager.getBlockScanner();
        if (blockScanner != null) {
            blockScanner.setEnabled(true);

            // 获取新的目标方块集合
            var targetBlocks = scannerManager.getPresetBlocks(presetName);
            scannerManager.setTargetBlocks(targetBlocks);

            // 清空扫描缓存
            if (blockScanner.getCache() != null) {
                blockScanner.getCache().clear();
            }

            // 重新扫描当前已加载的区块
            var client = net.minecraft.client.MinecraftClient.getInstance();
            if (client != null && client.world != null && client.player != null) {
                int scanRadius = core.getConfig().getScanRadiusChunks();
                blockScanner.rescanLoadedChunks(client.world, client.player.getBlockPos(), scanRadius);
            }
        }
    }

    /**
     * 获取预设的显示名称
     */
    private String getPresetDisplayName(String presetName) {
        return I18n.translate("preset.voxelptr." + presetName);
    }

    /**
     * 获取总开关按键
     */
    public KeyBinding getToggleKey() {
        return toggleKey;
    }

    /**
     * 获取预设切换按键
     */
    public KeyBinding getSwitchPresetKey() {
        return switchPresetKey;
    }
}
