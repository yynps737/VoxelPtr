package io.github.yynps737.voxelptr.client.hud;

import net.minecraft.client.gui.DrawContext;

/**
 * HUD 元素基类
 * 所有 HUD 显示组件的基类
 */
public abstract class HudElement {

    protected boolean enabled;
    protected int x;
    protected int y;

    public HudElement(int x, int y) {
        this.x = x;
        this.y = y;
        this.enabled = true;
    }

    /**
     * 渲染 HUD 元素
     *
     * @param context 绘制上下文
     * @param tickDelta tick 差值
     */
    public abstract void render(DrawContext context, float tickDelta);

    /**
     * 获取元素名称
     *
     * @return 元素名称
     */
    public abstract String getName();

    /**
     * 检查是否启用
     *
     * @return true 如果启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置位置
     *
     * @param x X 坐标
     * @param y Y 坐标
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 获取 X 坐标
     */
    public int getX() {
        return x;
    }

    /**
     * 获取 Y 坐标
     */
    public int getY() {
        return y;
    }
}
