package org.foobar.mc.lokitas;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.foobar.mc.lokitas.client.LokitasLandMainMenu;
import org.foobar.mc.lokitas.config.ClientConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.loading.LogMarkers.CORE;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("lokitasmainmenu")
public class MainMenu
{
    private static final String MOD_ID = ModLoadingContext.get().getActiveContainer().getModInfo().getModId();

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public MainMenu() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM LokitasMenu");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
        final Path targetFile = Paths.get(FMLPaths.CONFIGDIR.get().toString(),String.format("%s.toml",MOD_ID));

        try {
            if (!Files.exists(targetFile )) {
                InputStream io = getClass().getResourceAsStream(String.format("/META-INF/%s.toml",MOD_ID));

                Files.copy(
                        io,
                        targetFile ,
                        StandardCopyOption.REPLACE_EXISTING);

                IOUtils.closeQuietly(io);
            }
            ClientConfig.load();

        }catch (IOException ex){

        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("Nothing to do on the server side.");
    }

    @SubscribeEvent
    public void openGui(GuiOpenEvent e) {
        LOGGER.info(CORE,"Changing GUI to {}" + e.getGui().getClass().getSimpleName()  );
        if(e.getGui() instanceof MainMenuScreen){
            LokitasLandMainMenu menu = new LokitasLandMainMenu();
            e.setGui(menu);
        }
    }

}
