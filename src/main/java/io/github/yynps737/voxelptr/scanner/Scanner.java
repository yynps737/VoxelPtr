package io.github.yynps737.voxelptr.scanner;

import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 扫描器接口
 * 定义扫描行为的抽象
 *
 * @param <T> 目标类型，必须继承自 Target
 */
public interface Scanner<T extends Target> {

    /**
     * 异步扫描（推荐）
     * 在独立线程执行扫描，不阻塞主线程
     *
     * @param world 世界对象
     * @param center 扫描中心坐标
     * @param radiusChunks 扫描半径（区块）
     * @return 扫描结果的 Future
     */
    CompletableFuture<List<T>> scanAsync(World world, BlockPos center, int radiusChunks);

    /**
     * 同步扫描
     * 在当前线程立即执行扫描
     * 仅用于轻量级扫描（如实体扫描）
     *
     * @param world 世界对象
     * @param center 扫描中心坐标
     * @param radiusChunks 扫描半径（区块）
     * @return 扫描结果
     */
    List<T> scanSync(World world, BlockPos center, int radiusChunks);

    /**
     * 获取扫描器返回的目标类型
     *
     * @return 目标类型
     */
    TargetType getTargetType();

    /**
     * 检查扫描器是否启用
     *
     * @return true 如果启用
     */
    boolean isEnabled();

    /**
     * 设置扫描器启用状态
     *
     * @param enabled 是否启用
     */
    void setEnabled(boolean enabled);

    /**
     * 获取扫描器名称（用于日志和调试）
     *
     * @return 扫描器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
