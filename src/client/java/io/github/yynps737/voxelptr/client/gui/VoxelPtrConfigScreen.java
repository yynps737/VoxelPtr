package io.github.yynps737.voxelptr.client.gui;

import io.github.yynps737.voxelptr.client.VoxelPtrClient;
import io.github.yynps737.voxelptr.config.VoxelPtrConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * VoxelPtr 配置界面
 * 专业级视觉设计 - 现代、优雅、流畅
 */
public class VoxelPtrConfigScreen extends Screen {

    // ========== 布局常量 ==========
    private static final int HEADER_HEIGHT = 50;
    private static final int FOOTER_HEIGHT = 40;
    private static final int SECTION_SPACING = 25;
    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_SPACING = 6;
    private static final int CATEGORY_HEADER_HEIGHT = 24;
    private static final int LEFT_MARGIN = 40;
    private static final int RIGHT_MARGIN = 40;

    // ========== 颜色方案 - 日式樱花风格 🌸 ==========
    private static final int COLOR_BACKGROUND = 0x90000000; // 半透明黑色背景
    private static final int COLOR_PANEL = 0xD01A0A14; // 深紫色面板（带一点粉色调）
    private static final int COLOR_ACCENT = 0xFFFFB7D5; // 樱花粉强调色 🌸
    private static final int COLOR_ACCENT_DIM = 0xFFFF8FB8; // 暗樱花粉
    private static final int COLOR_ACCENT_GLOW = 0xFFFF69B4; // 亮樱花粉（悬停效果）
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFE6F0; // 主文本（带粉色调的白色）
    private static final int COLOR_TEXT_SECONDARY = 0xFFE6B8D0; // 次要文本（淡粉色）
    private static final int COLOR_DIVIDER = 0x40FFB7D5; // 分割线（樱花粉半透明）
    private static final int COLOR_SUCCESS = 0xFFB8E6C9; // 成功（薄荷绿）
    private static final int COLOR_WARNING = 0xFFFFD4A3; // 警告（暖橙色）

    // ========== 状态 ==========
    private final Screen parent;
    private final VoxelPtrConfig config;
    private int scrollOffset = 0;
    private int contentHeight = 0;
    private final List<ConfigWidget> widgets = new ArrayList<>();

    // ========== 动画状态 ==========
    private float fadeInProgress = 0.0f;
    private static final float FADE_SPEED = 0.15f;

    // ========== 滚动条状态 ==========
    private boolean isDraggingScrollbar = false;
    private int dragStartY = 0;
    private int dragStartScroll = 0;

    public VoxelPtrConfigScreen(Screen parent) {
        super(Text.translatable("gui.voxelptr.config.title"));
        this.parent = parent;
        this.config = VoxelPtrClient.getClientCore().getServerCore().getConfig();
    }

