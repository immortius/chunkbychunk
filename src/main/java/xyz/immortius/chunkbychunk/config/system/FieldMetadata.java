package xyz.immortius.chunkbychunk.config.system;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.List;

/**
 * Base for metadata on all config fields
 */
abstract class FieldMetadata {
    private final String name;
    private final String comment;

    public FieldMetadata(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public List<String> getComments() {
        if (Strings.isNullOrEmpty(comment)) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(comment);
        }
    }

    public abstract String serializeValue(Object object);

    public abstract void deserializeValue(Object object, String value);

}
