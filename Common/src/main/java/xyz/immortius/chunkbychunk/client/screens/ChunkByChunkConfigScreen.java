package xyz.immortius.chunkbychunk.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.immortius.chunkbychunk.client.uielements.SettingListWidget;
import xyz.immortius.chunkbychunk.common.util.ConfigUtil;

public class ChunkByChunkConfigScreen extends Screen {
    private final Screen lastScreen;
    private SettingListWidget settingsList;
    private Button cancelButton;
    private Button resetButton;
    private Button saveButton;

    public ChunkByChunkConfigScreen(Screen lastScreen) {
        super(Component.translatable("config.chunkbychunk.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        settingsList = new SettingListWidget(minecraft, this, width, 22, height - 44, (int) (0.9f * width));

        int w = (width / 3 - 60) / 2;
        resetButton = Button.builder(Component.translatable("controls.reset"), button -> {
            settingsList.reset();
        }).pos(w, height - 32).size(60, 20).build();
        cancelButton = Button.builder(Component.translatable("gui.cancel"), button -> {
            ConfigUtil.loadDefaultConfig();
            this.minecraft.setScreen(lastScreen);
        }).pos(width / 3 + w, height - 32).size(60, 20).build();
        saveButton = Button.builder(Component.translatable("selectWorld.edit.save"), button -> {
            ConfigUtil.saveDefaultConfig();
            this.minecraft.setScreen(lastScreen);
        }).pos(2 * width / 3 + w, height - 32).size(60, 20).build();

        this.addWidget(settingsList);
        this.addWidget(cancelButton);
        this.addWidget(saveButton);
        this.addWidget(resetButton);
    }

    @Override
    public void onClose() {
        ConfigUtil.loadDefaultConfig();
        super.onClose();
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void tick() {
        settingsList.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        this.settingsList.render(graphics, mouseX, mouseY, delta);

        int titleWidth = font.width(title);
        int titleX = (width - titleWidth) / 2;
        graphics.drawString(font, title, titleX, 8, 0xFFFFFF, true);
        this.cancelButton.render(graphics, mouseX, mouseY, delta);
        this.saveButton.render(graphics, mouseX, mouseY, delta);
        this.resetButton.render(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
    }
}
