package com.mio.libpatcher.transformer;

import javassist.CtClass;
import javassist.CtMethod;

import java.util.Arrays;
import java.util.List;

public class PlatformTransformer implements BaseTransformer {
    @Override
    public List<String> getTargetClassNames() {
        return Arrays.asList("org.lwjgl.system.Platform", "org.lwjgl.system.Platform$Architecture");
    }

    @Override
    public void transform(CtClass clazz) throws Throwable {
        if (clazz.getName().equals("org.lwjgl.system.Platform")) {
            try {
                CtMethod method = clazz.getDeclaredMethod("get");
                method.setBody("return LINUX;");
            } catch (Exception ignored) {}
        } else if (clazz.getName().equals("org.lwjgl.system.Platform$Architecture")) {
            try {
                CtMethod method = clazz.getDeclaredMethod("get");
                method.setBody("return ARM64;");
            } catch (Exception ignored) {}
        }
    }
}
