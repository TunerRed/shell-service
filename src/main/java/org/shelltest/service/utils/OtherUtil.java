package org.shelltest.service.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class OtherUtil {
    private static final SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * 获取距离当前一定时间的日期.
     * @param monthGap 与当前月相差的月份，正数表示向未来推，负数表示向过去退
     * @param day 指定月的第几天，0表示上个月的最后一天
     * @param dateFormat 返回的日期格式
     * @return 指定日期格式化后的字符串
     * */
    public static String getFormatDateInMonth(int monthGap, int day, SimpleDateFormat dateFormat) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, monthGap);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        if (dateFormat != null) {
            return dateFormat.format(calendar.getTime());
        }
        return defaultFormat.format(calendar.getTime());
    }
    /**
     * 获取距离当前一定时间的日期.
     * @param monthGap 与当前月相差的月份
     * @param day 指定月的第几天
     * @return 指定日期按默认格式格式化后的字符串
     * */
    public static String getFormatDateInMonth(int monthGap, int day) {
        return getFormatDateInMonth(monthGap, day, null);
    }


    /**
     * 加工字符串.
     * @param originName 源字符串
     * @param prefixList 待去除的前缀列表
     * @param suffixList 待去除的后缀列表
     * @return 源字符串清除前缀后缀之后的字符串
     * */
    public static String getRename(String originName, List<String> prefixList, List<String> suffixList) {
        String rename = originName;
        if (prefixList != null)
            for (int i = 0; i < prefixList.size(); i++)
                if (rename.startsWith(prefixList.get(i)))
                    rename = rename.substring(prefixList.get(i).length());
        if (suffixList != null)
            for (int i = 0; i < suffixList.size(); i++)
                if (rename.endsWith(suffixList.get(i)))
                    rename = rename.substring(0, rename.length() - suffixList.get(i).length());
        return rename;
    }
}
