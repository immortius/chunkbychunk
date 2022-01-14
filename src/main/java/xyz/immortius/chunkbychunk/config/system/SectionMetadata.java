package xyz.immortius.chunkbychunk.config.system;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Config metadata for a section
 */
class SectionMetadata extends ObjectMetadata {

    private final Field sectionField;
    private final String name;

    public SectionMetadata(String name, Collection<FieldMetadata> fields, Field sectionField) {
        super(fields);
        this.name = name;
        this.sectionField = sectionField;
        this.sectionField.setAccessible(true);
    }

    public String getName() {
        return name;
    }

    public Object getSectionObject(Object source) {
        try {
            return sectionField.get(source);
        } catch (IllegalAccessException e) {
            throw new ConfigException("Failed to access section object " + name, e);
        }
    }

}
