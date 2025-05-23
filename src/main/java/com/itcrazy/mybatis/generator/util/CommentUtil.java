package com.itcrazy.mybatis.generator.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

/**
 * @author: itcrazy0717
 * @version: $ CommentUtil.java,v0.1 2024-09-30 21:07 itcrazy0717 Exp $
 * @description:注解工具
 */
public class CommentUtil {

    /**
     * 增加方法注解
     * by itcrazy0717
     *
     * @param method
     * @param comment
     */
    public static void addMethodComment(Method method, String comment) {
        List<String> javaDocLines = method.getJavaDocLines();
        List<String> newJavaDocLines = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(javaDocLines)) {
            for (String javaDocLine : javaDocLines) {
                newJavaDocLines.add(javaDocLine);
                if ("/**".equals(javaDocLine)) {
                    newJavaDocLines.add(comment);
                }
            }
        } else {
            newJavaDocLines.add("/**");
            if (StringUtils.isNotBlank(comment)) {
                newJavaDocLines.add(comment);
            }
            List<Parameter> parameters = method.getParameters();
            if (CollectionUtils.isNotEmpty(parameters)) {
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
