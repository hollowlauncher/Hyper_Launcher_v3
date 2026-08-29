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
    /**
     * @return Exhaustive list of all 5 visitor classes in ASM 5.0.4
     */
    @Override
    public List<String> getTargetClassNames() {
        List<String> list = new ArrayList<>();
        /*
        We use ASM 5.0.4 as the override for older ASM versions, forge never shipped with it. So
        let's assume that if its 5.0.4, we overrid the requested ASM version and apply the bug
        backport.
         */
        if (!isASM504()) return list;
        list.add("org.objectweb.asm.ClassVisitor");
        list.add("org.objectweb.asm.MethodVisitor");
        list.add("org.objectweb.asm.FieldVisitor");
        list.add("org.objectweb.asm.AnnotationVisitor");
        list.add("org.objectweb.asm.signature.SignatureVisitor");
        return list;
    }

    /**
     * WARNING: Should only be used on ASM 5.0.4
     * Launchers can force the decision via -Dmiolibpatcher.asmBackport=true/false.
     * @throws CannotCompileException If used on the wrong class.
     */
    @Override
    public void transform(CtClass clazz) throws CannotCompileException {
        for (CtConstructor ctor : clazz.getDeclaredConstructors()) {
            if (!ctor.isClassInitializer()) {
                CodeIterator it = ctor.getMethodInfo().getCodeAttribute().iterator();
                // This is a bit janky, but it works for all five classes without manually
                // setting their Java source bodies.
                /*
                   What this does:
                     public ClassVisitor(final int api, final ClassVisitor cv) {
                        if (api != Opcodes.ASM4) {
                            throw new IllegalArgumentException(); // NOPs this part
                        }
                        this.api = api;
                        this.cv = cv; // This is unique to ClassVisitor
                     }
                   "throw new IllegalArgumentException()" compiles to this bytecode:
                     new
                     dup
                     invokespecial
                     athrow
                 */
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


                        // NOP the entire four instructions.
                        // I checked, we can assume at least this much of all five classes.
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

    private boolean isASM504() {
        // 启动器可通过系统属性强制指定是否启用该补丁
        String override = System.getProperty("miolibpatcher.asmBackport");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        if (isASM504Result == null) {
            isASM504Result = detectASM504();
        }
        return isASM504Result;
    }

    private static boolean detectASM504() {
        try {
            // Ensure we do NOT initialize the class, otherwise some mod loaders (fabric) can
            // cause duplicate class to load in their classloader, causing a crash.
            Class<?> asmClass = Class.forName("org.objectweb.asm.ClassReader", false, ClassLoader.getSystemClassLoader());
            Package asmPackage = asmClass.getPackage();
            String implVersion = asmPackage.getImplementationVersion();
            return "5.0.4".equals(implVersion);
        } catch (Exception e) {
            LogUtil.info("Unable to get ASM version info, ASMTransformer patch will be skipped: " + e);
        }
        return false;
    }
}
