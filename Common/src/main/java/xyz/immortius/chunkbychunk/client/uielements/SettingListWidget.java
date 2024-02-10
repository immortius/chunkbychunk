package xyz.immortius.chunkbychunk.client.uielements;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;
import xyz.immortius.chunkbychunk.config.ChunkByChunkConfig;
import xyz.immortius.chunkbychunk.config.system.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget providing a scrollable list of configuration options for ChunkByChunk
 */
public class SettingListWidget extends ContainerObjectSelectionList<SettingListWidget.SettingEntry> {

    private EditBox lastFocused = null;
    private int rowWidth;

    public SettingListWidget(Minecraft minecraft, Screen parent, int width, int top, int bottom, int rowWidth) {
        super(minecraft, width, parent.height, top, bottom, 22);
        this.rowWidth = rowWidth;
        ConfigMetadata metadata = MetadataBuilder.build(ChunkByChunkConfig.class);
        ChunkByChunkConfig defaultConfig = new ChunkByChunkConfig();

        for (SectionMetadata section : metadata.getSections().values()) {
            Object defaultSection = section.getSectionObject(defaultConfig);
            Object configSection = section.getSectionObject(ChunkByChunkConfig.get());
            this.addEntry(new SectionTitleEntry(section.getDisplayName()));
            for (FieldMetadata<?> field : section.getFields().values()) {
                if (field instanceof BooleanFieldMetadata boolField) {
                    Boolean defaultValue = boolField.getValue(defaultSection);
                    this.addEntry(new BooleanEntry(field.getDisplayName(),
                            () -> boolField.getValue(configSection),
                            (x) -> boolField.setValue(configSection, x), defaultValue));
                } else if (field instanceof EnumFieldMetadata enumField) {
                    Enum<?> defaultValue = enumField.getValue(defaultSection);
                    this.addEntry(new EnumEntry(enumField.getDisplayName(), enumField.enumType(),
                            () -> enumField.getValue(configSection),
                            (x) -> enumField.setValue(configSection, x), defaultValue));
                } else if (field instanceof IntFieldMetadata intField) {
                    Integer defaultValue = intField.getValue(defaultSection);
                    if (intField.getMaxValue() - intField.getMinValue() > 256) {
                        this.addEntry(new ExtendedIntegerEntry(field.getDisplayName(), intField.getMinValue(), intField.getMaxValue(),
                                () -> intField.getValue(configSection),
                                (x) -> intField.setValue(configSection, x), defaultValue));
                    } else {
                        this.addEntry(new IntegerEntry(field.getDisplayName(), intField.getMinValue(), intField.getMaxValue(),
                                () -> intField.getValue(configSection),
                                (x) -> intField.setValue(configSection, x), defaultValue));
                    }
                } else if (field instanceof StringFieldMetadata stringField) {
                   this.addEntry(new StringEntry(field.getDisplayName(), () -> stringField.getValue(configSection), (x) -> stringField.setValue(configSection, x), stringField.getValue(defaultSection)));
                } else {
                    ChunkByChunkConstants.LOGGER.info("Skipping config option {} as type not supported", field.getName());
                }
            }
        }
    }

    public int getRowWidth() {
        return rowWidth;
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + getRowWidth() / 2 + 4;
    }

    public void tick() {
        this.children().forEach(SettingEntry::tick);
    }

    public void reset() {
        children().forEach(SettingEntry::reset);
    }

    public class SectionTitleEntry extends SettingEntry {

        private final Component displayName;

        public SectionTitleEntry(Component displayName) {
            this.displayName = displayName;
        }

        @Override
        public void render(PoseStack stack, int listIndex, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            SettingListWidget.this.minecraft.font.drawShadow(stack, this.displayName, left + 12, top + 8, 0xFFFFFF);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }

    public class IntegerEntry extends AbstractWidgetEntry<IntegerSlider> {

        private final Integer defaultValue;
        private final Consumer<Integer> setter;

        public IntegerEntry(Component displayName, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter, Integer defaultValue) {
            super(new IntegerSlider(0, 0, getRowWidth(), 20, displayName, min, max, getter, setter));
            this.setter = setter;
            this.defaultValue = defaultValue;
        }

        @Override
        public void reset() {
            setter.accept(defaultValue);
            widget.setValue(defaultValue);
        }
    }

    public class StringEntry extends AbstractWidgetEntry<EditBox> {

        private final Component displayName;
        private final String defaultValue;
        private final Consumer<String> setter;

        public StringEntry(Component displayName, Supplier<String> getter, Consumer<String> setter, String defaultValue) {
            super(new EditBox(SettingListWidget.this.minecraft.font, 0, 0, getRowWidth(), 20, displayName));
            this.displayName = displayName;
            this.defaultValue = defaultValue;
            this.setter = setter;
            widget.setValue(getter.get());
            widget.setEditable(true);
            widget.setResponder(setter);
        }

        @Override
        public void reset() {
            setter.accept(defaultValue);
            widget.setValue(defaultValue);
        }

        @Override
        public boolean mouseClicked(double x, double y, int mouseButton) {
            if (super.mouseClicked(x, y, mouseButton)) {
                lastFocused = widget;
                return true;
            }
            return false;
        }

        @Override
        public void render(PoseStack stack, int listIndex, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            int labelLength = SettingListWidget.this.minecraft.font.width(this.displayName);
            SettingListWidget.this.minecraft.font.draw(stack, this.displayName, left, top + 6, 0xFFFFFF);
            widget.setX(left + labelLength + 6);
            widget.setWidth(getRowWidth() - labelLength - 6);
            widget.y = top;
            widget.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public boolean charTyped(char p_94683_, int p_94684_) {
            return widget.charTyped(p_94683_, p_94684_);
        }

        @Override
        public void tick() {
            widget.tick();
        }
    }

