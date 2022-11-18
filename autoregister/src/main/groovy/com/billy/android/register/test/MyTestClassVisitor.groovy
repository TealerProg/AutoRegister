package com.billy.android.register.test

import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
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
//        project.logger.error(" access:"+access+" name:"+name+" descriptor:"+descriptor+" signature:"+signature+" exceptions:"+exceptions)
       MethodVisitor mv=super.visitMethod(access,name,descriptor,signature,exceptions)

        if(name=="register"){
          //记得函数名叫做register
            // access:8 name:register descriptor:(Lcom/billy/app_lib_interface/ICategory;)V signature:null exceptions:null
            //找到函数名之后，要改写这个方法
            mv=new MyMethodVisitor(Opcodes.ASM5,mv)

        }
    }

    class MyMethodVisitor extends MethodVisitor{

        MyMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor)
        }

        @Override
        void visitInsn(int opcode) {
            //要重写这个方法，这个方法 简单理解就是整个方法调用都是在执行这个指令调用 Insn 其实就是instrument
            super.visitInsn(opcode)
        }
    }


}