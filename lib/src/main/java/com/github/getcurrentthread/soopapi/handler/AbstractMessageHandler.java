package com.github.getcurrentthread.soopapi.handler;

import com.github.getcurrentthread.soopapi.client.IChatMessageObserver;
import com.github.getcurrentthread.soopapi.model.Message;
import com.github.getcurrentthread.soopapi.model.MessageType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractMessageHandler implements IChatMessageObserver {
    private static final Logger LOGGER = Logger.getLogger(AbstractMessageHandler.class.getName());
    private final Map<MessageType, List<HandlerMethod>> messageHandlers = new HashMap<>();

    protected static class HandlerMethod {
        private final Method method;
        private final List<ParameterInfo> parameters;

        public HandlerMethod(Method method) {
            this.method = method;
            this.parameters = analyzeParameters(method);
        }

        private List<ParameterInfo> analyzeParameters(Method method) {
            List<ParameterInfo> params = new ArrayList<>();
            Parameter[] parameters = method.getParameters();

            for (Parameter param : parameters) {
                if (param.isAnnotationPresent(MessageParam.class)) {
                    MessageParam annotation = param.getAnnotation(MessageParam.class);
                    String name = annotation.value().isEmpty() ? param.getName() : annotation.value();
                    params.add(new ParameterInfo(ParameterType.DATA_FIELD, name, param.getType()));
                } else if (param.isAnnotationPresent(RawMessage.class)) {
                    params.add(new ParameterInfo(ParameterType.RAW, null, String.class));
                } else if (param.getType() == Message.class) {
                    params.add(new ParameterInfo(ParameterType.MESSAGE, null, Message.class));
                }
            }
            return params;
        }
    }

    protected enum ParameterType {
        MESSAGE,
        RAW,
        DATA_FIELD
    }

    protected static class ParameterInfo {
        private final ParameterType type;
        private final String name;
        private final Class<?> parameterClass;

        public ParameterInfo(ParameterType type, String name, Class<?> parameterClass) {
            this.type = type;
            this.name = name;
            this.parameterClass = parameterClass;
        }
    }

    public AbstractMessageHandler() {
        initializeHandlers();
    }

    private void initializeHandlers() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            MessageMapping mapping = method.getAnnotation(MessageMapping.class);
            if (mapping != null) {
                method.setAccessible(true);
                HandlerMethod handler = new HandlerMethod(method);
                for (MessageType type : mapping.value()) {
                    messageHandlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
                }
            }
        }
    }

    @Override
    public void notify(Message message) {
        List<HandlerMethod> handlers = messageHandlers.get(message.getType());
        if (handlers != null) {
            for (HandlerMethod handler : handlers) {
                try {
                    Object[] args = resolveParameters(handler, message);
                    handler.method.invoke(this, args);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error handling message", e);
                }
            }
        }
    }

    private Object[] resolveParameters(HandlerMethod handler, Message message) {
        Object[] args = new Object[handler.parameters.size()];

        for (int i = 0; i < handler.parameters.size(); i++) {
            ParameterInfo param = handler.parameters.get(i);

            switch (param.type) {
                case MESSAGE:
                    args[i] = message;
                    break;
                case RAW:
                    args[i] = message._getRaw();
                    break;
                case DATA_FIELD:
                    args[i] = convertValue(message.getData().get(param.name), param.parameterClass);
                    break;
            }
        }

        return args;
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 기본 타입 변환
        if (targetType == String.class) {
            return String.valueOf(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        } else if (targetType == Long.class || targetType == long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            return Boolean.parseBoolean(value.toString());
        }

        return value;
    }
}