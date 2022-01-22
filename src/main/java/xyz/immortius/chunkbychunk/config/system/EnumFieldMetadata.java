package xyz.immortius.chunkbychunk.config.system;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.interop.ChunkByChunkConstants;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Field Metadata for an enumeration config field
 */
class EnumFieldMetadata extends FieldMetadata{
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);
    private static final Joiner VALUE_JOINER = Joiner.on(", ");

    private final Field field;
    private final Map<String, Enum<?>> lowercaseValueMap = new HashMap<>();

    public EnumFieldMetadata(Field field, String name, String comment) {
        super(name, comment);
        Preconditions.checkArgument(Enum.class.isAssignableFrom(field.getType()));
        this.field = field;
        this.field.setAccessible(true);
        Object[] enumValues = field.getType().getEnumConstants();
        for (Object value : enumValues) {
            Enum<?> enumValue = (Enum<?>) value;
            lowercaseValueMap.put(enumValue.name().toLowerCase(Locale.ROOT), enumValue);
        }
    }

    @Override
    public List<String> getComments() {
        List<String> values = lowercaseValueMap.values().stream().map(Enum::name).toList();
        return ImmutableList.<String>builder().addAll(super.getComments()).add("Allowed Values: " + VALUE_JOINER.join(values)).build();
    }

    @Override
    public String serializeValue(Object object) {
        try {
            Enum<?> value = (Enum<?>) field.get(object);
            return "\"" + value.name() + "\"";
        } catch (IllegalAccessException e) {
            throw new ConfigException("Failed to retrieve " + getName() + " from object " + object, e);
        }
    }

    @Override
    public void deserializeValue(Object object, String value) {
        String processedValue = value;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            processedValue = value.substring(1, value.length() -1);
        }
        Enum<?> enumValue = lowercaseValueMap.get(processedValue);
        if (enumValue == null) {
            LOGGER.warn("Invalid value {} for config field {}", processedValue, getName());
        } else {
            try {
                field.set(object, enumValue);
            } catch (IllegalAccessException e) {
                throw new ConfigException("Failed to set " + getName() + " to value " + processedValue, e);
            }
        }
    }
}
