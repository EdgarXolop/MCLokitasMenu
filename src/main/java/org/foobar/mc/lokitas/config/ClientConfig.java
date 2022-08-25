package org.foobar.mc.lokitas.config;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

import static net.minecraftforge.fml.loading.LogMarkers.CORE;

public class ClientConfig {


    private static final Logger LOGGER = LogManager.getLogger( ClientConfig.class );

    private static ClientConfig INSTANCE = new ClientConfig();

    private static final String MOD_ID = ModLoadingContext.get().getActiveContainer().getModInfo().getModId();

    private static ConfigSpec configSpec = new ConfigSpec();

    private CommentedFileConfig configData;

    static {
        configSpec.define( "menu_play", "Play");
        configSpec.define( "server_name", "localhost");
        configSpec.define( "server_ip", "localhost:25699" );
        configSpec.define( "lan", Boolean.TRUE);
    }

    private void loadFrom(final Path configFile)
    {
        configData = CommentedFileConfig.builder(configFile).sync().
                defaultResource(String.format("/META-INF/%s.toml",MOD_ID)).
                autosave().autoreload().
                writingMode(WritingMode.REPLACE).
                build();
        try
        {
            configData.load();
        }
        catch (ParsingException e)
        {
            throw new RuntimeException("Failed to load FML config from " + configFile.toString(), e);
        }
        if (!configSpec.isCorrect(configData)) {
            LOGGER.warn(CORE, "Configuration file {} is not correct. Correcting", configFile);
            configSpec.correct(configData, (action, path, incorrectValue, correctedValue) ->
                    LOGGER.warn(CORE, "Incorrect key {} was corrected from {} to {}", path, incorrectValue, correctedValue));
        }
        configData.save();
    }

    public static void load()
    {
        final Path configFile = Paths.get(FMLPaths.CONFIGDIR.get().toString(),String.format("%s.toml",MOD_ID));
        INSTANCE.loadFrom(configFile);
        FMLPaths.getOrCreateGameRelativePath(Paths.get(FMLConfig.defaultConfigPath()), "default config directory");
    }

    public static String getServerName() {
        return INSTANCE.configData.<String>get("server_name");
    }

    public static String getServerIP() {
        return INSTANCE.configData.<String>get("server_ip");
    }

    public static Boolean getLAN() {
        return INSTANCE.configData.<Boolean>get("lan");
    }

    public static String getPlayButtonText() {
        return INSTANCE.configData.<String>get("menu_play");
    }
}