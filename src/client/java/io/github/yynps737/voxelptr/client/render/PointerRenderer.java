package io.github.yynps737.voxelptr.client.render;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.client.render.pointer.BoxPointer;
import io.github.yynps737.voxelptr.client.render.pointer.Pointer;
import io.github.yynps737.voxelptr.config.VoxelPtrConfig;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * 指针渲染器
 * 负责在世界中渲染所有目标的指针
 */
public class PointerRenderer {

    private final VoxelPtrCore core;
    private Pointer currentPointer;

    public PointerRenderer(VoxelPtrCore core) {
        this.core = core;
        // 默认使用方框指针
        this.currentPointer = new BoxPointer();
    }

    /**
     * 渲染所有目标的指针
     * 在世界渲染阶段调用
     *
     * @param matrices 矩阵栈
     * @param tickDelta tick 差值
     */
    public void renderPointers(MatrixStack matrices, float tickDelta) {
        // ========== 3D方框渲染已禁用 ==========
        // 原因：性能优化 + 用户不需要3D方框
        // 现在只使用HUD文字显示目标
        // 如需恢复，请取消下面代码的注释

        /*
        VoxelPtrConfig config = core.getConfig();

        if (!config.isEnabled()) {
            return; // Mod 已禁用
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }

        // 获取所有活跃目标
        TargetTracker tracker = core.getTargetTracker();
        if (tracker == null) {
            VoxelPtr.LOGGER.warn("TargetTracker 为 null，无法渲染指针");
            return;
        }

        List<Target> targets = tracker.getActiveTargets();
        if (targets.isEmpty()) {
            return;
        }

        VoxelPtr.LOGGER.debug("正在渲染 {} 个目标的指针", targets.size());

        // 获取相机位置
        Entity camera = client.gameRenderer.getCamera().getFocusedEntity();
        if (camera == null) {
            camera = client.player;
        }
        Vec3d cameraPos = camera.getCameraPosVec(tickDelta);

        // 获取最大渲染距离
        float maxDistance = config.getMaxDistance();

        // 渲染每个目标
        int renderedCount = 0;
        for (Target target : targets) {
            try {
                // 距离检查
                float distance = target.getDistanceTo(camera);
                if (distance > maxDistance) {
                    continue; // 超出渲染距离
                }

                // 渲染指针
                if (currentPointer != null && currentPointer.isEnabled()) {
                    currentPointer.render(matrices, target, cameraPos, tickDelta);
                    renderedCount++;
                }
            } catch (Exception e) {
                VoxelPtr.LOGGER.error("渲染目标指针时出错: {}", target, e);
            }
        }

        if (renderedCount > 0) {
            VoxelPtr.LOGGER.debug("成功渲染 {} 个指针", renderedCount);
        }
        */
    }

    /**
     * 设置当前指针样式
     *
     * @param pointer 新的指针实例
     */
    public void setPointer(Pointer pointer) {
        this.currentPointer = pointer;
        VoxelPtr.LOGGER.info("切换指针样式: {}", pointer.getTypeName());
    }

    /**
     * 获取当前指针
     *
     * @return 当前指针实例
     */
    public Pointer getCurrentPointer() {
        return currentPointer;
    }
}
