package mod.gottsch.fabric.treasure2.core.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;
import mod.gottsch.fabric.gottschcore.config.IConfig;

/**
 * Created by Mark Gottschling on 5/18/2023
 */
@Modmenu(modId = "treasure2")
@Config(name = "treasure2", wrapperName = "MyConfig")
public class ConfigModel implements IConfig {

    public static ConfigModel instance = new ConfigModel();

    public ConfigModel() {}

//    @SectionHeader("properties")
}
