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
		    char firstChar = str.charAt(0);
		    if (firstChar != '_') {
			    sb.append(String.valueOf(firstChar).toUpperCase());
		    }
		    int length = str.length();
		    for (int index = 1; index < length; index++) {
                char stringChar = str.charAt(index);
                if (stringChar != '_') {
                    sb.append(stringChar);
                } else {
                    if (index + 1 < length) {
                        sb.append(String.valueOf(str.charAt(index + 1)).toUpperCase());
                        index++;
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }

}
