package xyz.immortius.chunkbychunk.config.system;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Field Metadata for an enumeration config field
 */
public class EnumFieldMetadata extends FieldMetadata<Enum> {
    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);
    private static final Joiner VALUE_JOINER = Joiner.on(", ");

    private final Map<String, Enum<?>> lowercaseValueMap = new HashMap<>();
    private final Class<? extends Enum<?>> type;

    @SuppressWarnings("unchecked")
    public EnumFieldMetadata(Field field, String name, String comment) {
        super(field, name, comment);
        Preconditions.checkArgument(Enum.class.isAssignableFrom(field.getType()));
        type = (Class<? extends Enum<?>>) field.getType();
        Object[] enumValues = field.getType().getEnumConstants();
        for (Object value : enumValues) {
            Enum<?> enumValue = (Enum<?>) value;
            lowercaseValueMap.put(enumValue.name().toLowerCase(Locale.ROOT), enumValue);
        }
    }

    public Class<? extends Enum<?>> enumType() {
        return type;
    }

    @Override
    public List<String> getComments() {
        List<String> values = lowercaseValueMap.values().stream().map(Enum::name).toList();
        return ImmutableList.<String>builder().addAll(super.getComments()).add("Allowed Values: " + VALUE_JOINER.join(values)).build();
    }

    @Override
    public String serializeValue(Object object) {
        Enum<?> value = (Enum<?>) getValue(object);
        return "\"" + value.name() + "\"";
    }

    @Override
    public void deserializeValue(Object object, String value) {
        String processedValue = value.toLowerCase(Locale.ROOT);
        if (processedValue.startsWith("\"") && processedValue.endsWith("\"")) {
            processedValue = processedValue.substring(1, value.length() - 1);
        }
        Enum<?> enumValue = lowercaseValueMap.get(processedValue);
        if (enumValue == null) {
            LOGGER.warn("Invalid value {} for config field {}", value, getName());
        } else {
            setValue(object, enumValue);
        }
    }
}
