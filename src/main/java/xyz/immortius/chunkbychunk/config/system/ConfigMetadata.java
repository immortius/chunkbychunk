package xyz.immortius.chunkbychunk.config.system;

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * Root metadata for a configuration file
 */
class ConfigMetadata extends ObjectMetadata {
    private final Map<String, SectionMetadata> sections;

    public ConfigMetadata(Collection<SectionMetadata> sections, Collection<FieldMetadata> fields) {
        super(fields);
        ImmutableMap.Builder<String, SectionMetadata> sectionsBuilder = ImmutableMap.builder();
        for (SectionMetadata section : sections) {
            sectionsBuilder.put(section.getName().toLowerCase(Locale.ROOT), section);
        }
        this.sections = sectionsBuilder.build();
    }

    public Map<String, SectionMetadata> getSections() {
        return sections;
    }
}