    @Override
    protected void init() {
        super.init();
        widgets.clear();

        int contentWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
        int currentY = HEADER_HEIGHT + 10;

        // ========== 基础设置 ==========
        currentY = addCategoryHeader("gui.voxelptr.config.category.basic", currentY);

        // 启用/禁用 - 优雅的开关
        currentY = addToggleWidget(
            "gui.voxelptr.config.enabled",
            "gui.voxelptr.config.enabled.tooltip",
            config.isEnabled(),
            value -> config.setEnabled(value),
            currentY
        );

        // 当前预设 - 美观的选择器
        currentY = addCyclingWidget(
            "gui.voxelptr.config.preset",
            "gui.voxelptr.config.preset.tooltip",
            getPresetOptions(),
            config.getCurrentPreset(),
            value -> config.setCurrentPreset(value),
            currentY
        );

        currentY += SECTION_SPACING;

        // ========== 扫描设置 ==========
        currentY = addCategoryHeader("gui.voxelptr.config.category.scan", currentY);

        // 扫描半径
        currentY = addSliderWidget(
            "gui.voxelptr.config.scan_radius",
            "gui.voxelptr.config.scan_radius.tooltip",
            1, 10,
            config.getScanRadiusChunks(),
            value -> config.setScanRadiusChunks(value),
            value -> value + " chunks",
            currentY
        );

        // 扫描间隔
        currentY = addSliderWidget(
            "gui.voxelptr.config.scan_interval",
            "gui.voxelptr.config.scan_interval.tooltip",
            5, 100,
            config.getScanIntervalTicks(),
            value -> config.setScanIntervalTicks(value),
            value -> (value / 20.0f) + "s",
            currentY
        );

        // 异步扫描
        currentY = addToggleWidget(
            "gui.voxelptr.config.async_scan",
            "gui.voxelptr.config.async_scan.tooltip",
            config.isAsyncScan(),
            value -> config.setAsyncScan(value),
            currentY
        );

        currentY += SECTION_SPACING;

        // ========== HUD 设置 ==========
        currentY = addCategoryHeader("gui.voxelptr.config.category.hud", currentY);

        // HUD 启用
        currentY = addToggleWidget(
            "gui.voxelptr.config.hud_enabled",
            "gui.voxelptr.config.hud_enabled.tooltip",
            config.isHudEnabled(),
            value -> config.setHudEnabled(value),
            currentY
        );

        // HUD 位置
        currentY = addCyclingWidget(
            "gui.voxelptr.config.hud_position",
            "gui.voxelptr.config.hud_position.tooltip",
            getHudPositionOptions(),
            config.getHudPosition(),
            value -> config.setHudPosition(value),
            currentY
        );

        // 最大显示目标数
        currentY = addSliderWidget(
            "gui.voxelptr.config.max_targets",
            "gui.voxelptr.config.max_targets.tooltip",
            1, 50,
            config.getMaxHudTargets(),
            value -> config.setMaxHudTargets(value),
            value -> value + " targets",
            currentY
        );

        currentY += SECTION_SPACING;

        // ========== 底部按钮 ==========
        addBottomButtons();

        contentHeight = currentY + FOOTER_HEIGHT;
    }

    /**
     * 添加分类标题
     */
    private int addCategoryHeader(String translationKey, int y) {
        widgets.add(new CategoryHeaderWidget(translationKey, y));
        return y + CATEGORY_HEADER_HEIGHT;
    }

    /**
     * 添加切换开关
     */
    private int addToggleWidget(String labelKey, String tooltipKey, boolean initialValue,
                                  java.util.function.Consumer<Boolean> onChange, int y) {
        widgets.add(new ToggleConfigWidget(labelKey, tooltipKey, initialValue, onChange, y));
        return y + WIDGET_HEIGHT + WIDGET_SPACING;
    }

    /**
     * 添加滑块
     */
    private int addSliderWidget(String labelKey, String tooltipKey,
                                  int min, int max, int initialValue,
                                  java.util.function.Consumer<Integer> onChange,
                                  java.util.function.Function<Integer, String> formatter,
                                  int y) {
        widgets.add(new SliderConfigWidget(labelKey, tooltipKey, min, max, initialValue, onChange, formatter, y));
        return y + WIDGET_HEIGHT + WIDGET_SPACING;
    }

    /**
     * 添加循环按钮
     */
    private int addCyclingWidget(String labelKey, String tooltipKey,
                                   List<String> options, String initialValue,
                                   java.util.function.Consumer<String> onChange, int y) {
        widgets.add(new CyclingConfigWidget(labelKey, tooltipKey, options, initialValue, onChange, y));
        return y + WIDGET_HEIGHT + WIDGET_SPACING;
    }

