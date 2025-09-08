package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.path.json.JsonPath;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JsonUtils - Comprehensive JSON handling utilities
 * 
 * Features:
 * - JSON file reading/writing
 * - JSON string parsing and manipulation
 * - JSON path operations
 * - JSON validation and comparison
 * - Dynamic JSON modification
 */
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // ====================
    // FILE OPERATIONS
    // ====================
    
    /**
     * Read JSON from file and return as Map
     */
    public static Map<String, Object> readJsonFileAsMap(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }
    
    /**
     * Read JSON from file and return as specific object type
     */
    public static <T> T readJsonFileAsObject(String filePath, Class<T> clazz) {
        try {
            return objectMapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file as " + clazz.getSimpleName() + ": " + filePath, e);
        }
    }
    
    /**
     * Read JSON from file and return as List
     */
    public static <T> List<T> readJsonFileAsList(String filePath, Class<T> clazz) {
        try {
            return objectMapper.readValue(new File(filePath), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file as List: " + filePath, e);
        }
    }
    
    /**
     * Write object to JSON file
     */
    public static void writeJsonToFile(Object object, String filePath) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON to file: " + filePath, e);
        }
    }
    
    // ====================
    // STRING OPERATIONS
    // ====================
    
    /**
     * Parse JSON string to Map
     */
    public static Map<String, Object> parseJsonStringAsMap(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }
    
    /**
     * Parse JSON string to specific object type
     */
    public static <T> T parseJsonStringAsObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON string as " + clazz.getSimpleName(), e);
        }
    }
    
    /**
     * Convert object to JSON string
     */
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON string", e);
        }
    }
    
    /**
     * Convert object to pretty JSON string
     */
    public static String toPrettyJsonString(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to pretty JSON string", e);
        }
    }
    
    // ====================
    // JSON PATH OPERATIONS
    // ====================
    
    /**
     * Get value from JSON using JSONPath
     */
    public static <T> T getValueFromJson(String jsonString, String jsonPath) {
        JsonPath path = new JsonPath(jsonString);
        return path.get(jsonPath);
    }
    
    /**
     * Get value from JSON file using JSONPath
     */
    public static <T> T getValueFromJsonFile(String filePath, String jsonPath) {
        String jsonContent = readJsonFileAsString(filePath);
        return getValueFromJson(jsonContent, jsonPath);
    }
    
    /**
     * Read JSON file as raw string
     */
    private static String readJsonFileAsString(String filePath) {
        try {
            JsonNode jsonNode = objectMapper.readTree(new File(filePath));
            return objectMapper.writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file as string: " + filePath, e);
        }
    }
    
    // ====================
    // JSON MANIPULATION
    // ====================
    
    /**
     * Update JSON value at specific path
     */
    public static String updateJsonValue(String jsonString, String fieldPath, Object newValue) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            String[] pathParts = fieldPath.split("\\.");
            
            JsonNode currentNode = rootNode;
            for (int i = 0; i < pathParts.length - 1; i++) {
                currentNode = currentNode.path(pathParts[i]);
            }
            
            if (currentNode instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) currentNode;
                String finalField = pathParts[pathParts.length - 1];
                
                if (newValue instanceof String) {
                    objectNode.put(finalField, (String) newValue);
                } else if (newValue instanceof Integer) {
                    objectNode.put(finalField, (Integer) newValue);
                } else if (newValue instanceof Boolean) {
                    objectNode.put(finalField, (Boolean) newValue);
                } else {
                    objectNode.set(finalField, objectMapper.valueToTree(newValue));
                }
            }
            
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update JSON value at path: " + fieldPath, e);
        }
    }
    
    /**
     * Merge two JSON objects
     */
    public static String mergeJsonObjects(String json1, String json2) {
        try {
            JsonNode node1 = objectMapper.readTree(json1);
            JsonNode node2 = objectMapper.readTree(json2);
            
            JsonNode merged = merge(node1, node2);
            return objectMapper.writeValueAsString(merged);
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge JSON objects", e);
        }
    }
    
    private static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        if (updateNode.isObject()) {
            ObjectNode mainObject = (ObjectNode) mainNode;
            updateNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (mainObject.has(key)) {
                    mainObject.set(key, merge(mainObject.get(key), value));
                } else {
                    mainObject.set(key, value);
                }
            });
        }
        return mainNode;
    }
    
    // ====================
    // VALIDATION
    // ====================
    
    /**
     * Check if string is valid JSON
     */
    public static boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Compare two JSON strings for equality (ignoring field order)
     */
    public static boolean areJsonEqual(String json1, String json2) {
        try {
            JsonNode node1 = objectMapper.readTree(json1);
            JsonNode node2 = objectMapper.readTree(json2);
            return node1.equals(node2);
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Pretty print JSON string
     */
    public static void prettyPrintJson(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            System.err.println("Invalid JSON string: " + e.getMessage());
        }
    }
}
