package top.kscn.service;

import java.util.*;

/** 只由结构化属性生成CSS，不直接接受文档中的任意CSS或URL。 */
final class OfficePreviewCss {
    private OfficePreviewCss() {}
    static String color(String value) { return value != null && value.matches("(?i)[0-9a-f]{6}") ? "#" + value : null; }
    static String font(String value) { return value == null ? "sans-serif" : "'" + value.replaceAll("[\\p{Cntrl}'\";{}<>\\\\]", "") + "',sans-serif"; }
    static String pt(double value) { return number(value) + "pt"; }
    static String px(double value) { return number(value) + "px"; }
    static String number(double value) { return String.format(Locale.ROOT, "%.2f", Math.max(-2000, Math.min(4000, Double.isFinite(value) ? value : 0))); }
    static void put(Map<String, String> target, String key, String value) { if (value != null) target.put(key, value); }
}
