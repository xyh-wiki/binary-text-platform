package wiki.xyh.utils; /**
 * @Author: XYH
 * @Date: 2025-07-21
 * @Description: 使用文件魔数判断字节内容是否为图片，替代 Apache Tika，性能更优。
 */


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

//@Slf4j
public class JudgeImage {

    // 支持的图片文件头（魔数）前缀，用十六进制字符串表示
    private static final HashSet<String> imageMagicHeaders = new HashSet<>();

    static {
        imageMagicHeaders.add("FFD8FF");       // JPEG
        imageMagicHeaders.add("89504E47");     // PNG
        imageMagicHeaders.add("47494638");     // GIF
        imageMagicHeaders.add("49492A00");     // TIFF (little endian)
        imageMagicHeaders.add("4D4D002A");     // TIFF (big endian)
        imageMagicHeaders.add("424D");         // BMP
        imageMagicHeaders.add("38425053");     // PSD
        imageMagicHeaders.add("52494646");     // WebP, FPX 开头
    }

    /**
     * 判断二进制内容是否为图片类型
     *
     * @param bytes 文件字节内容
     * @return true 表示是图片
     */
    public static boolean isImage(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return false;

        // 读取前 4~8 个字节转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(8, bytes.length); i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        String fileHeader = sb.toString().toUpperCase();

        // 逐个比对魔数是否匹配
        for (String magic : imageMagicHeaders) {
            if (fileHeader.startsWith(magic)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        // 测试用例
        byte[] bytes = FileUtils.readFileToByteArray(new File("E:\\00-temp\\屏幕截图 2024-05-21 113600.png"));

        System.out.println("JPEG: " + isImage(bytes)); // true
    }
}
