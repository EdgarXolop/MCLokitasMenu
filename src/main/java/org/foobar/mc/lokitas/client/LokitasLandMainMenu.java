package org.foobar.mc.lokitas.client;

import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.foobar.mc.lokitas.config.ClientConfig;

@OnlyIn(Dist.CLIENT)
public class LokitasLandMainMenu extends MainMenuScreen {

    public LokitasLandMainMenu(){}

    @Override
    protected void init() {
        super.init();

        int i = 24;
        int j = this.height / 4 + 48;

        this.children.remove(this.buttons.get(0));
        this.buttons.remove(0);
        this.children.remove(this.buttons.get(0));
        this.buttons.remove(0);

        boolean flag = this.minecraft.allowsMultiplayer();
        Button.ITooltip button$itooltip = flag ? Button.NO_TOOLTIP : (p_238659_1_, p_238659_2_, p_238659_3_, p_238659_4_) -> {
            if (!p_238659_1_.active) {
                this.renderTooltip(p_238659_2_, this.minecraft.font.split(new TranslationTextComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), p_238659_3_, p_238659_4_);
            }
        };

        (this.addButton(new Button(this.width / 2 - 100, i + j * 1, 200, 20, new StringTextComponent(ClientConfig.getPlayButtonText()), (p_213095_1_) -> {
            this.minecraft.setScreen(new ConnectingScreen(this, this.minecraft, new ServerData(ClientConfig.getServerName(),ClientConfig.getServerIP(),ClientConfig.getLAN())));
        }, button$itooltip))).active = flag;


    }
}
