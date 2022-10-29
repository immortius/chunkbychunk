package xyz.immortius.chunkbychunk.config.system;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Locale;

/**
 * Config metadata for a section
 */
public class SectionMetadata extends ObjectMetadata {

    private final Field sectionField;
    private final String name;
    private final Component displayName;

    public SectionMetadata(String name, Collection<FieldMetadata<?>> fields, Field sectionField) {
        super(fields);
        this.name = name;
        this.sectionField = sectionField;
        this.sectionField.setAccessible(true);
        this.displayName = new TranslatableComponent("config.chunkbychunk.section." + name.toLowerCase(Locale.ROOT));
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public Object getSectionObject(Object source) {
        try {
            return sectionField.get(source);
        } catch (IllegalAccessException e) {
            throw new ConfigException("Failed to access section object " + name, e);
        }
    }

}
