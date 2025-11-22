package io.github.yynps737.voxelptr.mixin.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.client.VoxelPtrClient;
import io.github.yynps737.voxelptr.client.VoxelPtrClientCore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入 InGameHud
 * 在 HUD 渲染时渲染我们的自定义 HUD 元素
 * 适配 Minecraft 1.21+ API (使用 RenderTickCounter)
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {

    /**
     * 注入 HUD 渲染方法
     * 1.21+ API: 使用 RenderTickCounter 替代 float tickDelta
     */
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            VoxelPtrClientCore clientCore = VoxelPtrClient.getClientCore();
            if (clientCore != null && clientCore.getHudManager() != null) {
                // 1.21+ API: 从 RenderTickCounter 获取 tickDelta
                float tickDelta = tickCounter.getTickDelta(true);
                // 渲染所有 HUD 元素
                clientCore.getHudManager().renderHud(context, tickDelta);
            }
        } catch (Exception e) {
            VoxelPtr.LOGGER.error("渲染 HUD 时出错", e);
        }
    }
}
