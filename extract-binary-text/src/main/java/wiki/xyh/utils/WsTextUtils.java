package wiki.xyh.utils;

public class WsTextUtils {

    public static boolean isMess(String text) {
        if (text == null || text.isEmpty()) {
            return true; // 空文本认为是乱码
        }

        int chsCount = 0; // 中文字符数
        int notRec = 0;   // 不可识别字符数
        int enCount = 0;  // 英文字符数
        int numCount = 0; // 数字字符数
        int symCount = 0; // 符号字符数
        int length = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 忽略空白和特殊空格、×
            if (Character.isWhitespace(c) || c == '\u00A0' || c == '×') {
                continue;
            }

            length++;

            if (isChinese(c)) {
                chsCount++;
            } else if (isEnglish(c)) {
                enCount++;
            } else if (isNumber(c)) {
                numCount++;
            } else if (isSymbol(c)) {
                symCount++;
            } else {
                notRec++;
            }
        }

        // 判断是否为乱码
        return length * 0.8 < enCount ||
                length * 0.1 < notRec ||
                (length * 0.5 > chsCount && (numCount + symCount) < chsCount) ||
                length * 0.4 > chsCount ||
                length * 0.8 < numCount ||
                length * 0.8 < symCount;
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private static boolean isEnglish(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

    private static boolean isSymbol(char c) {
        return !isChinese(c) && !isEnglish(c) && !isNumber(c);
    }
}

//package wiki.xyh.utils;
//
//public class WsTextUtils {
//    public static boolean isMess(String text) {
//        if (text != null && text.length() > 0) {
//            String s = text.replaceAll("\\s|\\u00A0|×", "");
//
//            char[] chars = s.toCharArray();
//            int chsCount = 0; // 中文字符数
//            int notRec = 0;   // 不可识别字符数
//            int enCount = 0;  // 英文字符数
//            int numCount = 0; // 数字字符数
//            int symCount = 0; // 符号字符数
//            int length = s.length();
//
//            for (char c : chars) {
//                if (isChinese(c)) {
//                    chsCount++;
//                } else if (isEnglish(c)) {
//                    enCount++;
//                } else if (isNumber(c)) {
//                    numCount++;
//                } else if (isSymbol(c)) {
//                    symCount++;
//                } else {
//                    notRec++;
//                }
//            }
//
//            // 根据字符统计结果判断是否为乱码
//            return length * 0.8 < enCount ||
//                    length * 0.1 < notRec ||
//                    (length * 0.5 > chsCount && (numCount + symCount) < chsCount) ||
//                    length * 0.4 > chsCount ||
//                    length * 0.8 < numCount ||
//                    length * 0.8 < symCount;
//        }
//        return true; // 如果文本为空或长度为0，认为是乱码
//    }
//
//    // 判断是否为中文字符
//    private static boolean isChinese(char c) {
//        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
//        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
//    }
//
//    // 判断是否为英文字符
//    private static boolean isEnglish(char c) {
//        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
//    }
//
//    // 判断是否为数字字符
//    private static boolean isNumber(char c) {
//        return (c >= '0' && c <= '9');
//    }
//
//    // 判断是否为符号
//    private static boolean isSymbol(char c) {
//        return !isChinese(c) && !isEnglish(c) && !isNumber(c) && !Character.isWhitespace(c);
//    }
//}
