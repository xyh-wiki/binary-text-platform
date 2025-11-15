package wiki.xyh.web.service;

import org.springframework.stereotype.Service;
import wiki.xyh.web.bean.ExtractHistoryRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Author:XYH
 * Date:2025-11-09
 * Description: 文件提取历史记录服务，当前基于内存 List 存储，可后续替换为数据库实现
 */
@Service
public class ExtractHistoryService {

    /**
     * 自增 ID 生成器
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 内存中的历史记录列表
     */
    private final List<ExtractHistoryRecord> historyList =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * 保存一条历史记录
     *
     * @param record 历史记录实体，不需要设置 id 和 createTime
     * @return 填充 id 和 createTime 后的完整对象
     */
    public ExtractHistoryRecord save(ExtractHistoryRecord record) {
        record.setId(idGenerator.getAndIncrement());
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        historyList.add(record);
        return record;
    }

    /**
     * 根据 ID 查询详情
     *
     * @param id 主键 ID
     * @return 对应记录，不存在时返回 null
     */
    public ExtractHistoryRecord findById(Long id) {
        synchronized (historyList) {
            return historyList.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 分页查询历史记录
     *
     * @param pageNum      页码，从 1 开始
     * @param pageSize     每页大小
     * @param fileNameLike 文件名模糊匹配
     * @param fileType     文件类型精确匹配
     * @param success      成功状态过滤，null 表示不过滤
     * @param startDate    开始日期（仅比较日期）
     * @param endDate      结束日期
     * @return 当前页记录列表
     */
    public List<ExtractHistoryRecord> pageQuery(int pageNum,
                                                int pageSize,
                                                String fileNameLike,
                                                String fileType,
                                                Boolean success,
                                                LocalDate startDate,
                                                LocalDate endDate) {

        List<ExtractHistoryRecord> filtered;
        synchronized (historyList) {
            filtered = historyList.stream()
                    .filter(r -> {
                        if (fileNameLike != null && !fileNameLike.isEmpty()) {
                            if (r.getFileName() == null
                                    || !r.getFileName().toLowerCase().contains(fileNameLike.toLowerCase())) {
                                return false;
                            }
                        }
                        if (fileType != null && !fileType.isEmpty()) {
                            if (r.getFileType() == null
                                    || !fileType.equalsIgnoreCase(r.getFileType())) {
                                return false;
                            }
                        }
                        if (success != null) {
                            if (r.isSuccess() != success) {
                                return false;
                            }
                        }
                        if (startDate != null) {
                            if (r.getCreateTime() == null
                                    || r.getCreateTime().toLocalDate().isBefore(startDate)) {
                                return false;
                            }
                        }
                        if (endDate != null) {
                            if (r.getCreateTime() == null
                                    || r.getCreateTime().toLocalDate().isAfter(endDate)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    // 按时间倒序
                    .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                    .collect(Collectors.toList());
        }

        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= filtered.size()) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        return filtered.subList(fromIndex, toIndex);
    }

    /**
     * 统计符合条件的总记录数
     *
     * @param fileNameLike 文件名模糊
     * @param fileType     类型
     * @param success      成功状态
     * @param startDate    起始日期
     * @param endDate      结束日期
     * @return 记录总数
     */
    public long count(String fileNameLike,
                      String fileType,
                      Boolean success,
                      LocalDate startDate,
                      LocalDate endDate) {
        synchronized (historyList) {
            return historyList.stream()
                    .filter(r -> {
                        if (fileNameLike != null && !fileNameLike.isEmpty()) {
                            if (r.getFileName() == null
                                    || !r.getFileName().toLowerCase().contains(fileNameLike.toLowerCase())) {
                                return false;
                            }
                        }
                        if (fileType != null && !fileType.isEmpty()) {
                            if (r.getFileType() == null
                                    || !fileType.equalsIgnoreCase(r.getFileType())) {
                                return false;
                            }
                        }
                        if (success != null) {
                            if (r.isSuccess() != success) {
                                return false;
                            }
                        }
                        if (startDate != null) {
                            if (r.getCreateTime() == null
                                    || r.getCreateTime().toLocalDate().isBefore(startDate)) {
                                return false;
                            }
                        }
                        if (endDate != null) {
                            if (r.getCreateTime() == null
                                    || r.getCreateTime().toLocalDate().isAfter(endDate)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .count();
        }
    }

    /**
     * 统计结果对象，用于首页统计展示
     */
    public static class Stats {

        /**
         * 今日提取次数
         */
        private int todayCount;

        /**
         * 今日失败次数
         */
        private int todayFailed;

        /**
         * 历史总文档数
         */
        private int total;

        public int getTodayCount() {
            return todayCount;
        }

        public void setTodayCount(int todayCount) {
            this.todayCount = todayCount;
        }

        public int getTodayFailed() {
            return todayFailed;
        }

        public void setTodayFailed(int todayFailed) {
            this.todayFailed = todayFailed;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }

    /**
     * 计算统计信息：今日次数、今日失败次数、历史总数
     *
     * @return 统计结果对象
     */
    public Stats getStats() {
        Stats stats = new Stats();
        LocalDate today = LocalDate.now();
        synchronized (historyList) {
            stats.setTotal(historyList.size());
            long todayCount = historyList.stream()
                    .filter(r -> r.getCreateTime() != null
                            && today.equals(r.getCreateTime().toLocalDate()))
                    .count();
            long todayFailed = historyList.stream()
                    .filter(r -> r.getCreateTime() != null
                            && today.equals(r.getCreateTime().toLocalDate())
                            && !r.isSuccess())
                    .count();
            stats.setTodayCount((int) todayCount);
            stats.setTodayFailed((int) todayFailed);
        }
        return stats;
    }
}
