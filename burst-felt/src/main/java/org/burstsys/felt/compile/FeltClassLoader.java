/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.compile;

import java.util.HashMap;

/**
 * The term closure here refers to the bytecode that can be turned into a class.
 */
public class FeltClassLoader extends ClassLoader {

    static {
        // cash prizes if you can figure out how to do this in a scala classloader implementation!
        ClassLoader.registerAsParallelCapable();
    }

    private final HashMap<String, byte[]> classByteCodes = new HashMap<>();
    private final HashMap<String, Class<?>> classDefinitions = new HashMap<>();

    public FeltClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void addClassByteCode(String name, byte[] bytes) {
        synchronized (classByteCodes) {
            classByteCodes.put(name, bytes);
        }
    }

    /**
     * remove class artifacts by classname
     * @param className
     */
    public void removeClassesByName(String className) {
        synchronized (classByteCodes) {
            if(classByteCodes.containsKey(className)) {
                throw new RuntimeException(String.format("FELT_CLASS_LOADER class %s removal failed because it was in closures", className));
            }
            if(classDefinitions.remove(className) == null) {
                throw new RuntimeException(String.format("FELT_CLASS_LOADER class %s removal failed because it wasn't in definitions", className));
            }
        }
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name, false);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("FELT_CLASS_LOADER class %s not found", name));
        }

    }

    @Override
    protected Class<?> findClass(String name) {
        try {
            synchronized (classByteCodes) {
                Class<?> classDefinition = classDefinitions.get(name);
                if (classDefinition != null) return classDefinition;
                byte[] byteCode;
                byteCode = classByteCodes.get(name);
                if (byteCode != null) {
                    classByteCodes.remove(name);
                    classDefinition = defineClass(name, byteCode, 0, byteCode.length);
                    classDefinitions.put(name, classDefinition);
                    return classDefinition;
                } else {
                    return super.findClass(name);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("FELT_CLASS_LOADER class %s not found", name));
        }
    }

}
