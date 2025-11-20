package io.github.yynps737.voxelptr.mixin.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.scanner.impl.ChunkEventScanner;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入 ClientWorld
 * 监听客户端方块更新事件，更新缓存
 */
@Mixin(ClientWorld.class)
public class MixinClientWorld {

    /**
     * 注入方块状态设置方法
     * 当方块状态改变时调用
     */
    @Inject(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            at = @At("RETURN")
    )
    private void onBlockUpdate(
            BlockPos pos,
            BlockState state,
            int flags,
            int maxUpdateDepth,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // 只在方块状态成功更新时处理
        if (cir.getReturnValue()) {
            try {
                VoxelPtrCore core = VoxelPtr.getCore();
                if (core != null && core.getScannerManager() != null) {
                    ChunkEventScanner blockScanner = core.getScannerManager().getBlockScanner();

                    if (blockScanner != null && blockScanner.isEnabled()) {
                        // 更新缓存
                        blockScanner.onBlockUpdate(pos, state);
                    }
                }
            } catch (Exception e) {
                VoxelPtr.LOGGER.error("处理方块更新事件时出错: {}", pos, e);
            }
        }
    }
}
