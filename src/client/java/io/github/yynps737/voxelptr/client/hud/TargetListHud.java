package io.github.yynps737.voxelptr.client.hud;

import io.github.yynps737.voxelptr.client.VoxelPtrClient;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * 目标列表 HUD
 * 显示所有活跃目标的列表（带方向指示）
 */
public class TargetListHud extends HudElement {

    private final VoxelPtrCore core;
    private final int maxTargets;

    // 性能优化：缓存目标列表
    private List<Target> cachedTargets = new ArrayList<>();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 100; // 每100ms更新一次（10 FPS）

    public TargetListHud(VoxelPtrCore core, int x, int y) {
        super(x, y);
        this.core = core;
        this.maxTargets = 10; // 最多显示 10 个目标
    }

    /**
     * 获取方向字符串（东西南北）
     */
    private String getDirectionString(Entity player, Target target) {
        Vec3d playerPos = player.getPos();
        Vec3d targetPos = target.getPosition();

        double dx = targetPos.x - playerPos.x;
        double dz = targetPos.z - playerPos.z;
        double dy = targetPos.y - playerPos.y;

        // 计算水平方向
        String horizontal;
        if (Math.abs(dx) > Math.abs(dz)) {
            horizontal = dx > 0 ? "东" : "西";
        } else {
            horizontal = dz > 0 ? "南" : "北";
        }

        // 添加垂直方向
        String vertical = "";
        if (Math.abs(dy) > 2.0) { // 高度差超过2格才显示
            vertical = dy > 0 ? "↑" : "↓";
        }

        return vertical + horizontal;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        TargetTracker tracker = core.getTargetTracker();
        if (tracker == null) {
            return;
        }

        Entity player = client.player;

        // 性能优化：每100ms才更新一次目标列表
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL_MS) {
            cachedTargets = tracker.getNearestTargets(player, maxTargets);
            lastUpdateTime = currentTime;
        }

        if (cachedTargets.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;

        int yOffset = y;

        // 渲染模式和预设信息
        var clientCore = VoxelPtrClient.getClientCore();
        if (clientCore != null && clientCore.getKeyBindingManager() != null) {
            var keyManager = clientCore.getKeyBindingManager();
            String modeName = keyManager.getCurrentModeName();
            String presetName = keyManager.getCurrentPresetName();

            // 第一行：模式
            context.drawTextWithShadow(textRenderer, "§b" + modeName, x, yOffset, 0xFFFFFF);
            yOffset += 10;

            // 第二行：预设
            context.drawTextWithShadow(textRenderer, "§a" + presetName, x, yOffset, 0xFFFFFF);
            yOffset += 10;
        }

        // 第三行：目标数量
        String title = "目标: " + cachedTargets.size();
        context.drawTextWithShadow(textRenderer, title, x, yOffset, 0xFFFFFF);
        yOffset += 12;

        // 渲染每个目标（带方向指示）
        for (int i = 0; i < Math.min(cachedTargets.size(), maxTargets); i++) {
            Target target = cachedTargets.get(i);
            float distance = target.getDistanceTo(player);
            String direction = getDirectionString(player, target);

            // 格式: "钻石矿 东↓ 12.5m"
            String text = String.format("%s %s %.1fm",
                    target.getDisplayName(),
                    direction,
                    distance
            );

            int color = target.getColor();
            context.drawTextWithShadow(textRenderer, text, x, yOffset, color);

            yOffset += 10;
        }
    }

    @Override
    public String getName() {
        return "target_list";
    }
}
