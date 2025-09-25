package com.itcrazy.mybatis.generator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: dengxin.chen
 * @version: $ FixTableNameUtil.java,v0.1 2025-09-24 17:02 dengxin.chen Exp $
 * @description:
 */
public class FixTableNameUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisCodeGenerateUtil.class);

    /**
     * 修复数据表名双点问题
     * by itcrazy0717
     *
     * @param xmlFile     xml文件
     * @param catalogName 数据库名称
     * @param tableName   表名
     * @return
     */
    public static void fixTableName(File xmlFile, String catalogName, String tableName) {
        try {
            // 读取并处理XML内容（替换双点）
            if (xmlFile.exists()) {
                String xmlContent = readXmlContent(xmlFile);
                String fixedContent = fixTableName(xmlContent, catalogName, tableName);
                // 写入处理后的内容
                writeXmlContent(xmlFile, fixedContent);
            }
        } catch (Exception e) {
            LOGGER.error("xml文件生成异常", e);
            throw new RuntimeException("xml文件生成异常");
        }
    }

    /**
     * 读取xml内容
     * by itcrazy0717
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static String readXmlContent(File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 写入xml内容
     * by itcrazy0717
     *
     * @param file
     * @param content
     * @throws IOException
     */
    private static void writeXmlContent(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    /**
     * 修复表名双点问题
     * by itcrazy0717
     *
     * @param xmlContent
     * @param catalogName
     * @param tableName
     * @return
     */
    private static String fixTableName(String xmlContent, String catalogName, String tableName) {
        if (StringUtils.isBlank(xmlContent)) {
            return null;
        }
        String replaceTarget = catalogName + ".." + tableName;
        return xmlContent.replaceAll(replaceTarget, tableName);
    }
}
