package com.itcrazy.mybatis.generator.util;

/**
 * @author: dengxin.chen
 * @version: $ MyStringUtils.java,v0.1 2024-09-30 17:15 dengxin.chen Exp $
 * @description:
 */
public class MyStringUtils {

    /**
     * convert string from slash style to camel style, such as a_b_c will convert to A_B_C
     *
     * @param str
     * @return
     */
    public static String dbStringToCamelStyle(String str) {
        if (str != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(str.charAt(0)).toUpperCase());
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

    /**
     * convert string from slash style to camel style, such as a_b_c will convert to A_B_C
     *
     * @param str
     * @return
     */
    public static String dbStringToCamelStyle2(String str) {
        if (str != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(str.charAt(0)));
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
