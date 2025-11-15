/**
 * @Author: XYH
 * @Date: 2025-07-07
 * @Description: 多线程批量压测 GetTypeAndContent，增强功能版本
 * - 支持文件类型分组统计
 * - 支持异常分类统计
 * - 支持每轮主动 GC + 堆内存快照（可选）
 * - 支持超时控制
 */
package wiki.xyh.test;

import wiki.xyh.bean.TypeAndContent;
import wiki.xyh.utils.GetTypeAndContent;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GetTypeAndContentTestPlus {

    private static final int THREAD_COUNT = 8;
    private static final int MAX_ROUND = 1000;
    private static final int LOG_INTERVAL = 100;
    private static final int TIMEOUT_SECONDS = 30;

    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    private static final Map<String, AtomicInteger> typeCounter = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> errorCounter = new ConcurrentHashMap<>();
    private static final List<String> failedFiles = Collections.synchronizedList(new ArrayList<>());

    private static final String[] SUPPORTED_SUFFIX = {
            "txt", "doc", "docx", "xls", "xlsx", "pdf", "rtf", "html", "ofd"
    };

    public static void main(String[] args) throws Exception {
        String folderPath = "E:\\02-code\\extract-binary-text\\src\\main\\resources\\binary-doc"; // 替换为本地路径
        List<File> testFiles = new ArrayList<>();
        collectFiles(new File(folderPath), testFiles);

        if (testFiles.isEmpty()) {
            System.err.println("❌ 未找到任何可解析文件");
            return;
        }

        AtomicInteger totalCount = new AtomicInteger();
        long start = System.currentTimeMillis();

        for (int round = 1; round <= MAX_ROUND; round++) {
            System.out.printf("\n▶▶ 第 %d 轮开始，文件数：%d\n", round, testFiles.size());

            CountDownLatch latch = new CountDownLatch(testFiles.size());
            for (File file : testFiles) {
                executor.submit(() -> {
                    Future<?> future = null;
                    try {
                        Callable<Void> task = () -> {
                            String suffix = getSuffix(file.getName());
                            byte[] data = Files.readAllBytes(file.toPath());
                            TypeAndContent result = GetTypeAndContent.getFileTypeAndContent(data);

                            totalCount.incrementAndGet();
                            typeCounter.computeIfAbsent(result.getType(), k -> new AtomicInteger(0)).incrementAndGet();
                            return null;
                        };
                        future = Executors.newSingleThreadExecutor().submit(task);
                        future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        errorCounter.computeIfAbsent("超时", k -> new AtomicInteger(0)).incrementAndGet();
                        failedFiles.add(file.getName() + "（超时）");
                    } catch (Exception e) {
                        errorCounter.computeIfAbsent("异常", k -> new AtomicInteger(0)).incrementAndGet();
                        failedFiles.add(file.getName());
                    } finally {
                        latch.countDown();
                        if (totalCount.get() % LOG_INTERVAL == 0) {
                            logMemory(totalCount.get());
                        }
                        if (future != null) future.cancel(true);
                    }
                });
            }
            latch.await();
            System.gc();
            Thread.sleep(1000);
            System.out.printf("✅ 第 %d 轮完成，当前已处理：%d\n", round, totalCount.get());
        }

        long duration = System.currentTimeMillis() - start;
        System.out.printf("\n🎯 总耗时 %.2f 秒，共处理 %d 个文件\n", duration / 1000.0, totalCount.get());

        System.out.println("\n📊 类型统计:");
        typeCounter.forEach((type, count) ->
                System.out.printf("  %s: %d\n", type, count.get()));

        System.out.println("\n❌ 异常统计:");
        errorCounter.forEach((err, count) ->
                System.out.printf("  %s: %d\n", err, count.get()));

        if (!failedFiles.isEmpty()) {
            System.out.println("\n🚫 失败文件:");
            failedFiles.forEach(f -> System.out.println("  " + f));
        }

        executor.shutdown();
    }

    private static void collectFiles(File folder, List<File> list) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                collectFiles(file, list);
            } else {
                for (String suffix : SUPPORTED_SUFFIX) {
                    if (file.getName().toLowerCase().endsWith("." + suffix)) {
                        list.add(file);
                        break;
                    }
                }
            }
        }
    }

    private static void logMemory(int count) {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = bean.getHeapMemoryUsage();
        long usedMB = heap.getUsed() / 1024 / 1024;
        long maxMB = heap.getMax() / 1024 / 1024;
        System.out.printf("📦 已处理：%d，内存：%d MB / %d MB\n", count, usedMB, maxMB);
    }

    private static String getSuffix(String name) {
        int idx = name.lastIndexOf(".");
        return (idx > 0) ? name.substring(idx + 1).toLowerCase() : "unknown";
    }
}
