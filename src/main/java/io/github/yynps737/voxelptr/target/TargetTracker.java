package io.github.yynps737.voxelptr.target;

import io.github.yynps737.voxelptr.target.types.BlockTarget;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 目标追踪器
 * 管理所有活跃目标的生命周期
 *
 * 功能：
 * - 添加和移除目标
 * - 自动清理过期目标
 * - 验证目标有效性
 * - 按距离排序
 */
public class TargetTracker {

    /**
     * 活跃目标映射（线程安全）
     * Key: 目标 UUID
     * Value: 目标对象
     */
    private final ConcurrentHashMap<UUID, Target> activeTargets;

    /**
     * 实体目标过期时间（毫秒）
     * 实体可能移动或消失，30秒后清理
     */
    private static final long ENTITY_EXPIRY_TIME_MS = 30_000;

    /**
     * 方块目标过期时间（毫秒）
     * 方块不会移动，10分钟后清理（防止内存泄漏）
     */
    private static final long BLOCK_EXPIRY_TIME_MS = 600_000; // 10分钟

    public TargetTracker() {
        this.activeTargets = new ConcurrentHashMap<>();
    }

    /**
     * 每 tick 调用，清理过期和无效的目标
     *
     * @param world 当前世界（用于验证目标有效性）
     */
    public void tick(World world) {
        long now = System.currentTimeMillis();

        // 移除过期和无效的目标（性能优化：简化逻辑，减少重复判断）
        activeTargets.values().removeIf(target -> {
            // 1. 过期检查：根据目标类型使用不同的过期时间
            long expiryTime = (target instanceof BlockTarget) ? BLOCK_EXPIRY_TIME_MS : ENTITY_EXPIRY_TIME_MS;
            if (now - target.getLastSeen() > expiryTime) {
                return true; // 移除
            }

            // 2. 有效性检查（isValid(world) 会内部调用 isValid()，无需重复检查）
            return !target.isValid(world);
        });
    }

    /**
     * 添加或更新目标
     *
     * @param target 目标对象
     */
    public void addOrUpdateTarget(Target target) {
        target.updateLastSeen();
        activeTargets.put(target.getId(), target);
    }

    /**
     * 批量添加目标
     *
     * @param targets 目标列表
     */
    public void addTargets(List<? extends Target> targets) {
        targets.forEach(this::addOrUpdateTarget);
    }

    /**
     * 移除目标
     *
     * @param targetId 目标 UUID
     * @return 是否成功移除
     */
    public boolean removeTarget(UUID targetId) {
        return activeTargets.remove(targetId) != null;
    }

    /**
     * 获取所有活跃目标
     *
     * @return 目标列表（新创建的列表，修改不影响原数据）
     */
    public List<Target> getActiveTargets() {
        return new ArrayList<>(activeTargets.values());
    }

    /**
     * 获取指定类型的目标
     *
     * @param type 目标类型
     * @return 该类型的所有目标
     */
    public List<Target> getTargetsByType(TargetType type) {
        return activeTargets.values().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 按距离排序获取目标
     *
     * @param viewer 观察者（通常是玩家）
     * @return 按距离从近到远排序的目标列表
     */
    public List<Target> getTargetsSortedByDistance(Entity viewer) {
        return activeTargets.values().stream()
                .sorted(Comparator.comparingDouble(t -> t.getSquaredDistanceTo(viewer)))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定距离内的目标
     *
     * @param viewer 观察者
     * @param maxDistance 最大距离
     * @return 距离内的目标列表
     */
    public List<Target> getTargetsWithinDistance(Entity viewer, float maxDistance) {
        double maxSquaredDistance = maxDistance * maxDistance;
        return activeTargets.values().stream()
                .filter(t -> t.getSquaredDistanceTo(viewer) <= maxSquaredDistance)
                .sorted(Comparator.comparingDouble(t -> t.getSquaredDistanceTo(viewer)))
                .collect(Collectors.toList());
    }

    /**
     * 获取最近的 N 个目标
     *
     * @param viewer 观察者
     * @param count 数量
     * @return 最近的目标列表
     */
    public List<Target> getNearestTargets(Entity viewer, int count) {
        return activeTargets.values().stream()
                .sorted(Comparator.comparingDouble(t -> t.getSquaredDistanceTo(viewer)))
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 获取活跃目标数量
     *
     * @return 目标数量
     */
    public int getTargetCount() {
        return activeTargets.size();
    }

    /**
     * 获取指定类型的目标数量
     *
     * @param type 目标类型
     * @return 该类型的目标数量
     */
    public int getTargetCount(TargetType type) {
        return (int) activeTargets.values().stream()
                .filter(t -> t.getType() == type)
                .count();
    }

    /**
     * 清空所有目标
     */
    public void clear() {
        activeTargets.clear();
    }

    /**
     * 清空指定类型的目标
     *
     * @param type 目标类型
     */
    public void clearType(TargetType type) {
        activeTargets.values().removeIf(target -> target.getType() == type);
        io.github.yynps737.voxelptr.VoxelPtr.LOGGER.info("已清空类型为 {} 的目标", type);
    }
}
