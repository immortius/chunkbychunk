package xyz.immortius.chunkbychunk.config.system;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans a type to build config metadata
 */
public final class MetadataBuilder {

    private MetadataBuilder() {

    }

    /**
     * @param type The type to scan
     * @return ConfigMetadata describing the type
     */
    public static ConfigMetadata build(Class<?> type) {
        List<FieldMetadata<?>> fields = processFields(type);
        List<SectionMetadata> sections = new ArrayList<>();
        for (Field declaredField : type.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            Class<?> fieldType = declaredField.getType();
            if (Object.class.isAssignableFrom(fieldType) && !Enum.class.isAssignableFrom(fieldType)) {
                sections.add(processSection(fieldType, declaredField));
            }
        }

        return new ConfigMetadata(sections, fields);
    }

    private static SectionMetadata processSection(Class<?> type, Field field) {
        String name = getName(field);
        List<FieldMetadata<?>> fields = processFields(type);
        return new SectionMetadata(name, fields, field);
    }

    private static List<FieldMetadata<?>> processFields(Class<?> type) {
        List<FieldMetadata<?>> fields = new ArrayList<>();
        for (Field declaredField : type.getDeclaredFields()) {
            Class<?> fieldType = declaredField.getType();
            if (Integer.TYPE.equals(fieldType)) {
                fields.add(processIntField(declaredField));
            } else if (Boolean.TYPE.equals(fieldType)) {
                fields.add(processBooleanField(declaredField));
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                fields.add(processEnumField(declaredField));
            }
        }
        return fields;
    }

    private static FieldMetadata processEnumField(Field field) {
        String name = getName(field);
        String comment = getComment(field);
        return new EnumFieldMetadata(field, name, comment);
    }

    private static FieldMetadata processBooleanField(Field field) {
        String name = getName(field);
        String comment = getComment(field);
        return new BooleanFieldMetadata(field, name, comment);
    }

    private static FieldMetadata processIntField(Field field) {
        String name = getName(field);
        String comment = getComment(field);
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        IntRange intRange = field.getAnnotation(IntRange.class);
        if (intRange != null) {
            min = intRange.min();
            max = intRange.max();
        }
        return new IntFieldMetadata(field, name, comment, min, max);
    }

    private static String getName(Field field) {
        Name annotation = field.getAnnotation(Name.class);
        if (annotation != null) {
            return annotation.value();
        }
        return field.getName();
    }

    private static String getComment(Field field) {
        Comment annotation = field.getAnnotation(Comment.class);
        if (annotation != null) {
            return annotation.value();
        }
        return "";
    }

}
