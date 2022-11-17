package com.billy.android.register.test

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.billy.android.register.CodeScanProcessor
import jdk.internal.org.objectweb.asm.Opcodes
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarFile;


/**
 * 描述
 *
 * @author Created by lipengbo
 * @email 1162947801@qq.com
 * @time Created on 2022/11/17 13:45
 */
 class RegisterTransformTest extends Transform {

     Project project

     RegisterTransformTest(Project project){
       this.project=project
     }




     @Override
     String getName() {
         return "auto-register-test"
     }

     @Override
     Set<QualifiedContent.ContentType> getInputTypes() {
         return TransformManager.CONTENT_CLASS
     }

     @Override
     Set<? super QualifiedContent.Scope> getScopes() {
         return  TransformManager.SCOPE_FULL_PROJECT
     }

     @Override
     boolean isIncremental() {
         return false
     }

     @Override
     void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
       inputs.each {
           //先遍历Jar包
           TransformInput  input->
               input.jarInputs.each {
                   JarInput jarInput->
                       //先遍历Jar包
                       scanJar(jarInput,outputProvider)

               }

               input.directoryInputs.each { DirectoryInput  directoryInput->

                   // 获得产物的目录
                   File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                   String path = directoryInput.file.absolutePath
                   project.logger.error('path---'+path)
                   directoryInput.file.eachFileRecurse { File file->
                       if(file.isFile()){
                           asmScanClass(file)
                           project.logger.error('file---'+file.absolutePath)
                       }else {
                           project.logger.error('path---'+file.absolutePath)
                       }

                   }
                   // 处理完后拷到目标文件
                   FileUtils.copyDirectory(directoryInput.file, dest)
               }
       }


     }

     void scanJar(JarInput jarInput,TransformOutputProvider outputProvider) {
         def src=jarInput.file
         //遍历jar的字节码类文件，找到需要自动注册的类
         File dest = getDestFile(jarInput, outputProvider)
         // 获得输入文件
         def jarFile=new JarFile(src)
         def enumeration = jarFile.entries()
         //通过遍历获取class 然后打印出来 我们试试 记住先上传
         while (enumeration.hasMoreElements()){
             def jarEntry = enumeration.nextElement()
             def entryName = jarEntry.name
             if(entryName.startsWith("android/")||
                     entryName.startsWith("androidx/")||
                     entryName.startsWith("META-INF")){
                 continue
             }
             project.logger.error('jar---'+src.absolutePath+'  class---'+entryName)
         }
         //复制jar文件到transform目录：build/transforms/auto-register/
         FileUtils.copyFile(src, dest)
     }

     private void  asmScanClass(File file){
         InputStream inputStream=file.newInputStream()
         ClassReader cr=new ClassReader(inputStream)
         ClassWriter cw=new ClassWriter(cr,0)
        ScanClassVisitor cv=new ScanClassVisitor
                 (Opcodes.ASM5,cw,file.absolutePath)
         cr.accept(cv, ClassReader.EXPAND_FRAMES)
         inputStream.close()
     }

     class ScanClassVisitor extends ClassVisitor{
         private String filePath

         ScanClassVisitor(int api, ClassVisitor cv, String filePath) {
             super(api, cv)
             this.filePath = filePath
             project.logger.error('init---ScanClassVisitor')
         }
     }


     static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
         def destName = jarInput.name
         // 重名名输出文件,因为可能同名,会覆盖
         def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
         if (destName.endsWith(".jar")) {
             destName = destName.substring(0, destName.length() - 4)
         }
         // 获得输出文件
         File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
         return dest
     }



 }
