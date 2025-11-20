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
     * 获取方向字符串（基于玩家局部坐标系的向量投影算法）
     * 
     * 核心原理：
     * 1. 计算世界坐标系下的相对位移向量 (Delta Vector)
     * 2. 构建玩家的局部基向量 (Local Basis Vectors): 前方(Forward) 和 右方(Right)
     * 3. 使用点积 (Dot Product) 将位移向量投影到局部基向量上，得到相对距离
     */
    private String getDirectionString(Entity player, Target target) {
        // 1. 获取世界坐标系下的相对位移 (World Space Delta)
        Vec3d playerPos = player.getPos();
        Vec3d targetPos = target.getPosition();
        
        double dx = targetPos.x - playerPos.x;
        double dy = targetPos.y - playerPos.y;
        double dz = targetPos.z - playerPos.z;

        // 2. 构建玩家局部坐标系的基向量 (Local Basis Vectors)
        // Minecraft Yaw 定义: 0=南(+Z), -90=东(+X), 90=西(-X), 180=北(-Z)
        float yaw = player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);

        // 前方向量 (Forward Vector): 玩家视线在水平面的单位向量
        // 推导: Yaw=0(南) -> (0, 1); Yaw=-90(东) -> (1, 0)
        // 公式: x = -sin(yaw), z = cos(yaw)
        double fwdX = -sin;
        double fwdZ = cos;

        // 右方向量 (Right Vector): 垂直于前方向量，指向玩家右手边
        // 推导: 顺时针旋转90度。Yaw=0(南) -> 右手是西(-X) -> (-1, 0)
        // 公式: x = -cos(yaw), z = -sin(yaw)
        double rightX = -cos;
        double rightZ = -sin;

        // 3. 向量投影 (Vector Projection)
        // 使用点积计算目标在玩家局部坐标轴上的分量
        // dist = V_delta · V_basis
        
        double forwardDist = (dx * fwdX) + (dz * fwdZ); // 正数=前，负数=后
        double rightDist   = (dx * rightX) + (dz * rightZ); // 正数=右，负数=左
        
        // 4. 生成可视化指示
        // 阈值设为 1.0，避免在极近距离时方向指示跳动
        String upDown = "-";
        if (dy > 1.0) upDown = "↑";       // 上 (Y轴正方向)
        else if (dy < -1.0) upDown = "↓"; // 下 (Y轴负方向)

        String leftRight = "-";
        if (rightDist > 1.0) leftRight = "→";      // 右 (在右方向量上有正投影)
        else if (rightDist < -1.0) leftRight = "←"; // 左 (在右方向量上有负投影)

        String frontBack = "-";
        if (forwardDist > 1.0) frontBack = "↑";      // 前 (在前方向量上有正投影)
        else if (forwardDist < -1.0) frontBack = "↓"; // 后 (在前方向量上有负投影)

        return "上下:" + upDown + " 左右:" + leftRight + " 前后:" + frontBack;
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

        // 计算最长名称的字符数（用于对齐）
        int maxNameLength = 0;
        for (Target target : cachedTargets) {
            int nameLength = target.getDisplayName().length();
            if (nameLength > maxNameLength) {
                maxNameLength = nameLength;
            }
        }

        // 渲染每个目标（带方向指示）
        for (int i = 0; i < Math.min(cachedTargets.size(), maxTargets); i++) {
            Target target = cachedTargets.get(i);
            float distance = target.getDistanceTo(player);
            String direction = getDirectionString(player, target);

            // 使用固定宽度格式化名称，左对齐并填充空格
            // 格式: "钻石矿石     👆↑ 12.5m"
            String text = String.format("%-" + maxNameLength + "s %s %.1fm",
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
