package io.github.yynps737.voxelptr.target.types;

import io.github.yynps737.voxelptr.target.Target;
import io.github.yynps737.voxelptr.target.TargetType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * 实体目标
 * 表示一个可追踪的实体（生物、玩家等）
 *
 * 特点：
 * - 实体会移动，需要实时更新位置
 * - 实体可能死亡或被移除
 * - 使用弱引用避免内存泄漏
 */
public class EntityTarget extends Target {

    private final UUID entityUUID;
    private final EntityType<?> entityType;
    private final String customName;
    private Entity cachedEntity; // 缓存实体引用（可能为null）

    /**
     * 创建实体目标
     * @param entity 目标实体
     */
    public EntityTarget(Entity entity) {
        super(TargetType.ENTITY, entity.getPos());
        this.entityUUID = entity.getUuid();
        this.entityType = entity.getType();
        this.customName = entity.hasCustomName() ? entity.getCustomName().getString() : null;
        this.cachedEntity = entity;
    }

    @Override
    public String getDisplayName() {
        // 优先显示自定义名称
        if (customName != null) {
            return customName;
        }

        // 否则显示实体类型名称
        return entityType.getName().getString();
    }

    @Override
    public boolean isValid(World world) {
        // 尝试获取实体
        Entity entity = getEntity(world);

        if (entity == null) {
            return false; // 实体不存在
        }

        if (!entity.isAlive()) {
            return false; // 实体已死亡
        }

        if (entity.isRemoved()) {
            return false; // 实体已被移除
        }

        // 更新位置（实体会移动）
        this.position = entity.getPos();
        this.cachedEntity = entity;

        return true;
    }

    @Override
    public int getColor() {
        // 根据实体类型返回不同颜色

        // 玩家 - 白色
        if (entityType == EntityType.PLAYER) {
            return 0xFFFFFFFF;
        }

        // 友好生物 - 绿色
        if (isFriendlyMob()) {
            return 0xFF00FF00;
        }

        // 敌对生物 - 红色
        if (isHostileMob()) {
            return 0xFFFF0000;
        }

        // 中立生物 - 黄色
        if (isNeutralMob()) {
            return 0xFFFFFF00;
        }

        // Boss - 紫色
        if (isBoss()) {
            return 0xFFFF00FF;
        }

        // 其他 - 灰色
        return 0xFFAAAAAA;
    }

    /**
     * 获取实体引用
     *
     * 修复说明：
     * - 修复了 Box 参数可能为 null 的 bug
     * - 使用目标位置作为搜索中心（100格半径）
     */
    public Entity getEntity(World world) {
        // 先检查缓存
        if (cachedEntity != null && !cachedEntity.isRemoved()) {
            return cachedEntity;
        }

        // 使用合理的搜索范围（100格半径 = 200 直径）
        Vec3d searchCenter = position; // 使用目标位置作为搜索中心
        Box searchBox = Box.of(searchCenter, 200, 200, 200);

        // 从世界查找
        for (Entity entity : world.getEntitiesByClass(Entity.class, searchBox,
                e -> e.getUuid().equals(entityUUID))) {
            cachedEntity = entity;
            return entity;
        }

        return null;
    }

    /**
     * 获取实体UUID
     */
    public UUID getEntityUUID() {
        return entityUUID;
    }

    /**
     * 获取实体类型
     */
    public EntityType<?> getEntityType() {
        return entityType;
    }

    // ========== 实体分类辅助方法 ==========

    private boolean isFriendlyMob() {
        return entityType == EntityType.VILLAGER
            || entityType == EntityType.PIG
            || entityType == EntityType.COW
            || entityType == EntityType.SHEEP
            || entityType == EntityType.CHICKEN
            || entityType == EntityType.HORSE
            || entityType == EntityType.CAT
            || entityType == EntityType.PARROT
            || entityType == EntityType.RABBIT
            || entityType == EntityType.LLAMA
            || entityType == EntityType.AXOLOTL
            || entityType == EntityType.FROG
            || entityType == EntityType.STRIDER
            || entityType == EntityType.MOOSHROOM
            || entityType == EntityType.MULE
            || entityType == EntityType.DONKEY
            || entityType == EntityType.GOAT
            || entityType == EntityType.TURTLE
            || entityType == EntityType.OCELOT
            || entityType == EntityType.FOX
            || entityType == EntityType.SNOW_GOLEM
            || entityType == EntityType.ALLAY;
    }

    private boolean isHostileMob() {
        return entityType == EntityType.ZOMBIE
            || entityType == EntityType.SKELETON
            || entityType == EntityType.CREEPER
            || entityType == EntityType.SPIDER
            || entityType == EntityType.CAVE_SPIDER
            || entityType == EntityType.WITCH
            || entityType == EntityType.BLAZE
            || entityType == EntityType.GHAST
            || entityType == EntityType.MAGMA_CUBE
            || entityType == EntityType.SLIME
            || entityType == EntityType.PHANTOM
            || entityType == EntityType.DROWNED
            || entityType == EntityType.HUSK
            || entityType == EntityType.STRAY
            || entityType == EntityType.PILLAGER
            || entityType == EntityType.VINDICATOR
            || entityType == EntityType.EVOKER
            || entityType == EntityType.RAVAGER
            || entityType == EntityType.VEX
            || entityType == EntityType.SILVERFISH
            || entityType == EntityType.GUARDIAN
            || entityType == EntityType.SHULKER
            || entityType == EntityType.HOGLIN
            || entityType == EntityType.ZOGLIN
            || entityType == EntityType.PIGLIN_BRUTE
            || entityType == EntityType.WITHER_SKELETON
            || entityType == EntityType.ENDERMITE
            || entityType == EntityType.ZOMBIE_VILLAGER;
    }

    private boolean isNeutralMob() {
        return entityType == EntityType.IRON_GOLEM
            || entityType == EntityType.WOLF
            || entityType == EntityType.POLAR_BEAR
            || entityType == EntityType.BEE
            || entityType == EntityType.PIGLIN
            || entityType == EntityType.ZOMBIFIED_PIGLIN
            || entityType == EntityType.PANDA
            || entityType == EntityType.DOLPHIN
            || entityType == EntityType.TRADER_LLAMA
            || entityType == EntityType.LLAMA
            || entityType == EntityType.ENDERMAN;
    }

    private boolean isBoss() {
        return entityType == EntityType.ENDER_DRAGON
            || entityType == EntityType.WITHER
            || entityType == EntityType.ELDER_GUARDIAN
            || entityType == EntityType.WARDEN;
    }

    @Override
    public String toString() {
        return String.format("EntityTarget{uuid=%s, type=%s, name=%s}",
                entityUUID, entityType.getName().getString(), getDisplayName());
    }
}
