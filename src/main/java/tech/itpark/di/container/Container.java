package tech.itpark.di.container;

import tech.itpark.di.exception.AmbiguousConstructorException;
import tech.itpark.di.exception.DIException;
import tech.itpark.di.exception.UnmetDependencyException;

import java.lang.reflect.Constructor;
import java.util.*;

public class Container {
    private final Map<String, Object> objects = new HashMap<>();
    private final Map<String, Definition> definitions = new HashMap<>();

    public void register(Definition... definitions) {
        for (Definition definition : definitions) {
            this.definitions.put(definition.getName(), definition);
        }
    }

    public void wire() {
        for (Definition definition : definitions.values()) {
            if (definition.getDependencies().length == 0) {
                createObject(definition);
                continue;
            }
            hop(definition);
        }
    }

    public void hop(Definition definition) {
        if (objects.containsKey(definition.getName())) return;
        for (String dependency : definition.getDependencies()) {
            if (!objects.containsKey(dependency)) {
                hop(definitions.get(dependency));
            }
        }
        createObject(definition);
    }

    public void createObject(Definition definition) {
        try {
            final Class<?> cls = Class.forName(definition.getName());
            if(cls.isInterface()) return;
            final Constructor<?>[] constructors = cls.getConstructors();
            if (constructors.length != 1) {
                throw new AmbiguousConstructorException("Component must have only one public constructor");
            }
            final Constructor<?> constructor = constructors[0];
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final List<Object> parameters = new LinkedList<>();
            for (Class<?> parameterType : parameterTypes) {
                final Object parameter = objects.get(parameterType.getName());
                if (parameter == null) {
                    throw new UnmetDependencyException("Unmet dependency for " + cls.getName() +
                            ". Not found parameter with type " + parameterType.getName());
                }
                parameters.add(parameter);
            }
            final Object o = constructor.newInstance(parameters.toArray());
            objects.put(o.getClass().getName(), o);
            for (Class<?> iface : cls.getInterfaces()) {
                objects.put(iface.getName(), o);
            }
        } catch (Exception e) {
            throw new DIException(e);
        }
    }

}