    /**
     * 添加底部按钮
     */
    private void addBottomButtons() {
        int buttonWidth = 120;
        int buttonHeight = 20;
        int buttonY = height - 30;

        // 完成按钮 - 左侧
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.done").formatted(Formatting.BOLD),
            button -> this.close()
        ).dimensions(width / 2 - buttonWidth - 5, buttonY, buttonWidth, buttonHeight).build());

        // 重置按钮 - 右侧
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.voxelptr.config.reset").formatted(Formatting.YELLOW),
            button -> resetToDefaults()
        ).dimensions(width / 2 + 5, buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 动画淡入效果
        if (fadeInProgress < 1.0f) {
            fadeInProgress = Math.min(1.0f, fadeInProgress + FADE_SPEED);
        }

        // 1.20.2 修复：先调用父类渲染背景，再绘制我们的内容覆盖模糊层
        super.render(context, mouseX, mouseY, delta);

        // 渲染我们的自定义背景（覆盖模糊效果）
        context.fill(0, 0, width, height, COLOR_BACKGROUND);

        // 渲染主面板
        renderMainPanel(context);

        // 渲染标题
        renderHeader(context);

        // 渲染所有配置项
        renderConfigWidgets(context, mouseX, mouseY, delta);

        // 重新渲染按钮（因为被我们的背景覆盖了）
        for (var child : this.children()) {
            if (child instanceof net.minecraft.client.gui.widget.ClickableWidget widget) {
                widget.render(context, mouseX, mouseY, delta);
            }
        }

        // 渲染工具提示
        renderTooltips(context, mouseX, mouseY);
    }

    /**
     * 渲染主面板
     */
    private void renderMainPanel(DrawContext context) {
        int panelX = LEFT_MARGIN - 10;
        int panelY = HEADER_HEIGHT - 10;
        int panelWidth = width - LEFT_MARGIN - RIGHT_MARGIN + 20;
        int panelHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT + 20;

        // 主面板背景 - 带圆角效果
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, COLOR_PANEL);

        // 顶部装饰线
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, COLOR_ACCENT);
    }

    /**
     * 渲染标题
     */
    private void renderHeader(DrawContext context) {
        // 标题文本
        Text title = Text.translatable("gui.voxelptr.config.title").formatted(Formatting.BOLD);
        int titleWidth = textRenderer.getWidth(title);
        int titleX = width / 2 - titleWidth / 2;
        int titleY = 20;

        // 渲染标题（带发光效果）
        context.drawTextWithShadow(textRenderer, title, titleX, titleY, COLOR_ACCENT);

        // 副标题
        Text subtitle = Text.translatable("gui.voxelptr.config.subtitle").formatted(Formatting.GRAY, Formatting.ITALIC);
        int subtitleWidth = textRenderer.getWidth(subtitle);
        int subtitleX = width / 2 - subtitleWidth / 2;
        context.drawTextWithShadow(textRenderer, subtitle, subtitleX, titleY + 12, COLOR_TEXT_SECONDARY);
    }

    /**
     * 渲染配置组件
     */
    private void renderConfigWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        // 启用裁剪避免内容溢出
        int clipY = HEADER_HEIGHT;
        int clipHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT;

        // 启用裁剪区域
        context.enableScissor(0, clipY, width, clipY + clipHeight);

        // 渲染每个组件
        int contentWidth = width - LEFT_MARGIN - RIGHT_MARGIN - 20; // 为滚动条预留空间
        for (ConfigWidget widget : widgets) {
            widget.render(context, LEFT_MARGIN, widget.getY() - scrollOffset, contentWidth, mouseX, mouseY + scrollOffset, delta);
        }

        // 禁用裁剪
        context.disableScissor();

        // 渲染滚动条
        renderScrollbar(context, mouseX, mouseY);
    }

    /**
     * 渲染滚动条
     */
    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int maxScroll = Math.max(0, contentHeight - (height - HEADER_HEIGHT - FOOTER_HEIGHT));

        // 如果内容不需要滚动，不显示滚动条
        if (maxScroll <= 0) {
            return;
        }

        // 滚动条位置和尺寸
        int scrollbarX = width - RIGHT_MARGIN + 10;
        int scrollbarY = HEADER_HEIGHT;
        int scrollbarWidth = 6;
        int scrollbarHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT;

        // 滚动条轨道（暗色背景）
        context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0x40FFFFFF);

        // 计算滚动条手柄的大小和位置
        float viewportRatio = (float) scrollbarHeight / contentHeight;
        int handleHeight = Math.max(20, (int) (scrollbarHeight * viewportRatio));

        float scrollProgress = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0;
        int handleY = scrollbarY + (int) ((scrollbarHeight - handleHeight) * scrollProgress);

        // 检查鼠标是否悬停在滚动条上
        boolean hovered = mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                          mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;

        // 滚动条手柄（樱花粉，悬停时发光 ✨）
        int handleColor = hovered ? COLOR_ACCENT_GLOW : COLOR_ACCENT_DIM;
        context.fill(scrollbarX, handleY, scrollbarX + scrollbarWidth, handleY + handleHeight, handleColor);

        // 手柄边缘高光效果（樱花粉光晕）
        context.fill(scrollbarX, handleY, scrollbarX + 1, handleY + handleHeight, 0x60FFB7D5);
    }

    /**
     * 渲染工具提示
     */
    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        // 检查鼠标悬停在哪个组件上
        for (ConfigWidget widget : widgets) {
            if (widget.isHovered(mouseX, mouseY + scrollOffset)) {
                String tooltip = widget.getTooltip();
                if (tooltip != null && !tooltip.isEmpty()) {
                    // 渲染工具提示
                    context.drawTooltip(textRenderer, Text.translatable(tooltip), mouseX, mouseY);
                }
                break; // 只显示一个工具提示
            }
        }
    }

    /**
     * 重置为默认值
     */
    private void resetToDefaults() {
        // 创建新的默认配置并复制值
        VoxelPtrConfig defaults = new VoxelPtrConfig();

        config.setEnabled(defaults.isEnabled());
        config.setScanRadiusChunks(defaults.getScanRadiusChunks());
        config.setScanIntervalTicks(defaults.getScanIntervalTicks());
        config.setAsyncScan(defaults.isAsyncScan());
        config.setHudEnabled(defaults.isHudEnabled());
        config.setHudPosition(defaults.getHudPosition());
        config.setMaxHudTargets(defaults.getMaxHudTargets());

        // 重新初始化界面
        this.clearChildren();
        this.init();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 先处理父类按钮（完成和重置）
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0) { // 左键
            // 检查是否点击滚动条
            int maxScroll = Math.max(0, contentHeight - (height - HEADER_HEIGHT - FOOTER_HEIGHT));
            if (maxScroll > 0) {
                int scrollbarX = width - RIGHT_MARGIN + 10;
                int scrollbarY = HEADER_HEIGHT;
                int scrollbarWidth = 6;
                int scrollbarHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT;

                if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                    mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {
                    isDraggingScrollbar = true;
                    dragStartY = (int) mouseY;
                    dragStartScroll = scrollOffset;
                    return true;
                }
            }

            // 处理自定义组件点击
            for (ConfigWidget widget : widgets) {
                if (widget.isHovered((int) mouseX, (int) mouseY + scrollOffset)) {
                    widget.onClick((int) mouseX, (int) mouseY + scrollOffset);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 滚动支持
        int maxScroll = Math.max(0, contentHeight - (height - HEADER_HEIGHT - FOOTER_HEIGHT));
        scrollOffset = Math.max(0, Math.min(maxScroll, (int) (scrollOffset - verticalAmount * 10)));
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            // 拖动滚动条
            if (isDraggingScrollbar) {
                int maxScroll = Math.max(0, contentHeight - (height - HEADER_HEIGHT - FOOTER_HEIGHT));
                int scrollbarHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT;

                // 计算滚动条手柄高度
                float viewportRatio = (float) scrollbarHeight / contentHeight;
                int handleHeight = Math.max(20, (int) (scrollbarHeight * viewportRatio));

                // 根据鼠标移动距离计算滚动偏移
                int deltaMouseY = (int) mouseY - dragStartY;
                float scrollRatio = (float) deltaMouseY / (scrollbarHeight - handleHeight);
                int newScroll = dragStartScroll + (int) (maxScroll * scrollRatio);

                scrollOffset = Math.max(0, Math.min(maxScroll, newScroll));
                return true;
            }

            // 支持拖动滑块
            for (ConfigWidget widget : widgets) {
                if (widget instanceof SliderConfigWidget && widget.isHovered((int) mouseX, (int) mouseY + scrollOffset)) {
                    widget.onClick((int) mouseX, (int) mouseY + scrollOffset);
                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    // ========== 辅助方法 ==========

    private List<String> getPresetOptions() {
        return List.of(
            "diamond", "iron", "gold", "emerald", "ancient_debris",
            "coal", "redstone", "lapis", "copper", "quartz",
            "villager", "pillager", "enderman", "animals",
            "hostile", "player", "boss", "neutral"
        );
    }

    private List<String> getHudPositionOptions() {
        return List.of("top_left", "top_right", "bottom_left", "bottom_right");
    }

    // ========== 内部接口 - 配置组件 ==========

    private interface ConfigWidget {
        void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta);
        boolean isHovered(int mouseX, int mouseY);
        String getTooltip();
        int getY();
        void onClick(int mouseX, int mouseY);
    }

    /**
     * 分类标题组件
     */
    private class CategoryHeaderWidget implements ConfigWidget {
        private final String translationKey;
        private final int y;

        public CategoryHeaderWidget(String translationKey, int y) {
            this.translationKey = translationKey;
            this.y = y;
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta) {
            Text title = Text.translatable(translationKey).formatted(Formatting.BOLD);
            context.drawTextWithShadow(textRenderer, title, x, y, COLOR_ACCENT);

            // 装饰线
            int lineY = y + 11;
            context.fill(x, lineY, x + width, lineY + 1, COLOR_DIVIDER);
        }

        @Override
        public boolean isHovered(int mouseX, int mouseY) {
            return false;
        }

        @Override
        public String getTooltip() {
            return null;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void onClick(int mouseX, int mouseY) {
            // 标题不可点击
        }
    }

    /**
     * 切换开关组件
     */
    private class ToggleConfigWidget implements ConfigWidget {
        private final String labelKey;
        private final String tooltipKey;
        private boolean value;
        private final java.util.function.Consumer<Boolean> onChange;
        private final int y;

        public ToggleConfigWidget(String labelKey, String tooltipKey, boolean initialValue,
                                   java.util.function.Consumer<Boolean> onChange, int y) {
            this.labelKey = labelKey;
            this.tooltipKey = tooltipKey;
            this.value = initialValue;
            this.onChange = onChange;
            this.y = y;
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta) {
            // 标签
            Text label = Text.translatable(labelKey);
            context.drawTextWithShadow(textRenderer, label, x, y + 5, COLOR_TEXT_PRIMARY);

            // 开关按钮
            int toggleX = x + width - 40;
            int toggleY = y;
            int toggleWidth = 40;
            int toggleHeight = 20;

            // 背景
            int bgColor = value ? COLOR_ACCENT_DIM : 0xFF3A3A3A;
            context.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, bgColor);

            // 滑块
            int sliderX = value ? toggleX + toggleWidth - 18 : toggleX + 2;
            context.fill(sliderX, toggleY + 2, sliderX + 16, toggleY + 18,
                value ? COLOR_ACCENT : COLOR_TEXT_SECONDARY);

            // 文本
            String text = value ? "ON" : "OFF";
            int textWidth = textRenderer.getWidth(text);
            int textX = toggleX + (toggleWidth - textWidth) / 2;
            context.drawText(textRenderer, text, textX, y + 6, COLOR_TEXT_PRIMARY, false);
        }

        @Override
        public boolean isHovered(int mouseX, int mouseY) {
            int toggleX = LEFT_MARGIN + (width - LEFT_MARGIN - RIGHT_MARGIN) - 40;
            return mouseX >= toggleX && mouseX <= toggleX + 40 &&
                   mouseY >= y && mouseY <= y + 20;
        }

        @Override
        public String getTooltip() {
            return tooltipKey;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void onClick(int mouseX, int mouseY) {
            if (isHovered(mouseX, mouseY)) {
                value = !value;
                onChange.accept(value);
            }
        }
    }

    /**
     * 滑块组件
     */
    private class SliderConfigWidget implements ConfigWidget {
        private final String labelKey;
        private final String tooltipKey;
        private final int min;
        private final int max;
        private int value;
        private final java.util.function.Consumer<Integer> onChange;
        private final java.util.function.Function<Integer, String> formatter;
        private final int y;

        public SliderConfigWidget(String labelKey, String tooltipKey,
                                   int min, int max, int initialValue,
                                   java.util.function.Consumer<Integer> onChange,
                                   java.util.function.Function<Integer, String> formatter,
                                   int y) {
            this.labelKey = labelKey;
            this.tooltipKey = tooltipKey;
            this.min = min;
            this.max = max;
            this.value = initialValue;
            this.onChange = onChange;
            this.formatter = formatter;
            this.y = y;
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta) {
            // 标签
            Text label = Text.translatable(labelKey);
            context.drawTextWithShadow(textRenderer, label, x, y + 5, COLOR_TEXT_PRIMARY);

            // 滑块
            int sliderX = x + width / 2;
            int sliderWidth = width / 2 - 10;
            int sliderY = y + 5;

            // 滑块轨道
            context.fill(sliderX, sliderY + 4, sliderX + sliderWidth, sliderY + 6, COLOR_DIVIDER);

            // 滑块填充
            float progress = (float) (value - min) / (max - min);
            int fillWidth = (int) (sliderWidth * progress);
            context.fill(sliderX, sliderY + 4, sliderX + fillWidth, sliderY + 6, COLOR_ACCENT);

            // 滑块手柄
            int handleX = sliderX + fillWidth - 3;
            context.fill(handleX, sliderY, handleX + 6, sliderY + 10, COLOR_ACCENT);

            // 值文本
            String valueText = formatter.apply(value);
            int textWidth = textRenderer.getWidth(valueText);
            context.drawText(textRenderer, valueText, sliderX + sliderWidth - textWidth, y + 5,
                COLOR_ACCENT, false);
        }

        @Override
        public boolean isHovered(int mouseX, int mouseY) {
            int sliderX = LEFT_MARGIN + (width - LEFT_MARGIN - RIGHT_MARGIN) / 2;
            int sliderWidth = (width - LEFT_MARGIN - RIGHT_MARGIN) / 2 - 10;
            return mouseX >= sliderX && mouseX <= sliderX + sliderWidth &&
                   mouseY >= y && mouseY <= y + 20;
        }

        @Override
        public String getTooltip() {
            return tooltipKey;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void onClick(int mouseX, int mouseY) {
            if (isHovered(mouseX, mouseY)) {
                int sliderX = LEFT_MARGIN + (width - LEFT_MARGIN - RIGHT_MARGIN) / 2;
                int sliderWidth = (width - LEFT_MARGIN - RIGHT_MARGIN) / 2 - 10;

                // 计算新值
                float progress = (float) (mouseX - sliderX) / sliderWidth;
                progress = Math.max(0.0f, Math.min(1.0f, progress));
                int newValue = min + (int) (progress * (max - min));

                if (newValue != value) {
                    value = newValue;
                    onChange.accept(value);
                }
            }
        }
    }

    /**
     * 循环按钮组件
     */
    private class CyclingConfigWidget implements ConfigWidget {
        private final String labelKey;
        private final String tooltipKey;
        private final List<String> options;
        private String value;
        private final java.util.function.Consumer<String> onChange;
        private final int y;

        public CyclingConfigWidget(String labelKey, String tooltipKey,
                                    List<String> options, String initialValue,
                                    java.util.function.Consumer<String> onChange, int y) {
            this.labelKey = labelKey;
            this.tooltipKey = tooltipKey;
            this.options = options;
            this.value = initialValue;
            this.onChange = onChange;
            this.y = y;
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, float delta) {
            // 标签
            Text label = Text.translatable(labelKey);
            context.drawTextWithShadow(textRenderer, label, x, y + 5, COLOR_TEXT_PRIMARY);

            // 按钮
            int buttonWidth = width / 2 - 10;
            int buttonX = x + width / 2;
            int buttonY = y;

            // 按钮背景
            context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 20, 0xFF2A2A2A);
            context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 1, COLOR_ACCENT_DIM);

            // 当前值
            String displayValue = value;
            int textWidth = textRenderer.getWidth(displayValue);
            int textX = buttonX + (buttonWidth - textWidth) / 2;
            context.drawText(textRenderer, displayValue, textX, y + 6, COLOR_ACCENT, false);
        }

        @Override
        public boolean isHovered(int mouseX, int mouseY) {
            int buttonX = LEFT_MARGIN + (width - LEFT_MARGIN - RIGHT_MARGIN) / 2;
            int buttonWidth = (width - LEFT_MARGIN - RIGHT_MARGIN) / 2 - 10;
            return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                   mouseY >= y && mouseY <= y + 20;
        }

        @Override
        public String getTooltip() {
            return tooltipKey;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void onClick(int mouseX, int mouseY) {
            if (isHovered(mouseX, mouseY)) {
                // 循环到下一个选项
                int currentIndex = options.indexOf(value);
                int nextIndex = (currentIndex + 1) % options.size();
                value = options.get(nextIndex);
                onChange.accept(value);
            }
        }
    }
}
