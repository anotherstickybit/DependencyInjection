package tech.itpark.di.container;

import tech.itpark.di.exception.AmbiguousConstructorException;
import tech.itpark.di.exception.DIException;
import tech.itpark.di.exception.UnmetDependencyException;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class Container {
    private final Map<String, Object> objects = new HashMap<>();
    private final List<Class<?>> definitions = new LinkedList<>();

    public void register(List<Class<?>> classes) {
        this.definitions.addAll(classes);
    }

    public void wire() {
        int count = 0;
        while (true) {
            final ListIterator<Class<?>> iterator = definitions.listIterator();
            int prevCount = count;
            if (definitions.size() == 0) break;
            while (iterator.hasNext()) {
                final Class<?> cls = iterator.next();
                final Constructor<?>[] constructors = cls.getConstructors();
                if (constructors.length != 1) {
                    throw new AmbiguousConstructorException("Component must have only one public constructor");
                }
                final Constructor<?> constructor = constructors[0];
                final Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0) {
                    createObject(cls, new ArrayList<>(), constructor);
                    iterator.remove();
                    count++;
                    continue;
                }
                final List<Object> parameters = getConstructorParameters(parameterTypes);
                if (objects.keySet().containsAll(parameters.stream()
                        .map(i -> i.getClass().getName()).collect(Collectors.toList()))) {
                    createObject(cls, parameters, constructor);
                    iterator.remove();
                    count++;
                }
            }
            if (prevCount == count) {
                throw new UnmetDependencyException("Unmet dependencies for " + iterator.previous());
            }
        }
    }

    public List<Object> getConstructorParameters(Class<?>[] parameterTypes) {
        final List<Object> parameters = new LinkedList<>();
        for (Class<?> parameterType : parameterTypes) {
            final Object parameter = objects.get(parameterType.getName());
            if (!(parameter == null)) parameters.add(parameter);
        }
        return parameters;
    }

    public void createObject(Class<?> cls, List<Object> parameters, Constructor<?> constructor) {
        try {
            if (cls.isInterface()) return;
            final Object o = constructor.newInstance(parameters.toArray());
            objects.put(o.getClass().getName(), o);
            for (Class<?> iface : cls.getInterfaces()) {
                objects.put(iface.getName() + "_" + o.getClass().getName(), o);
            }
        } catch (Exception e) {
            throw new DIException(e);
        }
    }

}