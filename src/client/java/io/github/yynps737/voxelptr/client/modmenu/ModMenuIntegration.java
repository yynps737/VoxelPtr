package io.github.yynps737.voxelptr.client.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.yynps737.voxelptr.client.gui.VoxelPtrConfigScreen;

/**
 * ModMenu 集成
 * 提供配置界面入口
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return VoxelPtrConfigScreen::new;
    }
}