    public class BooleanEntry extends AbstractWidgetEntry<CycleButton<Boolean>> {

        private final Boolean defaultValue;
        private final Consumer<Boolean> setter;

        public BooleanEntry(Component displayName, Supplier<Boolean> getter, Consumer<Boolean> setter, Boolean defaultValue) {
            super(new CycleButton.Builder<Boolean>((x) -> new TranslatableComponent((x) ? "gui.yes" : "gui.no"))
                    .withValues(Boolean.FALSE, Boolean.TRUE)
                    .withInitialValue(getter.get())
                    .create(0, 0, getRowWidth(), 20, displayName, (cycleButton, value) -> setter.accept(value)));
            this.defaultValue = defaultValue;
            this.setter = setter;
        }

        @Override
        public void reset() {
            setter.accept(defaultValue);
            widget.setValue(defaultValue);
        }
    }

    public class ExtendedIntegerEntry extends AbstractWidgetEntry<EditBox> {
        private final Component displayName;
        private final Integer defaultValue;
        private final Consumer<Integer> setter;

        public ExtendedIntegerEntry(Component displayName, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter, Integer defaultValue) {
            super(new EditBox(SettingListWidget.this.minecraft.font, 0, 0, getRowWidth(), 20, displayName));
            this.displayName = displayName;
            this.defaultValue = defaultValue;
            this.setter = setter;
            widget.setFilter(x -> {
                if (x.isEmpty() || "-".equals(x)) {
                    return true;
                }
                try {
                    int val = Integer.parseInt(x);
                    return val >= min && val <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            widget.setValue(getter.get().toString());
            widget.setEditable(true);
            widget.setResponder(value -> {
                if (value.isEmpty() || "-".equals(value)) {
                    if (0 >= min && 0 <= max) {
                        setter.accept(0);
                    }
                } else {
                    setter.accept(Integer.parseInt(value));
                }
            });
        }

        @Override
        public void reset() {
            setter.accept(defaultValue);
            widget.setValue(defaultValue.toString());
        }

        @Override
        public boolean mouseClicked(double x, double y, int mouseButton) {
            if (super.mouseClicked(x, y, mouseButton)) {
                lastFocused = widget;
                return true;
            }
            return false;
        }

        @Override
        public void render(PoseStack stack, int listIndex, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            int labelLength = SettingListWidget.this.minecraft.font.width(this.displayName);
            SettingListWidget.this.minecraft.font.draw(stack, this.displayName, left, top + 6, 0xFFFFFF);
            widget.setX(left + labelLength + 6);
            widget.setWidth(getRowWidth() - labelLength - 6);
            widget.y = top;
            widget.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public boolean charTyped(char p_94683_, int p_94684_) {
            return widget.charTyped(p_94683_, p_94684_);
        }

        @Override
        public void tick() {
            widget.tick();
        }
    }

    public class EnumEntry extends AbstractWidgetEntry<CycleButton<Enum<?>>> {

        private final Enum<?> defaultValue;
        private final Consumer<Enum<?>> setter;

        public EnumEntry(Component displayName, Class<? extends Enum<?>> type, Supplier<Enum<?>> getter, Consumer<Enum<?>> setter, Enum<?> defaultValue) {
            super(new CycleButton.Builder<Enum<?>>((x) -> new TranslatableComponent("enumvalue.chunkbychunk." + type.getSimpleName() + "." + x.name()))
                    .withValues(type.getEnumConstants())
                    .withInitialValue(getter.get())
                    .create(0, 0, getRowWidth(), 20, displayName, (cycleButton, value) -> setter.accept(value)));
            this.defaultValue = defaultValue;
            this.setter = setter;
        }

        @Override
        public void reset() {
            setter.accept(defaultValue);
            widget.setValue(defaultValue);
        }
    }

    public abstract class AbstractWidgetEntry<T extends AbstractWidget> extends SettingEntry {

        protected final T widget;
        private boolean dragging;

        public AbstractWidgetEntry(T widget) {
            this.widget = widget;
        }

        @Override
        public void render(PoseStack stack, int listIndex, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
            widget.x = left;
            widget.y = top;
            widget.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double x, double y, int mouseButton) {
            if (SettingListWidget.this.lastFocused != null && SettingListWidget.this.lastFocused != widget) {
                SettingListWidget.this.lastFocused.setFocus(false);
            }
            dragging = true;
            return this.widget.mouseClicked(x, y, mouseButton);
        }

        @Override
        public boolean mouseReleased(double x, double y, int mouseButton) {
            dragging = false;
            return this.widget.mouseReleased(x, y, mouseButton);
        }

        @Override
        public boolean mouseDragged(double x, double y, int mouseButton, double deltaX, double deltaY) {
            if (dragging) {
                return this.widget.mouseDragged(x, y, mouseButton, deltaX, deltaY);
            }
            return false;
        }

        @Override
        public boolean isMouseOver(double p_93537_, double p_93538_) {
            return this.widget.isMouseOver(p_93537_, p_93538_);
        }

        @Override
        public boolean keyPressed(int p_94710_, int p_94711_, int p_94712_) {
            return this.widget.keyPressed(p_94710_, p_94711_, p_94712_);
        }

        @Override
        public boolean keyReleased(int p_94715_, int p_94716_, int p_94717_) {
            return this.widget.keyReleased(p_94715_, p_94716_, p_94717_);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(widget);
        }
    }

    public static abstract class SettingEntry extends Entry<SettingEntry> {

        public void tick() {
        }

        public void reset() {
        }
    }

}
