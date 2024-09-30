package com.itcrazy.mybatis.generator.util;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

/**
 * @author: dengxin.chen
 * @version: $ CommentUtil.java,v0.1 2024-09-30 21:07 dengxin.chen Exp $
 * @description:注解工具
 */
public class CommentUtil {

    /**
     * 增加方法注解
     * by dengxin.chen
     *
     * @param method
     * @param comment
     */
    public static void addMethodComment(Method method, String comment) {
        List<String> javaDocLines = method.getJavaDocLines();
        List<String> newJavaDocLines = new ArrayList<>();
        if (null != javaDocLines && !javaDocLines.isEmpty()) {
            for (String javaDocLine : javaDocLines) {
                newJavaDocLines.add(javaDocLine);
                if ("/**".equals(javaDocLine)) {
                    newJavaDocLines.add(comment);
                }
            }
        } else {
            newJavaDocLines.add("/**");
            newJavaDocLines.add(comment);
            List<Parameter> parameters = method.getParameters();
            if (null != parameters && !parameters.isEmpty()) {
                for (Parameter parameter : parameters) {
                    newJavaDocLines.add(" * @param " + parameter.getName());
                }
            }
            newJavaDocLines.add(" * @return");
            newJavaDocLines.add(" * ");
            newJavaDocLines.add(" * @mbg.generated");
            newJavaDocLines.add(" */");
        }
        method.getJavaDocLines().clear();
        method.getJavaDocLines().addAll(newJavaDocLines);
    }

}
