package com.mio.libpatcher.transformer;

import java.util.ArrayList;
import java.util.List;

import com.mio.libpatcher.util.LogUtil;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;

/**
 * For ASM 4.1 and above, it properly checks if proper Opcode is passed, but Applied Energistics 1
 * passes something completely invalid while using earlier versions. So backport the bug in case
 * some other smart guy mod also does something silly.
 */
public class ASMTransformer implements BaseTransformer {

    private static Boolean isASM504Result;

    @Override
    public List<String> getTargetClassNames() {
        List<String> list = new ArrayList<>();
        list.add("org.objectweb.asm.ClassVisitor");
        list.add("org.objectweb.asm.MethodVisitor");
        list.add("org.objectweb.asm.FieldVisitor");
        list.add("org.objectweb.asm.AnnotationVisitor");
        list.add("org.objectweb.asm.signature.SignatureVisitor");
        return list;
    }

    @Override
    public void transform(CtClass clazz) throws Throwable {
        // This should not be called anymore if we use the loader version, but keeping it for safety
    }

    @Override
    public void transform(CtClass clazz, ClassLoader loader) throws CannotCompileException {
        if (!isASM504(loader)) return;

        for (CtConstructor ctor : clazz.getDeclaredConstructors()) {
            if (!ctor.isClassInitializer()) {
                CodeIterator it = ctor.getMethodInfo().getCodeAttribute().iterator();
                while (it.hasNext()) {
                    try {
                        int pos = it.next();

                        if (it.byteAt(pos) != Opcode.NEW) continue;

                        int dup = it.next();
                        if (it.byteAt(dup) != Opcode.DUP) continue;

                        int invokespecial = it.next();
                        if (it.byteAt(invokespecial) != Opcode.INVOKESPECIAL) continue;

                        int athrow = it.next();
                        if (it.byteAt(athrow) != Opcode.ATHROW) continue;

                        for (int i = pos; i < athrow + 1; ++i) {
                            it.writeByte(Opcode.NOP, i);
                        }
                        break;
                    } catch (BadBytecode e) {
                        throw new CannotCompileException(
                                "Failed to parse bytecode while searching for the" +
                                        "IllegalArgumentException pattern, is this ASM 5.0.4?", e
                        );
                    }
                }
            }
        }
    }

    private boolean isASM504(ClassLoader loader) {
        String override = System.getProperty("miolibpatcher.asmBackport");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        if (isASM504Result != null) {
            return isASM504Result;
        }
        
        Boolean result = detectASM504(loader);
        if (result != null) {
            isASM504Result = result;
        }
        return isASM504Result != null && isASM504Result;
    }

    private static Boolean detectASM504(ClassLoader loader) {
        try {
            Class<?> asmClass = Class.forName("org.objectweb.asm.ClassReader", false, loader);
            Package asmPackage = asmClass.getPackage();
            if (asmPackage == null) return null;
            String implVersion = asmPackage.getImplementationVersion();
            if (implVersion == null) return null;
            return "5.0.4".equals(implVersion);
        } catch (ClassNotFoundException e) {
            return null; // Not found yet
        } catch (Exception e) {
            LogUtil.info("Unable to get ASM version info, ASMTransformer patch will be skipped: " + e);
            return false;
        }
    }
}
