package com.mio.libpatcher.transformer;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhysXTransformer implements BaseTransformer {

    @Override
    public boolean isTargetClass(String className) {
        if (className == null) return false;
        String dottedName = className.replace('/', '.');
        boolean match = dottedName.contains("de.fabmax.physxjni") || dottedName.contains("de.fabmax.physxandroid") || dottedName.contains("physx.");
        if (!match && dottedName.contains("physx")) {
            com.mio.libpatcher.util.LogUtil.info("PhysX possible match ignored: " + dottedName);
        }
        return match;
    }

    @Override
    public void transform(CtClass clazz) throws Throwable {
        List<CtBehavior> behaviors = new ArrayList<>();
        behaviors.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        behaviors.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
        if (clazz.getClassInitializer() != null) {
            behaviors.add(clazz.getClassInitializer());
        }

        for (CtBehavior behavior : behaviors) {
            behavior.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if ((m.getClassName().equals("java.lang.System") || m.getClassName().equals("java.lang.Runtime"))
                            && (m.getMethodName().equals("load") || m.getMethodName().equals("loadLibrary"))) {
                        m.replace(
                                "{ " +
                                        "   String libPath = java.lang.System.getProperty(\"sable_rapier_path\");" +
                                        "   if (libPath != null) {" +
                                        "       java.lang.System.out.println(\"[MioLibPatcher] PhysX load intercepted. Redirecting to: \" + libPath);" +
                                        "       java.lang.System.load(new java.io.File(libPath).getAbsolutePath());" +
                                        "   } else {" +
                                        "       java.lang.System.out.println(\"[MioLibPatcher] PhysX load NOT intercepted (sable_rapier_path is null)\");" +
                                        "       $_ = $proceed($$);" +
                                        "   }" +
                                        "}"
                        );
                    }
                }
            });
        }
    }
}
