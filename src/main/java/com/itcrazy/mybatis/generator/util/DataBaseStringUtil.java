package com.itcrazy.mybatis.generator.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: itcrazy0717
 * @version: $ DataBaseStringUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class DataBaseStringUtil {

    /**
     * 数据库表名驼峰转换  my_test -> MyTest
     * by itcrazy0717
     *
     * @param str
     * @return
     */
    public static String tableNameToCamelStyle(String str) {
        if (StringUtils.isNotBlank(str)) {
            StringBuilder sb = new StringBuilder();
            String lowerStr = str.toLowerCase();
            boolean nextUpper = true;
            for (char c : lowerStr.toCharArray()) {
                if (c == '_') {
                    nextUpper = true;
                } else {
                    if (nextUpper) {
                        sb.append(Character.toUpperCase(c));
                        nextUpper = false;
                    } else {
                        sb.append(c);
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }

}
