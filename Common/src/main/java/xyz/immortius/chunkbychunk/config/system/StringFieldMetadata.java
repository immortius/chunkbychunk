package xyz.immortius.chunkbychunk.config.system;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;

import java.lang.reflect.Field;

/**
 * A boolean field
 */
public class StringFieldMetadata extends FieldMetadata<String> {
    public static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);

    private final Field field;

    public StringFieldMetadata(Field field, String name, String comment) {
        super(field, name, comment);
        Preconditions.checkArgument(String.class.equals(field.getType()));
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    public String serializeValue(Object object) {
        try {
            return field.get(object).toString();
        } catch (IllegalAccessException e) {
            throw new ConfigException("Failed to retrieve " + getName() + " from object " + object, e);
        }
    }

    @Override
    public void deserializeValue(Object object, String value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set " + getName() + " to value " + value, e);
        }
    }
}
