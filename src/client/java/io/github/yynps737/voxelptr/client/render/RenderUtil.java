package io.github.yynps737.voxelptr.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * 渲染工具类
 * 提供常用的渲染方法
 */
public class RenderUtil {

    /**
     * 绘制 3D 方框
     *
     * @param matrices 矩阵栈
     * @param pos 方块位置
     * @param cameraPos 相机位置
     * @param red 红色 (0-1)
     * @param green 绿色 (0-1)
     * @param blue 蓝色 (0-1)
     * @param alpha 透明度 (0-1)
     * @param lineWidth 线条宽度
     */
    public static void drawBox(
            MatrixStack matrices,
            BlockPos pos,
            Vec3d cameraPos,
            float red,
            float green,
            float blue,
            float alpha,
            float lineWidth
    ) {
        matrices.push();

        // 转换到方块空间，减去相机位置
        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;

        matrices.translate(x, y, z);

        // 设置渲染状态 - 允许透过墙壁显示
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // 关键：禁用深度测试，允许透视
        RenderSystem.disableCull();
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 绘制方块的 12 条边
        drawBoxEdges(buffer, matrix, 0, 0, 0, 1, 1, 1, red, green, blue, alpha);

        tessellator.draw();

        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    /**
     * 绘制带扩展的方框
     *
     * @param matrices 矩阵栈
     * @param pos 方块位置
     * @param cameraPos 相机位置
     * @param expand 扩展大小
     * @param red 红色
     * @param green 绿色
     * @param blue 蓝色
     * @param alpha 透明度
     * @param lineWidth 线条宽度
     */
    public static void drawExpandedBox(
            MatrixStack matrices,
            BlockPos pos,
            Vec3d cameraPos,
            float expand,
            float red,
            float green,
            float blue,
            float alpha,
            float lineWidth
    ) {
        matrices.push();

        double x = pos.getX() - cameraPos.x - expand;
        double y = pos.getY() - cameraPos.y - expand;
        double z = pos.getZ() - cameraPos.z - expand;

        matrices.translate(x, y, z);

        // 设置渲染状态 - 允许透过墙壁显示
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // 关键：禁用深度测试，允许透视
        RenderSystem.disableCull();
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float size = 1.0f + expand * 2;
        drawBoxEdges(buffer, matrix, 0, 0, 0, size, size, size, red, green, blue, alpha);

        tessellator.draw();

        // 恢复渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    /**
     * 绘制从相机到目标的线条
     *
     * @param matrices 矩阵栈
     * @param targetPos 目标位置
     * @param cameraPos 相机位置
     * @param red 红色
     * @param green 绿色
     * @param blue 蓝色
     * @param alpha 透明度
     * @param lineWidth 线条宽度
     */
    public static void drawLineToTarget(
            MatrixStack matrices,
            Vec3d targetPos,
            Vec3d cameraPos,
            float red,
            float green,
            float blue,
            float alpha,
            float lineWidth
    ) {
        matrices.push();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 从相机位置到目标位置
        double x1 = -cameraPos.x;
        double y1 = -cameraPos.y;
        double z1 = -cameraPos.z;

        double x2 = targetPos.x - cameraPos.x;
        double y2 = targetPos.y - cameraPos.y;
        double z2 = targetPos.z - cameraPos.z;

        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1)
                .color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2)
                .color(red, green, blue, alpha);

        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    /**
     * 绘制方框的 12 条边
     */
    private static void drawBoxEdges(
            BufferBuilder buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        // 底面 4 条边
        addLine(buffer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(buffer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(buffer, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(buffer, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);

        // 顶面 4 条边
        addLine(buffer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(buffer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(buffer, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(buffer, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);

        // 竖直 4 条边
        addLine(buffer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(buffer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(buffer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(buffer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    /**
     * 添加一条线
     */
    private static void addLine(
            BufferBuilder buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
    }

    /**
     * 将 ARGB 颜色转换为 RGBA 分量
     *
     * @param argb ARGB 颜色值
     * @return [r, g, b, a] 数组，值在 0-1 范围内
     */
    public static float[] argbToRgba(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return new float[]{r, g, b, a};
    }
}
