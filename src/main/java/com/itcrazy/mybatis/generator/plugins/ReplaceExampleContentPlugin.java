package com.itcrazy.mybatis.generator.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author: itcrazy0717
 * @version: $ ReplaceExampleContentPlugin.java,v0.1 2024-09-30 20:02 itcrazy0717 Exp $
 * @description:替换example内容，主要将example替换成param
 */
public class ReplaceExampleContentPlugin extends PluginAdapter {

	/**
	 * 需要替换的内容
	 */
	private String searchString;

	/**
	 * 替换后的内容
	 */
	private String replaceString;

	/**
	 * 是否使用简单方法
	 */
	private Boolean simpleMethod;

	/**
	 * 替换正则
	 */
	private Pattern pattern;

	@Override
    public boolean validate(List<String> warnings) {
        searchString = properties.getProperty("searchString");
        replaceString = properties.getProperty("replaceString");
        simpleMethod = Boolean.valueOf(properties.getProperty("simpleMethod"));

        boolean valid = stringHasValue(searchString) && stringHasValue(replaceString);

        if (valid) {
            pattern = Pattern.compile(searchString);
        } else {
            if (!stringHasValue(searchString)) {
                warnings.add(getString("ValidationError.18", //$NON-NLS-1$
                                       "RenameExampleClassPlugin", //$NON-NLS-1$
                                       "searchString")); //$NON-NLS-1$
            }
            if (!stringHasValue(replaceString)) {
                warnings.add(getString("ValidationError.18", //$NON-NLS-1$
                                       "RenameExampleClassPlugin", //$NON-NLS-1$
                                       "replaceString")); //$NON-NLS-1$
            }
        }

        return valid;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String exampleType = introspectedTable.getExampleType();
        Matcher matcher = pattern.matcher(exampleType);
        exampleType = matcher.replaceAll(replaceString);
        // 将DOParam替换成Param
        Pattern paramPattern = Pattern.compile("DOParam");
        Matcher paramMatcher = paramPattern.matcher(exampleType);
        exampleType = paramMatcher.replaceAll("Param");
        // 调整param对象路径
        String paramPackage = properties.getProperty("paramPackage");
        if (StringUtils.isNotBlank(paramPackage)) {
            String paramClassName = exampleType.substring(exampleType.lastIndexOf(".") + 1);
            introspectedTable.setExampleType(paramPackage + "." + paramClassName);
        } else {
            introspectedTable.setExampleType(exampleType);
        }
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        return renameModelExample(topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return renameXmlExample(element, introspectedTable, false);
    }

    @Override
    public boolean providerCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return replaceExample(method, introspectedTable);
    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        replaceExample(method, introspectedTable);

        return renameModelExample(topLevelClass, introspectedTable);
    }

