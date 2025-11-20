package io.github.yynps737.voxelptr.scanner.impl;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.scanner.Scanner;
import io.github.yynps737.voxelptr.target.TargetType;
import io.github.yynps737.voxelptr.target.types.EntityTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 实体扫描器
 *
 * 特点：
 * - 在主线程执行（实体API不是线程安全的）
 * - 轻量级扫描，不需要缓存
 * - 支持按实体类型过滤
 *
 * 注意：
 * 实体会移动和消失，所以不缓存扫描结果
 * 每次都重新扫描
 */
public class EntityScanner implements Scanner<EntityTarget> {

    private boolean enabled;
    private Set<EntityType<?>> targetEntityTypes;

    /**
     * 创建实体扫描器
     * @param targetEntityTypes 目标实体类型集合
     */
    public EntityScanner(Set<EntityType<?>> targetEntityTypes) {
        this.enabled = true;
        this.targetEntityTypes = targetEntityTypes;
        VoxelPtr.LOGGER.info("EntityScanner 已创建，目标类型数: {}", targetEntityTypes.size());
    }

    /**
     * 同步扫描（主线程）
     */
    @Override
    public List<EntityTarget> scanSync(World world, BlockPos center, int radiusChunks) {
        return scanSyncWithExclusion(world, center, radiusChunks, null);
    }

    /**
     * 同步扫描（主线程，支持排除指定实体）
     *
     * @param world 世界对象
     * @param center 扫描中心
     * @param radiusChunks 扫描半径（区块）
     * @param excludeEntity 要排除的实体（通常是玩家自己），null表示不排除
     * @return 扫描结果
     */
    public List<EntityTarget> scanSyncWithExclusion(World world, BlockPos center, int radiusChunks, Entity excludeEntity) {
        if (!enabled || world == null) {
            return new ArrayList<>();
        }

        // 将区块半径转换为方块半径
        int radiusBlocks = radiusChunks * 16;

        // 垂直扫描范围（从玩家上下各32格）
        int verticalRadius = 32;

        // 创建扫描边界框
        Vec3d centerVec = Vec3d.ofCenter(center);
        Box searchBox = Box.of(centerVec,
            radiusBlocks * 2,    // X方向直径
            verticalRadius * 2,  // Y方向直径（更小）
            radiusBlocks * 2);   // Z方向直径

        // 扫描实体
        List<EntityTarget> targets = new ArrayList<>();

        try {
            // 获取边界框内的所有实体（排除指定实体）
            List<Entity> entities = world.getOtherEntities(excludeEntity, searchBox, entity -> {
                // 过滤条件：
                // 1. 实体存活
                // 2. 实体类型匹配
                // 3. 未被移除
                return entity != null
                    && entity.isAlive()
                    && !entity.isRemoved()
                    && targetEntityTypes.contains(entity.getType());
            });

            // 转换为 EntityTarget
            for (Entity entity : entities) {
                targets.add(new EntityTarget(entity));
            }

            if (!targets.isEmpty()) {
                VoxelPtr.LOGGER.debug("实体扫描完成，找到 {} 个目标", targets.size());
            }

        } catch (Exception e) {
            VoxelPtr.LOGGER.error("实体扫描时出错", e);
        }

        return targets;
    }

    /**
     * 异步扫描（立即返回，实际是同步执行）
     *
     * 注意：由于实体API必须在主线程调用，这里直接同步执行并返回完成的Future
     */
    @Override
    public CompletableFuture<List<EntityTarget>> scanAsync(World world, BlockPos center, int radiusChunks) {
        // 实体扫描必须在主线程，所以直接同步执行
        List<EntityTarget> result = scanSync(world, center, radiusChunks);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.ENTITY;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        VoxelPtr.LOGGER.info("EntityScanner {}", enabled ? "已启用" : "已禁用");
    }

    /**
     * 设置目标实体类型
     */
    public void setTargetEntityTypes(Set<EntityType<?>> targetEntityTypes) {
        this.targetEntityTypes = targetEntityTypes;
        VoxelPtr.LOGGER.info("EntityScanner 目标类型已更新，数量: {}", targetEntityTypes.size());
    }

    /**
     * 获取当前目标实体类型
     */
    public Set<EntityType<?>> getTargetEntityTypes() {
        return targetEntityTypes;
    }

    @Override
    public String getName() {
        return "EntityScanner";
    }
}
