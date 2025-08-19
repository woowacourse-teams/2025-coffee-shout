package coffeeshout.docs;

import coffeeshout.generator.AsyncApiGenerator;
import coffeeshout.generator.JsonSchemaEnumType;
import coffeeshout.generator.TestMessage;
import coffeeshout.generator.WebsocketMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.messaging.handler.annotation.MessageMapping;

public class AsyncapiTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void parameter테스트() {
        AsyncApiGenerator generator = new AsyncApiGenerator();
        Set<String> param = generator.getParam();
        System.out.println(param);
    }

    @Test
    void test() throws JsonProcessingException {
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

        // 기본 스키마 생성
        JsonNode schema = schemaGen.generateJsonSchema(MiniGameStateMessage.class);

        // 후처리: 어노테이션 달린 필드 찾아서 enum 넣어주기
        for (Field field : MiniGameStateMessage.class.getDeclaredFields()) {
            JsonSchemaEnumType ann = field.getAnnotation(JsonSchemaEnumType.class);
            if (ann != null) {
                Class<? extends Enum<?>> enumClass = ann.value();

                // enum 값 배열 만들기
                ArrayNode enumValues = mapper.createArrayNode();
                for (Enum<?> e : enumClass.getEnumConstants()) {
                    enumValues.add(e.name());
                }

                ((ObjectNode) schema.get("properties").get(field.getName()))
                        .set("enum", enumValues);
            }
        }

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
    }

    @Test
    void WebsocketMessage_찾기() throws JsonProcessingException {
        Reflections reflections = new Reflections("coffeeshout"); // 패키지 기준

        ObjectMapper mapper = new ObjectMapper();

        // DTO 스캔
        for (Class<?> dtoClass : reflections.getTypesAnnotatedWith(WebsocketMessage.class)) {
            String jsonSchema = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapper.generateJsonSchema(dtoClass));

            System.out.println("### DTO: " + dtoClass.getSimpleName());
            System.out.println("스키마: " + jsonSchema);
        }
    }

    @Test
    void MessageMapping_찾기() throws JsonProcessingException {
        Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);

        Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageMapping.class);
        for (Method method : methods) {
            MessageMapping annotation = method.getAnnotation(MessageMapping.class);
            Parameter[] parameters = method.getParameters();
            System.out.println(parameters[0].getAnnotations()[0].annotationType());
            String path = annotation.value()[0];
            System.out.println("method = " + method.getName());
            System.out.println("path = " + path);
            System.out.println("method.getParameterTypes()[0] = " + method.getParameterTypes()[0]);
        }
    }

    @Test
    void generator테스트() throws JsonProcessingException {
        ObjectNode channel = mapper.createObjectNode();
        AsyncApiGenerator generator = new AsyncApiGenerator();
        generator.generateAppChannel(channel);
        generator.generateTopicChannel(channel);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(channel));
    }

    @Test
    void generator_schema테스트() throws JsonProcessingException {
        ObjectNode schema = mapper.createObjectNode();
        AsyncApiGenerator generator = new AsyncApiGenerator();
        generator.generateSchema(schema);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
    }

    @Test
    void generator_message테스트() throws JsonProcessingException {
        ObjectNode message = mapper.createObjectNode();
        AsyncApiGenerator generator = new AsyncApiGenerator();
        generator.generateMessage(message);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
    }

    @Test
    void operation테스트() throws JsonProcessingException {
        ObjectNode operation = mapper.createObjectNode();
        AsyncApiGenerator generator = new AsyncApiGenerator();
        generator.generateOperation(operation);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(operation));
    }

    @Test
    void 통합테스트() throws Exception {
        ObjectNode root = mapper.createObjectNode();
        AsyncApiGenerator generator = new AsyncApiGenerator();
        ObjectNode channel = mapper.createObjectNode();
        generator.generateAppChannel(channel);
        generator.generateTopicChannel(channel);

        ObjectNode schema = mapper.createObjectNode();
        generator.generateSchema(schema);

        ObjectNode message = mapper.createObjectNode();
        generator.generateMessage(message);

        ObjectNode operation = mapper.createObjectNode();
        generator.generateOperation(operation);

        root.put("asyncapi", "3.0.0");
        root.put("info", generator.generateMeta());

        root.put("channels", channel);
        root.put("operations", operation);

        ObjectNode components = mapper.createObjectNode();
        components.put("messages", message);
        components.put("schemas", schema);

        root.put("components", components);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String yaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        System.out.println(yaml);
    }
}
