package coffeeshout.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;

public class AsyncApiGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    @EventListener(ApplicationReadyEvent.class)
    public void generateAsyncapiYml() throws IOException {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode channel = mapper.createObjectNode();
        generateAppChannel(channel);
        generateTopicChannel(channel);

        ObjectNode schema = mapper.createObjectNode();
        generateSchema(schema);

        ObjectNode message = mapper.createObjectNode();
        generateMessage(message);

        ObjectNode operation = mapper.createObjectNode();
        generateSendOperation(operation);
        generateTopicOperation(operation);

        root.put("asyncapi", "3.0.0");
        root.put("info", generateMeta());

        root.put("channels", channel);
        root.put("operations", operation);

        ObjectNode components = mapper.createObjectNode();
        components.put("messages", message);
        components.put("schemas", schema);

        root.put("components", components);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String yaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        Path path = Paths.get("asyncapi.yml"); // 현재 실행 경로에 저장
        Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));

       /*
       asyncapi generate fromTemplate asyncapi.yml @asyncapi/html-template@3.0.0 --use-new-generator -o ./output/dev --force-write -p singleFile=true
       이 요청 보내서 파일 생성하기 index.html파일 생성하기


        */
    }

    public JsonNode generateTopicOperation(ObjectNode operationNode) {
        /*
            1. MessageMapping을 찾는다.
            2. MessageMapping에 따라서 json정의
            3. Operation이 있으면 summery, description 정의
            4. MessageResponse있으면 reply정의
         */
        final Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);
        final Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageResponse.class);
        for (var method : methods) {
            if (!isTopic(method)) {
                continue;
            }
            ObjectNode body = mapper.createObjectNode();
            Operation operation = method.getAnnotation(Operation.class);
            MessageResponse messageResponse = method.getAnnotation(MessageResponse.class);
            body.put("action", "receive");
            body.put("channel", operationChannelRef(messageResponse.path(), "/topic"));
            if (operation != null) {
                body.put("summary", operation.summery());
                body.put("description", operation.description());
            }
            ArrayNode messagesArray = mapper.createArrayNode();
            messagesArray.add(messageParameterNode(messageResponse.returnType().getSimpleName()));
            body.put("messages", messagesArray);
            operationNode.put(messageResponse.path(), body);
        }
        return operationNode;
    }

    private boolean isTopic(Method method) {
        return method.getAnnotation(MessageMapping.class) == null;
    }


    public JsonNode generateSendOperation(ObjectNode operationNode) {
        /*
            1. MessageMapping을 찾는다.
            2. MessageMapping에 따라서 json정의
            3. Operation이 있으면 summery, description 정의
            4. MessageResponse있으면 reply정의
         */
        final Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);
        final Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageMapping.class);
        for (var method : methods) {
            ObjectNode body = mapper.createObjectNode();
            MessageMapping messageMapping = method.getAnnotation(MessageMapping.class);
            Operation operation = method.getAnnotation(Operation.class);
            MessageResponse messageResponse = method.getAnnotation(MessageResponse.class);
            body.put("action", "send");
            body.put("channel", operationChannelRef(messageMapping.value()[0], "/app"));
            if (operation != null) {
                body.put("summary", operation.summery());
                body.put("description", operation.description());
            }
            ArrayNode messagesArray = mapper.createArrayNode();
            for (var param : method.getParameters()) {
                if (!isDestinationVariable(param)) {
                    messagesArray.add(messageParameterNode(param.getType().getSimpleName()));
                }
            }
            body.put("messages", messagesArray);
            if (messageResponse != null) {
                ObjectNode reply = mapper.createObjectNode();
                reply.put("channel", operationChannelRef(messageResponse.path(), "/topic"));
                ArrayNode responseNodes = mapper.createArrayNode();
                responseNodes.add(messageParameterNode(messageResponse.returnType().getSimpleName()));
                reply.put("messages", responseNodes);
                body.put("reply", reply);
                operationNode.put(messageMapping.value()[0], body);
            }
        }
        return operationNode;
    }


    public Set<String> getParam() {
        List<String> destinationParams = getDestinationParams();
        List<String> messageResponseParams = getMessageResponseParams();
        Set<String> ret = new HashSet<>(destinationParams.size() + messageResponseParams.size());
        ret.addAll(destinationParams);
        ret.addAll(messageResponseParams);
        return ret;
    }

    public List<String> getDestinationParams() {
        final List<String> ret = new ArrayList<>();
        final Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);
        final Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageMapping.class);
        for (var method : methods) {
            for (var param : method.getParameters()) {
                if (isDestinationVariable(param)) {
                    ret.add(param.getName());
                }
            }
        }
        return ret;
    }

    public List<String> getMessageResponseParams() {
        final List<String> ret = new ArrayList<>();
        final Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);
        final Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageResponse.class);
        for (var method : methods) {
            MessageResponse annotation = method.getAnnotation(MessageResponse.class);
            for (var payload : getParams(annotation.path())) {
                ret.add(payload);
            }
        }
        return ret;
    }

    public JsonNode generateMeta() {
        final ObjectNode metadata = mapper.createObjectNode();
        metadata.put("title", "coffee-shout wesocket docs");
        metadata.put("version", LocalDateTime.now().toString());
        metadata.put("description", "커피빵에서 사용되는 웹소켓 명세서");
        return metadata;
    }

    public JsonNode generateMessage(ObjectNode messageNode) {
        final Reflections reflections = new Reflections("coffeeshout");
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(WebsocketMessage.class);
        for (var clazz : typesAnnotatedWith) {
            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.put("payload", schemaNode(clazz.getSimpleName()));
            messageNode.put(clazz.getSimpleName(), payloadNode);
        }
        return messageNode;
    }


    public JsonNode generateSchema(ObjectNode schemaNode) {
        final Reflections reflections = new Reflections("coffeeshout");

        // ⚡ victools 설정
        SchemaGeneratorConfigBuilder configBuilder =
                new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                        .without(Option.DEFINITIONS_FOR_ALL_OBJECTS)   // definitions/ref 없애고 inline
                        .without(Option.EXTRA_OPEN_API_FORMAT_VALUES); // 필요없으면 뺄 수 있음

        // Enum 처리 커스터마이징
        configBuilder.forFields().withEnumResolver(field -> {
                    JsonSchemaEnumType annotation = field.getAnnotation(JsonSchemaEnumType.class);
                    if (annotation != null) {
                        Class<? extends Enum<?>> enumClass = annotation.enumType();
                        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList();
                    }
                    return null;
                });

        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);

        // 커스텀 어노테이션 붙은 클래스만 스캔
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(WebsocketMessage.class);
        for (var clazz : typesAnnotatedWith) {
            JsonNode schema = generator.generateSchema(clazz);
            ((ObjectNode) schemaNode).set(clazz.getSimpleName(), schema);
        }

        return schemaNode;
    }

    private JsonNode replaceRefWithDef(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            if (obj.has("$ref")) {
                JsonNode refValue = obj.get("$ref");
                obj.remove("$ref");
                obj.set("$def", refValue);
            }
            obj.fields().forEachRemaining(entry -> replaceRefWithDef(entry.getValue()));
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (JsonNode child : arr) {
                replaceRefWithDef(child);
            }
        }
        return node;
    }

    public JsonNode generateAppChannel(ObjectNode channel) {
        final Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);
        final Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageMapping.class);
        for (Method method : methods) {
            final MessageMapping annotation = method.getAnnotation(MessageMapping.class);
            final String path = "/app" + annotation.value()[0];
            final ObjectNode body = mapper.createObjectNode();
            final ObjectNode messageNode = mapper.createObjectNode();
            final ObjectNode paramNode = mapper.createObjectNode();
            for (var param : method.getParameters()) {
                String paramName = param.getName();
                String paramTypeName = param.getType().getSimpleName();
                if (isDestinationVariable(param)) {
                    paramNode.put(paramName, refParameterNode(paramName));
                } else {
                    messageNode.put(paramTypeName, messageParameterNode(paramTypeName));
                }
            }
//            if (!paramNode.isEmpty()) {
//                body.put("parameters", paramNode);
//            }
            if (!messageNode.isEmpty()) {
                body.put("messages", messageNode);
            }
            channel.put(path, body);
        }
        return channel;
    }

    public JsonNode generateTopicChannel(ObjectNode channel) {
        final Reflections reflections = new Reflections("coffeeshout", Scanners.MethodsAnnotated);
        final Set<Method> methods = reflections.getMethodsAnnotatedWith(MessageResponse.class);
        for (Method method : methods) {
            final MessageResponse annotation = method.getAnnotation(MessageResponse.class);
            final String path = "/topic" + annotation.path();
            final ObjectNode body = mapper.createObjectNode();
            final ObjectNode messageNode = mapper.createObjectNode();
            final ObjectNode paramNode = mapper.createObjectNode();
            for (var paramName : getParams(path)) {
                paramNode.put(paramName, refParameterNode(paramName));
            }
            String returnTypeName = annotation.returnType().getSimpleName();
            messageNode.put(returnTypeName, messageParameterNode(returnTypeName));
//            if (!paramNode.isEmpty()) {
//                body.put("parameters", paramNode);
//            }
            if (!messageNode.isEmpty()) {
                body.put("messages", messageNode);
            }
            channel.put(path, body);
        }
        return channel;
    }

    private ObjectNode refParameterNode(String paramName) {
        ObjectNode refNode = mapper.createObjectNode();
        refNode.put("$ref", "#/components/parameters/" + paramName);
        return refNode;
    }

    private ObjectNode messageParameterNode(String paramName) {
        ObjectNode refNode = mapper.createObjectNode();
        refNode.put("$ref", "#/components/messages/" + paramName);
        return refNode;
    }

    private ObjectNode schemaNode(String paramName) {
        ObjectNode refNode = mapper.createObjectNode();
        refNode.put("$ref", "#/components/schemas/" + paramName);
        return refNode;
    }

    private ObjectNode operationChannelRef(String path, String prefix) {
        ObjectNode refNode = mapper.createObjectNode();
        String concat = prefix + path;
        String parse = concat.replaceAll("/", "~1");
        refNode.put("$ref", "#/channels/" + parse);
        return refNode;
    }

    private boolean isDestinationVariable(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations())
                .anyMatch(ann -> ann.annotationType() == DestinationVariable.class);
    }

    private List<String> getParams(String path) {
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(path);

        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group(1));
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        new AsyncApiGenerator().generateAsyncapiYml();
    }
}
