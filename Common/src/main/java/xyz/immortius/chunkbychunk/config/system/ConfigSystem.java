package xyz.immortius.chunkbychunk.config.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.immortius.chunkbychunk.common.ChunkByChunkConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A system for reading or writing configuration files.
 *
 * Currently supports only:
 * <ul>
 *     <li>int fields</li>
 *     <li>boolean fields</li>
 *     <li>a single layer of pojo objets off the target object - it is expected these are not null</li>
 * </ul>
 * The pojo objects become sub-sections of the config file.
 * Fields can be annotated to set their serialization name, comment, and range for integers.
 */
public class ConfigSystem {

    private static final Logger LOGGER = LogManager.getLogger(ChunkByChunkConstants.MOD_ID);
    private static final String NEWLINE = "\n";
    private static final String START_COMMENT = "#";
    private static final String EQUALS = " = ";
    private static final String START_SECTION = "[";
    private static final String END_SECTION = "]";
    private static final String INDENT = "\t";

    private final Map<Class<?>, ConfigMetadata> metadataMap = new HashMap<>();

    /**
     * Loads the configFile if it exists, creates it from object if it does not.
     * @param configFile The config file to load or create
     * @param defaultFile Fallback config file to load from if the configFile doesn't exist.
     * @param object The config object to load into or create the file from
     */
    public void synchConfig(Path configFile, Path defaultFile, Object object) {
        if (!createPathTo(configFile)) {
            return;
        }

        if (Files.exists(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                readInto(reader, object);
            } catch (IOException e) {
                LOGGER.error("Failed to read server config at '{}'", configFile, e);
            }
        } else if (Files.exists(defaultFile)) {
            try (BufferedReader reader = Files.newBufferedReader(defaultFile)) {
                readInto(reader, object);
            } catch (IOException e) {
                LOGGER.error("Failed to read default config at '{}'", configFile, e);
            }
        }
        if (!Files.exists(configFile)) {
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                write(writer, object);
            } catch (IOException e) {
                LOGGER.error("Failed to write server config at {}", configFile, e);
            }
        }
    }

    /**
     * Loads the configFile if it exists, creates it from object if it does not.
     * @param configFile The config file to load or create
     * @param object The config object to load into or create the file from
     */
    public void synchConfig(Path configFile, Object object) {
        if (!createPathTo(configFile)) {
            return;
        }

        if (Files.exists(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
                readInto(reader, object);
            } catch (IOException e) {
                LOGGER.error("Failed to read server config at '{}'", configFile, e);
            }
        } else {
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                write(writer, object);
            } catch (IOException e) {
                LOGGER.error("Failed to write server config at {}", configFile, e);
            }
        }
    }

    /**
     * Writes config to the given path
     * @param configFile The path to write config to
     * @param object The configuration to write
     */
    public void write(Path configFile, Object object) {
        if (!createPathTo(configFile)) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            write(writer, object);
        } catch (IOException e) {
            LOGGER.error("Failed to write server config at {}", configFile, e);
        }
    }

    private boolean createPathTo(Path configFile) {
        if (configFile.getParent() != null && !Files.exists(configFile.getParent())) {
            try {
                Files.createDirectories(configFile.getParent());
                return true;
            } catch (IOException e) {
                LOGGER.error("Failed to create server config path '{}'", configFile.getParent(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * Reads config into an existing object
     * @param reader A reader to read configuration data from
     * @param into The object to populate from the data
     */
    public void readInto(BufferedReader reader, Object into) {
        ConfigMetadata metadata = getMetadata(into);
        try {
            Object currentObject = into;
            ObjectMetadata currentMetadata = metadata;
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.startsWith(START_SECTION)) {
                    int endIndex = line.indexOf(END_SECTION);
                    if (endIndex != -1) {
                        String sectionName = line.substring(START_SECTION.length(), endIndex);
                        SectionMetadata sectionMetadata = metadata.getSections().get(sectionName.toLowerCase(Locale.ROOT));
                        if (sectionMetadata != null) {
                            currentMetadata = sectionMetadata;
                            currentObject = sectionMetadata.getSectionObject(into);
                        } else {
                            LOGGER.warn("Encountered unexpected section {}", sectionName);
                        }
                    } else {
                        LOGGER.warn("Invalid section statement {}", line);
                    }
                } else if (!line.startsWith(START_COMMENT) && !line.isEmpty()) {
                    String[] parts = line.split(EQUALS);
                    if (parts.length == 2) {
                        String fieldName = parts[0].trim();
                        String value = parts[1].trim();
                        FieldMetadata fieldMetadata = currentMetadata.getFields().get(fieldName.toLowerCase(Locale.ROOT));
                        if (fieldMetadata != null) {
                            fieldMetadata.deserializeValue(currentObject, value);
                        } else {
                            LOGGER.warn("Unexpected field {}", fieldName);
                        }
                    } else {
                        LOGGER.warn("Bad config line {}", line);
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read config", e);
        }
    }

    /**
     * Writes a configuration object
     * @param writer The writer to write into
     * @param object The config object to write
     */
    public void write(Writer writer, Object object) {
        ConfigMetadata metadata = getMetadata(object);
        try {
            for (FieldMetadata field : metadata.getFields().values()) {
                writeField(writer, object, field, "");
            }
            for (SectionMetadata section : metadata.getSections().values()) {
                writer.write(NEWLINE);
                writer.write(START_SECTION);
                writer.write(section.getName());
                writer.write(END_SECTION);
                writer.write(NEWLINE);
                Object sectionObject = section.getSectionObject(object);
                for (FieldMetadata field : section.getFields().values()) {
                    writeField(writer, sectionObject, field, INDENT);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write config", e);
        }

    }

    private void writeField(Writer writer, Object object, FieldMetadata<?> field, String indentation) throws IOException {
        for (String commentLine : field.getComments()) {
            for (String line : commentLine.split("[\n\r]+")) {
                writer.write(indentation);
                writer.write(START_COMMENT);
                writer.write(line);
                writer.write(NEWLINE);
            }
        }
        writer.write(indentation);
        writer.write(field.getName());
        writer.write(EQUALS);
        writer.write(field.serializeValue(object));
        writer.write(NEWLINE);
    }

    private ConfigMetadata getMetadata(Object o) {
        ConfigMetadata metadata = metadataMap.get(o.getClass());
        if (metadata == null) {
            metadata = MetadataBuilder.build(o.getClass());
            metadataMap.put(o.getClass(), metadata);
        }
        return metadata;
    }

}
