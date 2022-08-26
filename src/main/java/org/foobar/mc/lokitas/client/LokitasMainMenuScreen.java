package org.foobar.mc.lokitas.client;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.foobar.mc.lokitas.config.ClientConfig;

import static net.minecraftforge.fml.VersionChecker.Status.BETA;
import static net.minecraftforge.fml.VersionChecker.Status.BETA_OUTDATED;

@OnlyIn(Dist.CLIENT)
public class LokitasMainMenuScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final RenderSkyboxCube CUBE_MAP = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private boolean minceraftEasterEgg;
    @Nullable
    private String splash;
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private boolean realmsNotificationsInitialized;
    private Screen realmsNotificationsScreen;
    private int copyrightWidth;
    private int copyrightX;
    private final RenderSkybox panorama = new RenderSkybox(CUBE_MAP);
    private boolean fading;
    private long fadeInStart;
    private net.minecraftforge.client.gui.NotificationModUpdateScreen modUpdateNotification;

    public LokitasMainMenuScreen() {
        this(false);
    }

    public LokitasMainMenuScreen(boolean p_i51107_1_) {
        super(new TranslationTextComponent("narrator.screen.title"));
        this.fading = p_i51107_1_;
        this.minceraftEasterEgg = (double)(new Random()).nextFloat() < 1.0E-4D;
    }

    private boolean realmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen != null;
    }

    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }

    }

    public static CompletableFuture<Void> preloadResources(TextureManager p_213097_0_, Executor p_213097_1_) {
        return CompletableFuture.allOf(p_213097_0_.preload(MINECRAFT_LOGO, p_213097_1_), p_213097_0_.preload(MINECRAFT_EDITION, p_213097_1_), p_213097_0_.preload(PANORAMA_OVERLAY, p_213097_1_), CUBE_MAP.preload(p_213097_0_, p_213097_1_));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }

        this.copyrightWidth = this.font.width("Copyright Mojang AB. Do not distribute!");
        this.copyrightX = this.width - this.copyrightWidth - 2;
        int i = 24;
        int j = this.height / 4 + 48;
        Button modButton = null;
        this.createNormalMenuOptions(j, i);
        modButton = this.addButton(new Button(this.width / 2 - 100, j + 24 * 2, 98, 20, new TranslationTextComponent("fml.menu.mods"), button -> {
            this.minecraft.setScreen(new net.minecraftforge.fml.client.gui.screen.ModListScreen(this));
        }));

        modUpdateNotification = init(this, modButton);

        this.addButton(new ImageButton(this.width / 2 - 124, j + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (p_213090_1_) -> {
            this.minecraft.setScreen(new LanguageScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()));
        }, new TranslationTextComponent("narrator.button.language")));
        this.addButton(new Button(this.width / 2 - 100, j + 72 + 12, 98, 20, new TranslationTextComponent("menu.options"), (p_213096_1_) -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));
        this.addButton(new Button(this.width / 2 + 2, j + 72 + 12, 98, 20, new TranslationTextComponent("menu.quit"), (p_213094_1_) -> {
            this.minecraft.stop();
        }));
        this.addButton(new ImageButton(this.width / 2 + 104, j + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64, (p_213088_1_) -> {
            this.minecraft.setScreen(new AccessibilityScreen(this, this.minecraft.options));
        }, new TranslationTextComponent("narrator.button.accessibility")));
        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications && !this.realmsNotificationsInitialized) {
            RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
            this.realmsNotificationsScreen = realmsbridgescreen.getNotificationScreen(this);
            this.realmsNotificationsInitialized = true;
        }

        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }

    }

    private void createNormalMenuOptions(int p_73969_1_, int p_73969_2_) {
        boolean flag = this.minecraft.allowsMultiplayer();
        Button.ITooltip button$itooltip = flag ? Button.NO_TOOLTIP : (p_238659_1_, p_238659_2_, p_238659_3_, p_238659_4_) -> {
            if (!p_238659_1_.active) {
                this.renderTooltip(p_238659_2_, this.minecraft.font.split(new TranslationTextComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), p_238659_3_, p_238659_4_);
            }

        };
        (this.addButton(new Button(this.width / 2 - 100, p_73969_1_, 200, 20, new TranslationTextComponent(ClientConfig.getPlayButtonText()), (p_213095_1_) -> {
            ServerData serverData = new ServerData(ClientConfig.getServerName(),ClientConfig.getServerIP(),ClientConfig.getLAN());
            this.minecraft.setScreen(new ConnectingScreen(this, this.minecraft, serverData ));
        }, button$itooltip))).active = flag;
        (this.addButton(new Button(this.width / 2 + 2, p_73969_1_ + p_73969_2_ * 2, 98, 20, new TranslationTextComponent("menu.online"), (p_238661_1_) -> {
            this.realmsButtonClicked();
        }, button$itooltip))).active = flag;
    }

    private void realmsButtonClicked() {
        RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
        realmsbridgescreen.switchToRealms(this);
    }

    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        fill(p_230430_1_, 0, 0, this.width, this.height, -1);
        this.panorama.render(p_230430_4_, MathHelper.clamp(f, 0.0F, 1.0F));
        int i = 274;
        int j = this.width / 2 - 137;
        int k = 30;
        this.minecraft.getTextureManager().bind(PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.fading ? (float)MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
        blit(p_230430_1_, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float f1 = this.fading ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(f1 * 255.0F) << 24;
        if ((l & -67108864) != 0) {
            this.minecraft.getTextureManager().bind(MINECRAFT_LOGO);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);
            if (this.minceraftEasterEgg) {
                this.blitOutlineBlack(j, 30, (p_238660_2_, p_238660_3_) -> {
                    this.blit(p_230430_1_, p_238660_2_ + 0, p_238660_3_, 0, 0, 99, 44);
                    this.blit(p_230430_1_, p_238660_2_ + 99, p_238660_3_, 129, 0, 27, 44);
                    this.blit(p_230430_1_, p_238660_2_ + 99 + 26, p_238660_3_, 126, 0, 3, 44);
                    this.blit(p_230430_1_, p_238660_2_ + 99 + 26 + 3, p_238660_3_, 99, 0, 26, 44);
                    this.blit(p_230430_1_, p_238660_2_ + 155, p_238660_3_, 0, 45, 155, 44);
                });
            } else {
                this.blitOutlineBlack(j, 30, (p_238657_2_, p_238657_3_) -> {
                    this.blit(p_230430_1_, p_238657_2_ + 0, p_238657_3_, 0, 0, 155, 44);
                    this.blit(p_230430_1_, p_238657_2_ + 155, p_238657_3_, 0, 45, 155, 44);
                });
            }

            this.minecraft.getTextureManager().bind(MINECRAFT_EDITION);
            blit(p_230430_1_, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
            renderMainMenu(this, p_230430_1_, this.font, this.width, this.height, l);
            if (this.splash != null) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
                RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
                float f2 = 1.8F - MathHelper.abs(MathHelper.sin((float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
                f2 = f2 * 100.0F / (float)(this.font.width(this.splash) + 32);
                RenderSystem.scalef(f2, f2, f2);
                drawCenteredString(p_230430_1_, this.font, this.splash, 0, -8, 16776960 | l);
                RenderSystem.popMatrix();
            }

            String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                s = s + " Demo";
            } else {
                s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (this.minecraft.isProbablyModded()) {
                s = s + I18n.get("menu.modded");
            }

            net.minecraftforge.fml.BrandingControl.forEachLine(true, true, (brdline, brd) ->
                    drawString(p_230430_1_, this.font, brd, 2, this.height - ( 10 + brdline * (this.font.lineHeight + 1)), 16777215 | l)
            );

            net.minecraftforge.fml.BrandingControl.forEachAboveCopyrightLine((brdline, brd) ->
                    drawString(p_230430_1_, this.font, brd, this.width - font.width(brd), this.height - (10 + (brdline + 1) * ( this.font.lineHeight + 1)), 16777215 | l)
            );

            drawString(p_230430_1_, this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 16777215 | l);
            if (p_230430_2_ > this.copyrightX && p_230430_2_ < this.copyrightX + this.copyrightWidth && p_230430_3_ > this.height - 10 && p_230430_3_ < this.height) {
                fill(p_230430_1_, this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 16777215 | l);
            }

            for(Widget widget : this.buttons) {
                widget.setAlpha(f1);
            }

            super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
            if (this.realmsNotificationsEnabled() && f1 >= 1.0F) {
                this.realmsNotificationsScreen.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
            }
            modUpdateNotification.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);

        }
    }

    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        if (super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_)) {
            return true;
        } else if (this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_)) {
            return true;
        } else {
            if (p_231044_1_ > (double)this.copyrightX && p_231044_1_ < (double)(this.copyrightX + this.copyrightWidth) && p_231044_3_ > (double)(this.height - 10) && p_231044_3_ < (double)this.height) {
                this.minecraft.setScreen(new WinGameScreen(false, Runnables.doNothing()));
            }

            return false;
        }
    }

    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }

    }

    public static NotificationModUpdateScreen init(LokitasMainMenuScreen guiMainMenu, Button modButton)
    {
        NotificationModUpdateScreen notificationModUpdateScreen = new NotificationModUpdateScreen(modButton);
        notificationModUpdateScreen.resize(guiMainMenu.getMinecraft(), guiMainMenu.width, guiMainMenu.height);
        notificationModUpdateScreen.init();
        return notificationModUpdateScreen;
    }

    public static void renderMainMenu(LokitasMainMenuScreen gui, MatrixStack mStack, FontRenderer font, int width, int height, int alpha)
    {
        VersionChecker.Status status = ForgeVersion.getStatus();
        if (status == BETA || status == BETA_OUTDATED)
        {
            // render a warning at the top of the screen,
            ITextComponent line = new TranslationTextComponent("forge.update.beta.1", TextFormatting.RED, TextFormatting.RESET).withStyle(TextFormatting.RED);
            AbstractGui.drawCenteredString(mStack, font, line, width / 2, 4 + (0 * (font.lineHeight + 1)), 0xFFFFFF | alpha);
            line = new TranslationTextComponent("forge.update.beta.2");
            AbstractGui.drawCenteredString(mStack, font, line, width / 2, 4 + (1 * (font.lineHeight + 1)), 0xFFFFFF | alpha);
        }

        String line = null;
        switch(status)
        {
            //case FAILED:        line = " Version check failed"; break;
            //case UP_TO_DATE:    line = "Forge up to date"}; break;
            //case AHEAD:         line = "Using non-recommended Forge build, issues may arise."}; break;
            case OUTDATED:
            case BETA_OUTDATED: line = I18n.get("forge.update.newversion", ForgeVersion.getTarget()); break;
            default: break;
        }
    }
}
