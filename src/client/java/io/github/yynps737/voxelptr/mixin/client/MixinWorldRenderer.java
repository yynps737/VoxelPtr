package io.github.yynps737.voxelptr.mixin.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.client.VoxelPtrClient;
import io.github.yynps737.voxelptr.client.VoxelPtrClientCore;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入 WorldRenderer
 * 在世界渲染时渲染指针
 */
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    /**
     * 注入世界渲染方法
     * 在渲染完世界后渲染我们的指针
     */
    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private void onRender(
            MatrixStack matrices,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        try {
            VoxelPtrClientCore clientCore = VoxelPtrClient.getClientCore();
            if (clientCore != null && clientCore.getPointerRenderer() != null) {
                // 渲染所有指针
                clientCore.getPointerRenderer().renderPointers(matrices, tickDelta);
            }
        } catch (Exception e) {
            VoxelPtr.LOGGER.error("渲染指针时出错", e);
        }
    }
}
