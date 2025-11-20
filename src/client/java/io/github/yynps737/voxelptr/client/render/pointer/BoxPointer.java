package io.github.yynps737.voxelptr.client.render.pointer;

import io.github.yynps737.voxelptr.client.render.RenderUtil;
import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.types.BlockTarget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * 方框指针
 * 在目标周围渲染一个 3D 方框
 */
public class BoxPointer implements Pointer {

    private final float lineWidth;
    private final float expandSize;
    private final boolean filled;

    public BoxPointer() {
        this(2.0f, 0.005f, false);
    }

    public BoxPointer(float lineWidth, float expandSize, boolean filled) {
        this.lineWidth = lineWidth;
        this.expandSize = expandSize;
        this.filled = filled;
    }

    @Override
    public void render(MatrixStack matrices, Target target, Vec3d cameraPos, float tickDelta) {
        // 获取目标颜色
        int color = target.getColor();
        float[] rgba = RenderUtil.argbToRgba(color);

        // 对于方块目标，在方块周围渲染方框
        if (target instanceof BlockTarget blockTarget) {
            BlockPos pos = blockTarget.getBlockPos();

            // 渲染扩展的方框
            RenderUtil.drawExpandedBox(
                    matrices,
                    pos,
                    cameraPos,
                    expandSize,
                    rgba[0], // red
                    rgba[1], // green
                    rgba[2], // blue
                    rgba[3] * 0.6f, // alpha (稍微透明)
                    lineWidth
            );
        } else {
            // 对于其他类型的目标，在位置周围渲染方框
            Vec3d targetPos = target.getPosition();
            BlockPos blockPos = BlockPos.ofFloored(targetPos);

            RenderUtil.drawBox(
                    matrices,
                    blockPos,
                    cameraPos,
                    rgba[0],
                    rgba[1],
                    rgba[2],
                    rgba[3] * 0.6f,
                    lineWidth
            );
        }
    }

    @Override
    public String getTypeName() {
        return "box";
    }
}
