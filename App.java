package com.example.logindemo.test;

import java.io.*;
import java.lang.reflect.Field;

/**
 * @description: 在原来Entity文件的基础上生产builder类
 * @author: gk
 * @date: 2019/7/26 15:08
 * @since: jdk1.8
 */

public class App {

    private String app;

    public static void main(String[] args) throws Exception {

        String targetProject = "D:\\IntelliJ_IDEA_2018.1.6\\workspace\\logindemo\\src\\main\\java";
        Class<?> clazz = Post.class;

        // 默认生产的内部静态类为Builder
        func1(targetProject, clazz);
    }

    public static void func(String targetProject, String targetName, Class<?> clazz) throws Exception {

        String fileName = targetName + ".java";
        String packageName = clazz.getPackage().getName();
        String packagePath = packageName.replaceAll("\\.", "\\\\");
        String pathFileName = targetProject + "\\\\" + packagePath + "\\\\" + fileName;
        System.out.println("------   " + pathFileName);

        File file = new File(pathFileName);

        if (file.exists()) {
            file.deleteOnExit();
        }

        // 创建文件成功
        if (file.createNewFile()) {
            OutputStream outputStream = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(outputStream);

            String clazzName = clazz.getSimpleName();
            String clazzNameLowerCase = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);

            writer.write("package " + packageName + ";" + "\n"
                    + "\n"
                    + "public class " + targetName + " {" + "\n"
                    + "\tprivate " + clazzName + " " + clazzNameLowerCase + ";" + "\n"
                    + "\n"
                    + "\tpublic " + targetName + "() {" + "\n"
                    + "\t\tthis." + clazzNameLowerCase + " = new " + clazzName + "();" + "\n"
                    + "\t}" + "\n"
                    + "\n"
            );

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String fieldTypeSimpleName = field.getType().getSimpleName();
                String fieldName = field.getName();
                String addName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                String setMethod = "set" + addName;
                writer.write("\tpublic " + targetName + " add" + addName + "(" + fieldTypeSimpleName + " " + fieldName + ") {" + "\n"
                        + "\t\t" + clazzNameLowerCase + "." + setMethod + "(" + fieldName + ");" + "\n"
                        + "\t\t" + "return this;" + "\n"
                        + "\t}" + "\n"
                        + "\n"
                );
            }

            writer.write("\tpublic " + clazzName + " build() {" + "\n"
                    + "\t\t" + "return this." + clazzNameLowerCase + ";" + "\n"
                    + "\t}" + "\n"
            );

            writer.write("}" + "\n");

            writer.flush();
            writer.close();
            outputStream.close();
        }
    }

    public static void func1(String targetProject, Class<?> clazz) throws Exception {

        String targetName = "Builder";
        String fileName = clazz.getSimpleName() + ".java";
        String packageName = clazz.getPackage().getName();
        String packagePath = packageName.replaceAll("\\.", "\\\\");
        String pathFileName = targetProject + "\\\\" + packagePath + "\\" + fileName;
        System.out.println("--- " + pathFileName);

        File file = new File(pathFileName);

        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }

        // 找到最后一个}位置
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new RuntimeException("文件太大了, 不支持");
        }

        byte[] buf = new byte[(int)fileSize];
        String fileContext = String.valueOf(buf);
        int lastPos = fileContext.lastIndexOf("}");

        if (lastPos < 0) {
            lastPos += (int)fileSize;
        }

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(lastPos-1);

        String clazzName = clazz.getSimpleName();
        String clazzNameLowerCase = clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);

        raf.writeBytes("\n"
                + "\tpublic static class " + targetName + " {" + "\n"
                + "\n"
                + "\t\tprivate " + clazzName + " " + clazzNameLowerCase + ";" + "\n"
                + "\n"
                + "\t\tpublic " + targetName + "() {" + "\n"
                + "\t\t\tthis." + clazzNameLowerCase + " = new " + clazzName + "();" + "\n"
                + "\t\t}" + "\n"
                + "\n"
        );

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldTypeSimpleName = field.getType().getSimpleName();
            String fieldName = field.getName();
            String addName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            String setMethod = "set" + addName;
            raf.writeBytes("\t\tpublic " + targetName + " add" + addName + "(" + fieldTypeSimpleName + " " + fieldName + ") {" + "\n"
                    + "\t\t\t" + clazzNameLowerCase + "." + setMethod + "(" + fieldName + ");" + "\n"
                    + "\t\t\t" + "return this;" + "\n"
                    + "\t\t}" + "\n"
                    + "\n"
            );
        }

        raf.writeBytes("\t\tpublic " + clazzName + " build() {" + "\n"
                + "\t\t\t" + "return this." + clazzNameLowerCase + ";" + "\n"
                + "\t\t}" + "\n"
        );

        raf.writeBytes("\t}" + "\n");
        raf.writeBytes("}" + "\n");

        raf.close();
    }
}


