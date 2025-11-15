package wiki.xyh.utils;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @Author: XYH
 * @Date: 2023/4/12
 * @Description: 去除 Tika 依赖的文件类型检测工具类（支持批处理使用）
 */
public class FileTypeDetector {

    private static final byte[] DOC_MAGIC_NUMBER = {(byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1};
    private static final byte[] DOCX_MAGIC_NUMBER = {(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};
    private static final byte[] HTML_MAGIC_NUMBER = {(byte) 0x3C, (byte) 0x68, (byte) 0x74, (byte) 0x6D};  // <html
    private static final byte[] PDF_MAGIC_NUMBER = {(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46};
    private static final byte[] XLS_MAGIC_NUMBER = DOC_MAGIC_NUMBER;
    private static final byte[] XLSX_MAGIC_NUMBER = DOCX_MAGIC_NUMBER;
    private static final byte[] RTF_MAGIC_NUMBER = {(byte) 0x7B, (byte) 0x5C, (byte) 0x72, (byte) 0x74, (byte) 0x66};

    /**
     * 检测文件类型（去除 Tika）
     * @param bytes 文件内容字节数组
     */
    public static FileType detectFileType(byte[] bytes) {
        if (bytes == null || bytes.length < 8) {
            return FileType.UNKNOWN;
        }

        try {
            if (isHTMLFile(bytes)) return FileType.HTML;
            if (compareMagicNumber(bytes, RTF_MAGIC_NUMBER)) return FileType.RTF;
            if (isTextFile(bytes, StandardCharsets.UTF_8)) return FileType.TXT;
            if (isOFDFile(bytes)) return FileType.OFD;

            byte[] headerBytes = Arrays.copyOf(bytes, 8);
            if (compareMagicNumber(headerBytes, DOC_MAGIC_NUMBER)) return FileType.DOC;
            if (compareMagicNumber(headerBytes, DOCX_MAGIC_NUMBER)) return detectZipBasedType(bytes);
            if (compareMagicNumber(headerBytes, PDF_MAGIC_NUMBER)) return FileType.PDF;
            if (compareMagicNumber(headerBytes, XLS_MAGIC_NUMBER)) return FileType.XLS;
            if (compareMagicNumber(headerBytes, XLSX_MAGIC_NUMBER)) return detectZipBasedType(bytes);
            if (JudgeImage.isImage(bytes)) return FileType.JPEG;

        } catch (Exception e) {
            // 可以添加日志
        }

        return FileType.UNKNOWN;
    }

    /** 判断是否为OFD文件（ZIP包中包含 OFD.xml） */
    public static boolean isOFDFile(byte[] bytes) {
        try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if ("OFD.xml".equals(entry.getName())) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    /** 比较头部魔数 */
    private static boolean compareMagicNumber(byte[] bytes, byte[] magic) {
        if (bytes.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (bytes[i] != magic[i]) return false;
        }
        return true;
    }

    /** 检测 Zip 包类型，是 docx 还是 xlsx */
    private static FileType detectZipBasedType(byte[] bytes) {
        try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("word/")) return FileType.DOCX;
                if (name.startsWith("xl/")) return FileType.XLSX;
            }
        } catch (Exception ignored) {}
        return FileType.UNKNOWN;
    }

    /** 判断是否为 HTML 文件 */
    public static boolean isHTMLFile(byte[] bytes) {
        String head = new String(bytes, 0, Math.min(bytes.length, 100), StandardCharsets.UTF_8).toLowerCase();
        return head.contains("<html") || head.contains("<!doctype html");
    }

    /**
     * 判断是否为纯文本（尝试 UTF-8 解码 + 控制字符检测）
     */
    public static boolean isTextFile(byte[] bytes, Charset charset) {
        if (bytes == null || charset == null) return false;

        // 1. UTF-8 解码验证
        try {
            CharsetDecoder decoder = charset.newDecoder();
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }

        // 2. 控制字符检测
        for (int i = 0; i < Math.min(bytes.length, 512); i++) {
            byte b = bytes[i];
            if ((b >= 0 && b < 9) || (b > 13 && b < 32)) {
                return false;
            }
        }
        return true;
    }

    /** 文件类型枚举 */
    public enum FileType {
        DOC, DOCX, HTML, PDF, XLS, XLSX, RTF, TXT, OFD, JPEG, XML, UNKNOWN
    }
}
