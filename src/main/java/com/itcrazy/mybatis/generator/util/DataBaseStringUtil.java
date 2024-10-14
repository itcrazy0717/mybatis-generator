package com.itcrazy.mybatis.generator.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: itcrazy0717
 * @version: $ DataBaseStringUtil.java,v0.1 2024-09-30 17:15 itcrazy0717 Exp $
 * @description:
 */
public class DataBaseStringUtil {

	/**
	 * 数据库表驼峰转换  my_test -> MyTest
	 * by itcrazy0717
	 *
	 * @param str
	 * @return
	 */
	public static String tableNameToCamelStyle(String str) {
	    if (StringUtils.isNotBlank(str)) {
            StringBuilder sb = new StringBuilder();
		    char firstChar = str.charAt(0);
		    if (firstChar != '_') {
			    sb.append(String.valueOf(firstChar).toUpperCase());
		    }
            for (int i = 1; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c != '_') {
                    sb.append(c);
                } else {
                    if (i + 1 < str.length()) {
                        sb.append(String.valueOf(str.charAt(i + 1)).toUpperCase());
                        i++;
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }

}
