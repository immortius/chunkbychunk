package xyz.immortius.chunkbychunk.config.system;

import com.google.common.base.Strings;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * Base for metadata on all config fields
 */
public abstract class FieldMetadata<T> {
    private final String name;
    private final String comment;
    private final Component displayName;
    private final Field field;

    public FieldMetadata(Field field, String name, String comment) {
        this.field = field;
        this.field.setAccessible(true);
        this.name = name;
        this.comment = comment;
        this.displayName = Component.translatable("config.chunkbychunk.option." + field.getName());
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public List<String> getComments() {
        if (Strings.isNullOrEmpty(comment)) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(comment);
        }
    }

    /**
     * Gets the value of the field from the given container instance
     * @param object The instance to read.
     * @return The value of the field
     */
    @SuppressWarnings("unchecked")
    public T getValue(Object object) {
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new ConfigException("Failed to retrieve " + getName() + " from object " + object, e);
        }
    }

    /**
     * Sets the value of the field in the given container instance
     * @param object The instance to write to
     * @param value The value to set the field to
     */
    public void setValue(Object object, T value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new ConfigException("Failed to set " + getName() + " to value " + value, e);
        }
    }

    public abstract String serializeValue(Object object);

    public abstract void deserializeValue(Object object, String value);

}
