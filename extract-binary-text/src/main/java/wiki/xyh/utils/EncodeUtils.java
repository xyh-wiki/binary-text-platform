package wiki.xyh.utils;

import lombok.NonNull;

import java.io.*;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EncodeUtils {

    private static final int BYTE_SIZE = 8;
    public static final String CODE_UTF8 = "UTF-8";
    public static final String CODE_UTF8_BOM = "UTF-8_BOM";
    public static final String CODE_GBK = "GBK";
    public static final String CODE_UTF16LE = "UTF-16LE";
    public static final String CODE_UTF16BE = "UTF-16BE";

    // ⚠ 缓存 key：可以是文件路径，也可以改为 hash 值（如 byte[] 的摘要）
    private static final Map<String, String> ENCODING_CACHE = new ConcurrentHashMap<>();

    /**
     * 主调用入口：支持 byte[] 编码识别 + 缓存
     */
    public static String detectEncoding(@NonNull byte[] bytes, boolean ignoreBom) {
        String cacheKey = generateKey(bytes);
        return ENCODING_CACHE.computeIfAbsent(cacheKey, k -> {
            try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
                bis.mark(1024);
                return getEncode(bis, ignoreBom);
            } catch (IOException e) {
                throw new RuntimeException("编码检测失败", e);
            }
        });
    }

    /**
     * 支持文件路径缓存（可扩展文件修改时间验证）
     */
    public static String detectEncoding(@NonNull File file, boolean ignoreBom) {
        String cacheKey = file.getAbsolutePath();
        return ENCODING_CACHE.computeIfAbsent(cacheKey, k -> {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                bis.mark(1024);
                return getEncode(bis, ignoreBom);
            } catch (IOException e) {
                throw new RuntimeException("编码检测失败: " + file.getName(), e);
            }
        });
    }

    /**
     * 核心编码判断逻辑
     */
    private static String getEncode(@NonNull BufferedInputStream bis, boolean ignoreBom) throws IOException {
        bis.mark(1024);
        byte[] head = new byte[3];
        bis.read(head);

        if (head[0] == (byte) 0xFF && head[1] == (byte) 0xFE) {
            return CODE_UTF16LE;
        } else if (head[0] == (byte) 0xFE && head[1] == (byte) 0xFF) {
            return CODE_UTF16BE;
        } else if (head[0] == (byte) 0xEF && head[1] == (byte) 0xBB && head[2] == (byte) 0xBF) {
            return ignoreBom ? CODE_UTF8 : CODE_UTF8_BOM;
        }

        return isUTF8(bis) ? CODE_UTF8 : CODE_GBK;
    }

    /**
     * 判断是否是无BOM的 UTF-8
     */
    private static boolean isUTF8(@NonNull BufferedInputStream bis) throws IOException {
        bis.reset();
        int code;
        while ((code = bis.read()) != -1) {
            BitSet bitSet = convert2BitSet(code);
            if (bitSet.get(0)) {
                if (!checkMultiByte(bis, bitSet)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkMultiByte(@NonNull BufferedInputStream bis, @NonNull BitSet bitSet) throws IOException {
        int count = getCountOfSequential(bitSet);
        if (count < 2 || count > 6) return false;

        byte[] bytes = new byte[count - 1];
        int read = bis.read(bytes);
        if (read != count - 1) return false;

        for (byte b : bytes) {
            if (!checkUtf8Byte(b)) return false;
        }
        return true;
    }

    private static boolean checkUtf8Byte(byte b) {
        BitSet bitSet = convert2BitSet(b);
        return bitSet.get(0) && !bitSet.get(1); // 开头为 10xxxxxx
    }

    private static int getCountOfSequential(@NonNull BitSet bitSet) {
        int count = 0;
        for (int i = 0; i < BYTE_SIZE; i++) {
            if (bitSet.get(i)) {
                count++;
            } else break;
        }
        return count;
    }

    private static BitSet convert2BitSet(int code) {
        BitSet bitSet = new BitSet(BYTE_SIZE);
        for (int i = 0; i < BYTE_SIZE; i++) {
            if (((code >> (7 - i)) & 1) == 1) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    /**
     * 使用简单 hash 做缓存 key，可根据业务改为 MD5 等
     */
    private static String generateKey(byte[] bytes) {
        int hash = 1;
        for (byte b : bytes) {
            hash = 31 * hash + b;
        }
        return String.valueOf(hash);
    }
}
