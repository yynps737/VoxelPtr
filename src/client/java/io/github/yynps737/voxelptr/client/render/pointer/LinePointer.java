package io.github.yynps737.voxelptr.client.render.pointer;

import io.github.yynps737.voxelptr.client.render.RenderUtil;
import io.github.yynps737.voxelptr.target.Target;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * 线条指针
 * 从玩家位置到目标绘制一条线
 */
public class LinePointer implements Pointer {

    private final float lineWidth;

    public LinePointer() {
        this(1.5f);
    }

    public LinePointer(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public void render(MatrixStack matrices, Target target, Vec3d cameraPos, float tickDelta) {
        // 获取目标颜色
        int color = target.getColor();
        float[] rgba = RenderUtil.argbToRgba(color);

        // 获取目标位置中心
        Vec3d targetPos = target.getPosition();

        // 绘制从相机到目标的线条
        RenderUtil.drawLineToTarget(
                matrices,
                targetPos,
                cameraPos,
                rgba[0], // red
                rgba[1], // green
                rgba[2], // blue
                rgba[3] * 0.5f, // alpha (更透明)
                lineWidth
        );
    }

    @Override
    public String getTypeName() {
        return "line";
    }
}
