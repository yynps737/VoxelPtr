package io.github.yynps737.voxelptr.client.hud;

import io.github.yynps737.voxelptr.VoxelPtr;
import io.github.yynps737.voxelptr.core.VoxelPtrCore;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD 管理器
 * 管理所有 HUD 元素的显示
 */
public class HudManager {

    private final VoxelPtrCore core;
    private final List<HudElement> elements;

    public HudManager(VoxelPtrCore core) {
        this.core = core;
        this.elements = new ArrayList<>();

        initializeHudElements();
    }

    /**
     * 初始化 HUD 元素
     */
    private void initializeHudElements() {
        // 创建目标列表 HUD（左上角）
        TargetListHud targetListHud = new TargetListHud(core, 5, 5);
        elements.add(targetListHud);

        VoxelPtr.LOGGER.info("初始化了 {} 个 HUD 元素", elements.size());
    }

    /**
     * 渲染所有 HUD 元素
     *
     * @param context 绘制上下文
     * @param tickDelta tick 差值
     */
    public void renderHud(DrawContext context, float tickDelta) {
        if (!core.getConfig().isEnabled() || !core.getConfig().isHudEnabled()) {
            return; // HUD 已禁用
        }

        for (HudElement element : elements) {
            try {
                if (element.isEnabled()) {
                    element.render(context, tickDelta);
                }
            } catch (Exception e) {
                VoxelPtr.LOGGER.error("渲染 HUD 元素 {} 时出错", element.getName(), e);
            }
        }
    }

    /**
     * 获取指定名称的 HUD 元素
     *
     * @param name 元素名称
     * @return HUD 元素，如果不存在返回 null
     */
    public HudElement getElement(String name) {
        return elements.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加 HUD 元素
     *
     * @param element HUD 元素
     */
    public void addElement(HudElement element) {
        elements.add(element);
    }

    /**
     * 移除 HUD 元素
     *
     * @param name 元素名称
     * @return true 如果成功移除
     */
    public boolean removeElement(String name) {
        return elements.removeIf(e -> e.getName().equals(name));
    }

    /**
     * 获取所有 HUD 元素
     *
     * @return HUD 元素列表
     */
    public List<HudElement> getAllElements() {
        return new ArrayList<>(elements);
    }
}
