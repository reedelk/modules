package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.DynamicMap;
import com.reedelk.runtime.api.script.DynamicValue;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

// TODO: I need work
@SuppressWarnings("unchecked")
public enum JavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(JavascriptEngine.class);

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;
    private final Invocable invocable;

    JavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
        invocable = (Invocable) engine;
    }

    /**
     *        if (body == null || body.isBlank()) {
     *             // If the script is empty, there is nothing to evaluate.
     *             // No content type header
     *             return empty();
     *         } else if (body.isMessagePayload()) {
     *             // Content == Payload
     *             return fromContent(message.getContent());
     *         } else {
     *             // The is a script: we evaluate it and set the result.
     *             // No content type header, it is set by the user
     *             Object result = scriptEngine.evaluate(body, message, flowContext);
     *             byte[] bodyAsBytes = result.toString().getBytes();
     *             return fromBytes(bodyAsBytes);
     *         }
     * @param value
     * @param message
     * @param flowContext
     * @param <T>
     * @return
     */
    // TODO: What if the result I want is a stream!?
    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, Message message, FlowContext flowContext) {
        if (value == null) {
            return Optional.empty();

        } else if (value.isScript()) {
            // If script is empty, no need to evaluate it.
            if (value.isEmptyScript()) {
                return Optional.empty();
            } else if (value.isMessagePayload()) {
                throw new ESBException("asdf");
                //return Optional.ofNullable(message.getContent().data())
            } else {
                String functionName = functionNameOf(value, INLINE_SCRIPT);
                try {
                    Object payload = message.getContent().data();
                    return Optional.ofNullable((T) invocable.invokeFunction(functionName, message, payload, flowContext));
                } catch (ScriptException | NoSuchMethodException e) {
                    throw new ESBException(e);
                }
            }
        } else {
            // TODO: Need a converter to convert from string to int, string to X and so on...
            throw new UnsupportedOperationException("not implemented yet");
            //return Optional.ofNullable(value.getBody());
        }
    }

    @Override
    public <T> Publisher<T> evaluateStream(Script script, Message message, FlowContext flowContext) {
        return Mono.empty();
    }

    /**
     *        if (responseBody == null|| responseBody.isBlank()) {
     *             return Mono.empty();
     *         } else if  (Evaluate.isErrorPayload(responseBody)) {
     *             // We avoid evaluating a script if we just want
     *             // to return the exception stacktrace (optimization).
     *             return StackTraceUtils.asByteStream(exception);
     *         } else {
     *             return evaluateBodyScript();
     *         }
     * @param value
     * @param throwable
     * @param flowContext
     * @param <T>
     * @return
     */
    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> value, Throwable throwable, FlowContext flowContext) {
        return Mono.empty();
    }

    /**
     *         if (responseBody == null || responseBody.isBlank()) {
     *             return Mono.empty();
     *         } else if (responseBody.isMessagePayload()) {
     *             // We avoid evaluating a script if we just want
     *             // to return the message payload (optimization).
     *             return message.getContent().asByteArrayStream();
     *         } else {
     *             Object result = scriptEngine.evaluate(responseBody, message, flowContext);
     *             return Mono.just(result.toString().getBytes());
     *         }
     * @param value
     * @param message
     * @param flowContext
     * @param <T>
     * @return
     */
    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> value, Message message, FlowContext flowContext) {
        return Mono.empty();
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, Throwable exception, FlowContext flowContext) {
        if (value == null) {
            return Optional.empty();
        } else if (value.isScript()) {
            String functionName = functionNameOf(value, INLINE_ERROR_SCRIPT);
            try {
                return Optional.ofNullable((T) invocable.invokeFunction(functionName, exception, flowContext));
            } catch (ScriptException | NoSuchMethodException e) {
                throw new ESBException(e);
            }
        } else {
            throw new UnsupportedOperationException("not implemented yet");
            //return Optional.ofNullable(value.getBody());
        }
    }

    @Override
    public <T> Optional<T> evaluate(Script script, Message message, FlowContext flowContext) {
        throw new IllegalArgumentException("Not implemented yet.");
    }

    private static final Map<String,?> EMPTY_MAP = Collections.unmodifiableMap(Collections.emptyMap());

    @Override
    public <T> Map<String, T> evaluate(Message message, FlowContext context, DynamicMap<T> dynamicMap) {
        if (dynamicMap.isEmpty()) {
            // If dynamic map is empty, nothing to do.
            return (Map<String, T>) EMPTY_MAP;
        } else {
            String functionName = functionNameOf(dynamicMap);
            try {
                return (Map<String, T>) invocable.invokeFunction(functionName, message, message.getContent().data(), context);
            } catch (ScriptException | NoSuchMethodException e) {
                throw new ESBException(e);
            }
        }
    }


    @Override
    public void onDisposed(Component component) {
        // TODO: Complete me
        //String key = key(component);
        //ORIGIN_FUNCTION_NAME.remove(key);
    }


    private static final String INLINE_SCRIPT =
            "var %s = function(message, payload, context) {\n" +
                    "  return %s\n" +
                    "};";

    private static final String INLINE_ERROR_SCRIPT =
            "var %s = function(error, context) {\n" +
                    "  return %s\n" +
                    "};";



    private final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();


    /**
     * We compile the function body if a function has not been registered yet.
     * @param dynamicValue the dynamic value.
     * @return the function name required to evaluate this dynamic value.
     */
    private <T> String functionNameOf(DynamicValue<T> dynamicValue, String template) {
        String valueUUID =  dynamicValue.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    String scriptBody = (String) dynamicValue.getBody();
                    String functionDefinition = format(template, functionName, ScriptUtils.unwrap(scriptBody));
                    try {
                        engine.eval(functionDefinition);
                        ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                    } catch (ScriptException e) {
                        throw new ESBException(e);
                    }
                }
            }
        }
        return functionName;
    }

    private <T> String functionNameOf(DynamicMap<T> dynamicMap) {
        String valueUUID =  dynamicMap.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.getOrDefault(valueUUID, null);
        if (functionName == null) {
            synchronized (this) {
                if (functionName == null) {
                    functionName = "fun" + valueUUID;
                    EvaluateMapFunction<T> evaluateMapFunction = new EvaluateMapFunction<>(functionName, dynamicMap);
                    String functionDefinition = evaluateMapFunction.script();
                    try {
                        engine.eval(functionDefinition);
                        ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                    } catch (ScriptException e) {
                        throw new ESBException(e);
                    }
                }
            }
        }
        return functionName;
    }
}
