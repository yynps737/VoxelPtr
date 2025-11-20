package io.github.yynps737.voxelptr.client.render.pointer;

import io.github.yynps737.voxelptr.target.Target;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * 指针接口
 * 定义如何渲染一个目标
 */
public interface Pointer {

    /**
     * 渲染指针
     *
     * @param matrices 矩阵栈
     * @param target 目标
     * @param cameraPos 相机位置
     * @param tickDelta tick 差值
     */
    void render(MatrixStack matrices, Target target, Vec3d cameraPos, float tickDelta);

    /**
     * 获取指针类型名称
     *
     * @return 类型名称
     */
    String getTypeName();

    /**
     * 检查指针是否启用
     *
     * @return true 如果启用
     */
    default boolean isEnabled() {
        return true;
    }
}
