package io.github.yynps737.voxelptr.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * 渲染工具类
 * 提供常用的渲染方法
 * 适配 Minecraft 1.21.5+ API (使用 VertexConsumerProvider + RenderLayer)
 */
public class RenderUtil {

    /**
     * 绘制 3D 方框
     * 使用 VertexConsumerProvider 和 RenderLayer.getLines()
     *
     * @param matrices 矩阵栈
     * @param consumers 顶点消费者提供者
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
            VertexConsumerProvider consumers,
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

        // 设置线条宽度
        RenderSystem.lineWidth(lineWidth);

        // 获取线条渲染层的顶点消费者
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 绘制方块的 12 条边
        drawBoxEdges(consumer, matrix, 0, 0, 0, 1, 1, 1, red, green, blue, alpha);

        matrices.pop();
    }

    /**
     * 简化版本：使用 Tessellator 直接绘制（用于没有 VertexConsumerProvider 的场景）
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

        RenderSystem.lineWidth(lineWidth);

        // 1.21.5+ API: 使用 MinecraftClient.getBufferBuilders()
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 绘制方块的 12 条边
        drawBoxEdges(consumer, matrix, 0, 0, 0, 1, 1, 1, red, green, blue, alpha);

        // 立即绘制
        immediate.draw();

        matrices.pop();
    }

    /**
     * 绘制实体碰撞箱
     *
     * @param matrices 矩阵栈
     * @param consumers 顶点消费者提供者
     * @param boundingBox 实体的碰撞箱
     * @param cameraPos 相机位置
     * @param red 红色
     * @param green 绿色
     * @param blue 蓝色
     * @param alpha 透明度
     * @param lineWidth 线条宽度
     */
    public static void drawEntityBox(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            net.minecraft.util.math.Box boundingBox,
            Vec3d cameraPos,
            float red,
            float green,
            float blue,
            float alpha,
            float lineWidth
    ) {
        matrices.push();

        // 计算碰撞箱中心相对于相机的位置
        double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0 - cameraPos.x;
        double centerY = (boundingBox.minY + boundingBox.maxY) / 2.0 - cameraPos.y;
        double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0 - cameraPos.z;

        matrices.translate(centerX, centerY, centerZ);

        RenderSystem.lineWidth(lineWidth);

        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 计算碰撞箱在局部坐标系中的大小
        float halfWidth = (float) (boundingBox.maxX - boundingBox.minX) / 2.0f;
        float halfHeight = (float) (boundingBox.maxY - boundingBox.minY) / 2.0f;
        float halfDepth = (float) (boundingBox.maxZ - boundingBox.minZ) / 2.0f;

        drawBoxEdges(consumer, matrix,
                -halfWidth, -halfHeight, -halfDepth,
                halfWidth, halfHeight, halfDepth,
                red, green, blue, alpha);

        matrices.pop();
    }

    /**
     * 简化版本：使用 Tessellator 直接绘制实体碰撞箱
     */
    public static void drawEntityBox(
            MatrixStack matrices,
            net.minecraft.util.math.Box boundingBox,
            Vec3d cameraPos,
            float red,
            float green,
            float blue,
            float alpha,
            float lineWidth
    ) {
        matrices.push();

        double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0 - cameraPos.x;
        double centerY = (boundingBox.minY + boundingBox.maxY) / 2.0 - cameraPos.y;
        double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0 - cameraPos.z;

        matrices.translate(centerX, centerY, centerZ);

        RenderSystem.lineWidth(lineWidth);

        // 1.21.5+ API: 使用 MinecraftClient.getBufferBuilders()
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float halfWidth = (float) (boundingBox.maxX - boundingBox.minX) / 2.0f;
        float halfHeight = (float) (boundingBox.maxY - boundingBox.minY) / 2.0f;
        float halfDepth = (float) (boundingBox.maxZ - boundingBox.minZ) / 2.0f;

        drawBoxEdges(consumer, matrix,
                -halfWidth, -halfHeight, -halfDepth,
                halfWidth, halfHeight, halfDepth,
                red, green, blue, alpha);

        immediate.draw();

        matrices.pop();
    }

    /**
     * 绘制带扩展的方框
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

        RenderSystem.lineWidth(lineWidth);

        // 1.21.5+ API: 使用 MinecraftClient.getBufferBuilders()
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float size = 1.0f + expand * 2;
        drawBoxEdges(consumer, matrix, 0, 0, 0, size, size, size, red, green, blue, alpha);

        immediate.draw();

        matrices.pop();
    }

    /**
     * 绘制从相机到目标的线条
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

        RenderSystem.lineWidth(lineWidth);

        // 1.21.5+ API: 使用 MinecraftClient.getBufferBuilders()
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 起点：相机位置（在渲染坐标系中为原点）
        float x1 = 0;
        float y1 = 0;
        float z1 = 0;

        // 终点：目标位置（相对于相机）
        float x2 = (float) (targetPos.x - cameraPos.x);
        float y2 = (float) (targetPos.y - cameraPos.y);
        float z2 = (float) (targetPos.z - cameraPos.z);

        // 1.21.5+ API: 使用 VertexConsumer 添加线条顶点
        // 线条需要法线向量
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).normal(0, 1, 0);
        consumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).normal(0, 1, 0);

        immediate.draw();

        matrices.pop();
    }

    /**
     * 绘制方框的 12 条边
     */
    private static void drawBoxEdges(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        // 底面 4 条边
        addLine(consumer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(consumer, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);

        // 顶面 4 条边
        addLine(consumer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(consumer, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(consumer, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);

        // 竖直 4 条边
        addLine(consumer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(consumer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    /**
     * 添加一条线
     * 1.21.5+ API: RenderLayer.getLines() 需要法线向量
     */
    private static void addLine(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        // 计算线条方向作为法线
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) {
            nx /= len;
            ny /= len;
            nz /= len;
        } else {
            ny = 1;
        }

        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz);
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(nx, ny, nz);
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
