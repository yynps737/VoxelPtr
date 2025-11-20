package io.github.yynps737.voxelptr.mixin.client;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import io.github.yynps737.voxelptr.scanner.impl.ChunkEventScanner;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入 ClientChunkManager
 * 监听客户端区块加载事件，触发扫描
 */
@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    /**
     * 注入区块加载方法
     * 当客户端接收到区块数据时调用
     */
    @Inject(
            method = "loadChunkFromPacket",
            at = @At("RETURN")
    )
    private void onChunkLoad(
            int x,
            int z,
            PacketByteBuf buf,
            NbtCompound nbt,
            java.util.function.Consumer<ChunkData.BlockEntityVisitor> consumer,
            CallbackInfoReturnable<WorldChunk> cir
    ) {
        WorldChunk chunk = cir.getReturnValue();

        if (chunk != null) {
            try {
                VoxelPtrCore core = VoxelPtr.getCore();
                if (core != null && core.getScannerManager() != null) {
                    ChunkEventScanner blockScanner = core.getScannerManager().getBlockScanner();

                    if (blockScanner != null && blockScanner.isEnabled()) {
                        // 异步扫描区块
                        blockScanner.onChunkLoad(chunk);
                    }
                }
            } catch (Exception e) {
                VoxelPtr.LOGGER.error("处理区块加载事件时出错: {}", new ChunkPos(x, z), e);
            }
        }
    }
}
