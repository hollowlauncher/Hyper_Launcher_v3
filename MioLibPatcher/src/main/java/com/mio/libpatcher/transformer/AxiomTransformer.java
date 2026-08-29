package com.mio.libpatcher.transformer;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.Arrays;
import java.util.List;

public class AxiomTransformer implements BaseTransformer {

    @Override
    public List<String> getTargetClassNames() {
        return Arrays.asList(
                "imgui.moulberry92.ImGui",
                "imgui.moulberry.ImGui",
                "imgui.ImGui"
        );
    }

    @Override
    public void transform(CtClass clazz) throws Throwable {
        CtConstructor constructor = clazz.getClassInitializer();
        if (constructor == null) return;
        
        constructor.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().equals("java.lang.System") && 
                    (m.getMethodName().equals("load") || m.getMethodName().equals("loadLibrary"))) {
                    
                    m.replace(
                            "{ " +
                                    "   String path = System.getProperty(\"imgui.library.path\");" +
                                    "   if (path == null) path = System.getProperty(\"imgui.moulberry92.library.path\");" +
                                    "   if (path == null) path = System.getProperty(\"imgui.moulberry.library.path\");" +
                                    "   String name = System.getProperty(\"imgui.library.name\");" +
                                    "   if (name == null) name = System.getProperty(\"imgui.moulberry92.library.name\");" +
                                    "   if (name == null) name = System.getProperty(\"imgui.moulberry.library.name\");" +
                                    "   if (path != null && name != null) {" +
                                    "          String fullPath = new java.io.File(path, name).getAbsolutePath();" +
                                    "          System.out.println(\"[MioLibPatcher] Redirecting load to: \" + fullPath);" +
                                    "          System.load(fullPath);" +
                                    "   } else {" +
                                    "          $_ = $proceed($$);" +
                                    "   }" +
                                    "}"
                    );
                }
            }
        });
    }
}
