package io.github.yynps737.voxelptr.target;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * 目标抽象类
 * 表示一个可被追踪的世界目标（方块、实体等）
 */
public abstract class Target {

    protected final UUID id;
    protected volatile Vec3d position; // volatile 确保并发安全
    protected TargetType type;
    protected int priority;
    protected long lastSeen;
    protected boolean valid;

    public Target(TargetType type, Vec3d position) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.position = position;
        this.priority = 0;
        this.lastSeen = System.currentTimeMillis();
        this.valid = true;
    }

    /**
     * 获取显示名称
     */
    public abstract String getDisplayName();

    /**
     * 计算到观察者的距离
     */
    public float getDistanceTo(Entity viewer) {
        return (float) viewer.getPos().distanceTo(position);
    }

    /**
     * 计算到观察者的平方距离（性能优化）
     * 用于排序比较，避免 sqrt 计算
     */
    public double getSquaredDistanceTo(Entity viewer) {
        return viewer.getPos().squaredDistanceTo(position);
    }

    /**
     * 检查目标是否仍然有效
     * 子类实现具体的验证逻辑
     */
    public abstract boolean isValid(World world);

    /**
     * 获取渲染颜色（ARGB 格式）
     */
    public abstract int getColor();

    /**
     * 更新最后发现时间
     */
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    // ========== Getters and Setters ==========

    public UUID getId() {
        return id;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public TargetType getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return id.equals(target.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
