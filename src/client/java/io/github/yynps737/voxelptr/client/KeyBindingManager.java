package io.github.yynps737.voxelptr.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.scanner.ScannerManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

/**
 * 按键绑定管理器
 * 管理模组的所有按键绑定
 */
public class KeyBindingManager {

    /**
     * 扫描模式枚举
     */
    private enum ScanMode {
        BLOCK,   // 矿物模式
        ENTITY   // 实体模式
    }

    private final VoxelPtrCore core;

    // 按键绑定
    private KeyBinding toggleKey;          // V键：总开关
    private KeyBinding switchModeKey;      // M键：切换模式
    private KeyBinding switchPresetKey;    // N键：切换预设

    // 当前扫描模式
    private ScanMode currentMode = ScanMode.BLOCK;

    // 矿物预设列表
    private final String[] blockPresets = {
        "diamond", "iron", "gold", "emerald", "ancient_debris",
        "coal", "redstone", "lapis", "copper", "quartz"
    };
    private int currentBlockPresetIndex = 0; // 默认钻石

    // 实体预设列表
    private final String[] entityPresets = {
        "villager", "pillager", "enderman", "animals",
        "hostile", "player", "boss", "neutral"
    };
    private int currentEntityPresetIndex = 0; // 默认村民

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

        // 切换模式按键 (默认: M)
        switchModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.switch_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.voxelptr"
        ));

        // 切换预设按键 (默认: N)
        switchPresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.voxelptr.switch_preset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.voxelptr"
        ));

        VoxelPtr.LOGGER.info("按键绑定已注册 (V:开关 M:模式 N:预设)");
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

            // M键：切换模式
            if (switchModeKey.wasPressed()) {
                onSwitchModePressed();
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

        String statusText = enabled ? "已禁用" : "已启用";
        VoxelPtr.LOGGER.info("VoxelPtr {}", statusText);

        // 发送消息给玩家
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§6[VoxelPtr] §f" + statusText), false);
        }
    }

    /**
     * M键：切换模式被按下
     */
    private void onSwitchModePressed() {
        if (!core.getConfig().isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§6[VoxelPtr] §c功能已禁用，请先按V键启用"), false);
            }
            return;
        }

        // 切换模式
        currentMode = (currentMode == ScanMode.BLOCK) ? ScanMode.ENTITY : ScanMode.BLOCK;

        String modeName = (currentMode == ScanMode.BLOCK) ? "矿物模式" : "实体模式";
        VoxelPtr.LOGGER.info("切换到: {}", modeName);

        // 清空所有目标
        if (core.getTargetTracker() != null) {
            core.getTargetTracker().clear();
        }

        // 应用当前模式的当前预设
        applyCurrentPreset();

        // 发送消息给玩家
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String currentPresetName = getCurrentPresetDisplayName();
            client.player.sendMessage(Text.literal(
                "§6[VoxelPtr] §f切换到§b" + modeName + " §f- §a" + currentPresetName), false);
        }
    }

    /**
     * N键：切换预设被按下
     */
    private void onSwitchPresetPressed() {
        if (!core.getConfig().isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§6[VoxelPtr] §c功能已禁用，请先按V键启用"), false);
            }
            return;
        }

        var scannerManager = core.getScannerManager();
        if (scannerManager == null) {
            return;
        }

        // 根据当前模式切换预设
        if (currentMode == ScanMode.BLOCK) {
            // 矿物模式：切换矿物预设
            currentBlockPresetIndex = (currentBlockPresetIndex + 1) % blockPresets.length;
        } else {
            // 实体模式：切换实体预设
            currentEntityPresetIndex = (currentEntityPresetIndex + 1) % entityPresets.length;
        }

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
            String modeName = (currentMode == ScanMode.BLOCK) ? "矿物" : "实体";
            client.player.sendMessage(Text.literal(
                "§6[VoxelPtr] §f正在搜索§b" + modeName + "§f: §a" + displayName), false);
        }
    }

    /**
     * 应用当前模式的当前预设
     */
    private void applyCurrentPreset() {
        var scannerManager = core.getScannerManager();
        if (scannerManager == null) {
            return;
        }

        if (currentMode == ScanMode.BLOCK) {
            // 应用矿物预设
            String presetName = blockPresets[currentBlockPresetIndex];
            applyBlockPreset(scannerManager, presetName);
        } else {
            // 应用实体预设
            String presetName = entityPresets[currentEntityPresetIndex];
            applyEntityPreset(scannerManager, presetName);
        }
    }

    /**
     * 获取当前预设的显示名称
     */
    private String getCurrentPresetDisplayName() {
        if (currentMode == ScanMode.BLOCK) {
            String presetName = blockPresets[currentBlockPresetIndex];
            return getPresetDisplayName(presetName);
        } else {
            String presetName = entityPresets[currentEntityPresetIndex];
            return getPresetDisplayName(presetName);
        }
    }

    /**
     * 获取当前扫描模式
     */
    public String getCurrentModeName() {
        return (currentMode == ScanMode.BLOCK) ? "矿物模式" : "实体模式";
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
        // 禁用实体扫描器
        var entityScanner = scannerManager.getEntityScanner();
        if (entityScanner != null) {
            entityScanner.setEnabled(false);
        }

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
                int scanRadius = 8; // 8个区块半径
                blockScanner.rescanLoadedChunks(client.world, client.player.getBlockPos(), scanRadius);
            }
        }
    }

    /**
     * 应用实体预设
     */
    private void applyEntityPreset(ScannerManager scannerManager, String presetName) {
        // 禁用方块扫描器
        var blockScanner = scannerManager.getBlockScanner();
        if (blockScanner != null) {
            blockScanner.setEnabled(false);
        }

        // 启用实体扫描器
        var entityScanner = scannerManager.getEntityScanner();
        if (entityScanner != null) {
            // 获取新的目标实体类型
            var targetEntities = scannerManager.getPresetEntities(presetName);
            scannerManager.setTargetEntities(targetEntities);
            entityScanner.setEnabled(true);

            VoxelPtr.LOGGER.info("已启用实体扫描器，目标类型: {}", targetEntities.size());
        }
    }

    /**
     * 获取预设的显示名称
     */
    private String getPresetDisplayName(String presetName) {
        return switch (presetName) {
            // 矿石预设
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
            // 实体预设
            case "villager" -> "村民";
            case "pillager" -> "掠夺者";
            case "enderman" -> "末影人";
            case "animals" -> "动物";
            case "hostile" -> "敌对生物";
            case "player" -> "玩家";
            case "boss" -> "Boss生物";
            case "neutral" -> "中立生物";
            default -> "未知";
        };
    }

    /**
     * 获取总开关按键
     */
    public KeyBinding getToggleKey() {
        return toggleKey;
    }

    /**
     * 获取模式切换按键
     */
    public KeyBinding getSwitchModeKey() {
        return switchModeKey;
    }

    /**
     * 获取预设切换按键
     */
    public KeyBinding getSwitchPresetKey() {
        return switchPresetKey;
    }
}
