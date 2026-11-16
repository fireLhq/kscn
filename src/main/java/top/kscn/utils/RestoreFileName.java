package top.kscn.utils;

import java.util.function.Predicate;

/** 所有回收站共用的恢复命名规则，扩展名保持在最后。 */
public final class RestoreFileName {
    private RestoreFileName() {}

    public static String available(String original, boolean folder, Predicate<String> exists) {
        if (!exists.test(original)) return original;
        int dot = folder ? -1 : original.lastIndexOf('.');
        String base = dot > 0 ? original.substring(0, dot) : original;
        String extension = dot > 0 ? original.substring(dot) : "";
        for (int index = 1; ; index++) {
            String suffix = "恢复" + (index == 1 ? "" : index);
            // 超长扩展名也必须保证完整名称不超过数据库字段长度。
            String ending = extension.length() > 240 ? extension.substring(0, 240) : extension;
            int length = 255 - ending.length() - suffix.length();
            String candidate = base.substring(0, Math.min(base.length(), Math.max(0, length))) + suffix + ending;
            if (!exists.test(candidate)) return candidate;
        }
    }
}