    /**
     * 修改模型类里的example
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private boolean renameModelExample(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String paramName = introspectedTable.getExampleType();
        String converted = firstLetterLowerCase(extractClassBaseName(paramName));

        /**
         * 类的所有方法
         */
        List<Method> methods = topLevelClass.getMethods();
        List<Method> newMethods = new ArrayList<>();
        // 替换方法中的example
        for (Method method : methods) {
            List<String> lines = method.getBodyLines();
            List<String> newLines = new ArrayList<String>();
            for (String line : lines) {
                newLines.add(line.replaceAll("example", converted));
            }
            method.getBodyLines().clear();
            method.getBodyLines().addAll(newLines);

            // 替换参数中的example
            List<Parameter> parameters = method.getParameters();
            List<Parameter> newParameters = new ArrayList<Parameter>();
            for (Parameter parameter : parameters) {
                String parameterName = parameter.getName();
                if ("example".equals(parameterName)) {
                    String shortName = parameter.getType().getShortName();
                    String convertedWord = firstLetterLowerCase(shortName);
                    Parameter newParameter = new Parameter(parameter.getType(), convertedWord, false);
                    // 替换参数注解里的example
                    List<String> annotations = parameter.getAnnotations();
                    for (String anno : annotations) {
                        newParameter.addAnnotation(anno.replace("example", convertedWord));
                    }
                    newParameters.add(newParameter);
                } else {
                    newParameters.add(parameter);
                }
            }
            method.getParameters().clear();
            method.getParameters().addAll(newParameters);
            newMethods.add(method);
        }
        topLevelClass.getMethods().clear();
        topLevelClass.getMethods().addAll(newMethods);
        return true;
    }

    /**
     * 替换XML里的example和Example
     *
     * @param element
     * @param introspectedTable
     * @return
     */
    private boolean renameXmlExample(XmlElement element, IntrospectedTable introspectedTable, boolean all) {
        String paramName = introspectedTable.getExampleType();
        paramName = extractClassBaseName(paramName);
        recursiveReplaceXmlExample(element, paramName, all);
        return true;
    }

    /**
     * 递归替换XML里的example和Example
     * by itcrazy0717
     *
     * @param element
     * @param paramName
     * @param all
     */
    private void recursiveReplaceXmlExample(XmlElement element, String paramName, boolean all) {
        String lowerCaseParamName = firstLetterLowerCase(paramName);
        List<Attribute> attributeList = element.getAttributes();
        List<Attribute> newAttributeList = new ArrayList<Attribute>();
        for (Attribute attr : attributeList) {
            String name = attr.getName();
            if (simpleMethod && ("id".equals(name) || "refid".equals(name))) {
                String value = attr.getValue();
                value = value.replaceAll("example", lowerCaseParamName);
                Attribute newAttr = new Attribute(name, value.replaceAll("Example", replaceString));
                newAttributeList.add(newAttr);
            } else if (!"parameterType".equals(name)) {
                String value = attr.getValue();
                value = value.replaceAll("example", lowerCaseParamName);
                Attribute newAttr = new Attribute(name, value.replaceAll("Example", paramName));
                newAttributeList.add(newAttr);
            }
        }
        element.getAttributes().clear();
        element.getAttributes().addAll(newAttributeList);

        List<Element> elementList = element.getElements();
        List<Element> newElementList = new ArrayList<Element>();
        for (Element e : elementList) {
            if (e instanceof TextElement) {
                String content = ((TextElement) e).getContent();
                if (all) {
                    content = content.replaceAll("example", lowerCaseParamName);
                    Element newElement = new TextElement(content.replaceAll("Example", paramName));
                    newElementList.add(newElement);
                } else {
                    Element newElement = new TextElement(content);
                    newElementList.add(newElement);
                }
            } else if (e instanceof XmlElement) {
                /** 递归 */
                recursiveReplaceXmlExample((XmlElement) e, paramName, all);
                newElementList.add(e);
            }
        }
        element.getElements().clear();
        element.getElements().addAll(newElementList);
    }

    /**
     * 替换example
     * by itcrazy0717
     *
     * @param method
     * @param introspectedTable
     * @return
     */
    private boolean replaceExample(Method method, IntrospectedTable introspectedTable) {
        List<Parameter> parameters = method.getParameters();
        List<Parameter> newParameters = new ArrayList<>();
        for (Parameter parameter : parameters) {
            // 将参数中的example替换
            String parameterName = parameter.getName();
            if (StringUtils.equals("example", parameterName)) {
                String shortName = parameter.getType().getShortName();
                String lowerCaseShortName = firstLetterLowerCase(shortName);
                List<String> javaDocLines = method.getJavaDocLines();
                List<String> newJavaDocLines = new ArrayList<>();
                for (String s : javaDocLines) {
                    newJavaDocLines.add(s.replace("example", lowerCaseShortName));
                }
                javaDocLines.clear();
                javaDocLines.addAll(newJavaDocLines);
                Parameter newParameter = new Parameter(parameter.getType(), lowerCaseShortName, false);
                // 将参数注解里的example替换
                List<String> annotations = parameter.getAnnotations();
                for (String anno : annotations) {
                    newParameter.addAnnotation(anno.replaceAll("example", lowerCaseShortName));
                }
                newParameters.add(newParameter);
            } else {
                newParameters.add(parameter);
            }
        }
        method.getParameters().clear();
        method.getParameters().addAll(newParameters);

        // 将方法名里的Example替换
        String methodName = method.getName();
        String example = introspectedTable.getExampleType();
        String classBaseName = extractClassBaseName(example);
        String lowerCaseclassBaseName = firstLetterLowerCase(classBaseName);
        String newMethodName;
        if (simpleMethod) {
            newMethodName = methodName.replaceAll("Example", replaceString);
        } else {
            newMethodName = methodName.replaceAll("Example", classBaseName);
        }
        method.setName(newMethodName);

        // 将方法体里的example变量名替换
        List<String> bodyLines = method.getBodyLines();
        List<String> newBodyLines = new ArrayList<>();
        for (String line : bodyLines) {
            String newLine = line.replaceAll("example", lowerCaseclassBaseName);
            if (methodName.contains("WithoutBLOBs")) {
                newLine = newLine.replaceAll(methodName.replaceAll("WithoutBLOBs", ""), newMethodName.replaceAll("WithoutBLOBs", ""));
            } else {
                newLine = newLine.replaceAll(methodName, newMethodName);
            }
            newBodyLines.add(newLine);
        }
        method.getBodyLines().clear();
        method.getBodyLines().addAll(newBodyLines);

        // 将方法注解里的Example替换，用于SQL采用注解方式
        List<String> annotations = method.getAnnotations();
        List<String> newAnnotations = new ArrayList<String>();
        for (String anno : annotations) {
            if (simpleMethod) {
                newAnnotations.add(anno.replaceAll("Example", replaceString));
            } else {
                newAnnotations.add(anno.replaceAll("Example", classBaseName));
            }
        }
        method.getAnnotations().clear();
        method.getAnnotations().addAll(newAnnotations);
        return true;
    }

    /**
     * 将首字母小写
     * by itcrazy0717
     *
     * @param inputString
     * @return
     */
    private String firstLetterLowerCase(String inputString) {
        if (StringUtils.isBlank(inputString) || inputString.length() < 1) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        String first = inputString.substring(0, 1).toLowerCase();
        String last = inputString.substring(1);
        sb.append(first);
        sb.append(last);
        return sb.toString();
    }

    /**
     * 提取类名
     * by itcrazy0717
     *
     * @param classType
     * @return
     */
    private String extractClassBaseName(String classType) {
        String[] names = classType.split("[.]");
        return names[names.length - 1];
    }
}
