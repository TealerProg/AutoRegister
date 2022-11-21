package com.billy.android.register.test

import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MyTestClassVisitor extends ClassVisitor{

    Project project
    MyTestClassVisitor(Project tempProject,int api, ClassVisitor classVisitor) {
        super(api, classVisitor)
        this.project=tempProject
    }

    /**
     * 这个类访问器，我们主要用来访问 com/billy/app_lib_interface/CategoryManager
     * 的register方法
     * @param access
     * @param name
     * @param descriptor
     * @param signature
     * @param exceptions
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

       MethodVisitor mv=super.visitMethod(access,name,descriptor,signature,exceptions)

        if(name=="<clinit>"){
            project.logger.error(" access:"+access+" name:"+name+" descriptor:"+descriptor+" signature:"+signature+" exceptions:"+exceptions)
          //记得函数名叫做register
            // access:8 name:register descriptor:(Lcom/billy/app_lib_interface/ICategory;)V signature:null exceptions:null
            //找到函数名之后，要改写这个方法
            mv=new MyMethodVisitor(Opcodes.ASM5,mv)

        }
        return mv
    }

    class MyMethodVisitor extends MethodVisitor{

        MyMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitInsn(int opcode) {
            //要重写这个方法，这个方法 简单理解就是整个方法调用都是在执行这个指令调用 Insn 其实就是instrument
            if(opcode>=Opcodes.IRETURN && opcode<=Opcodes.RETURN){
                //这里就是我们插入代码的核心，这里我们要借助一个插件  ASM Bytecode Viewer
               mv.visitTypeInsn(Opcodes.NEW, "com/billy/app_lib/CategoryA");
               mv.visitInsn(Opcodes.DUP);
               mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/billy/app_lib/CategoryA", "<init>", "()V", false);
               mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/billy/app_lib_interface/CategoryManager", "register", "(Lcom/billy/app_lib_interface/ICategory;)V", false);


            }

            super.visitInsn(opcode)
        }
    }


}