package xyz.immortius.chunkbychunk.config.system;

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Base metadata for an object - either the root config object, or a section
 */
abstract class ObjectMetadata {
    private final Map<String, FieldMetadata> fields;

    public ObjectMetadata(Collection<FieldMetadata> fields) {
        ImmutableMap.Builder<String, FieldMetadata> fieldsBuilder = ImmutableMap.builder();
        for (FieldMetadata field : fields) {
            fieldsBuilder.put(field.getName().toLowerCase(Locale.ROOT), field);
        }
        this.fields = fieldsBuilder.build();
    }

    public Map<String, FieldMetadata> getFields() {
        return fields;
    }
}
