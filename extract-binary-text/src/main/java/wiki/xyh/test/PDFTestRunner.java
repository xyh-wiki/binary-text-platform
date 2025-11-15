package wiki.xyh.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.xyh.utils.PDFWatermarkRemover;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @Author: XYH
 * @Date: 2025-07-29
 * @Description: 测试 PDF 去水印工具的调用性能，每 10 万次打印一次耗时
 */
@Slf4j
public class PDFTestRunner {

    /**
     * 读取 PDF 文件并循环调用 readPDF 方法
     */

    public static void main(String[] args) throws IOException {



        byte[] bytes = FileUtils.readFileToByteArray(new File("src/main/resources/binary-doc/1.pdf"));
        int totalLoops = 10;


        System.out.println("✅ 开始测试，循环调用次数: " + totalLoops);

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalLoops; i++) {
            String content = readPDF(bytes);

            System.out.println(content);

            // 每 10 万次打印一次耗时
            if (i % 100000 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("✅ 已调用 " + i + " 次，耗时: " + elapsed + " ms");
                startTime = System.currentTimeMillis(); // 重置时间以便下一段计时
            }
        }

        System.out.println("✅ 所有调用完成");
    }

    /**
     * 调用 PDF 去水印核心方法
     */
    public static String readPDF(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            return PDFWatermarkRemover.removeWatermarkBasedOnAngle(bytes);
        } catch (Exception ignored) {
            // 忽略所有异常，返回 null
        }
        return null;
    }
}
