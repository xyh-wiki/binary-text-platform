package wiki.xyh.web.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wiki.xyh.bean.TypeAndContent;
import wiki.xyh.utils.GetTypeAndContent;
import wiki.xyh.web.bean.ExtractHistoryRecord;
import wiki.xyh.web.dto.ExtractResultDto;
import wiki.xyh.web.service.ExtractHistoryService;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:XYH
 * Date:2025-11-09
 * Description: 文件提取接口控制器，提供上传提取、历史查询、统计等 REST 接口
 */
@RestController
@RequestMapping("/api")
public class ExtractController {

    /**
     * 历史记录服务
     */
    @Resource
    private ExtractHistoryService historyService;

    /**
     * 上传文件并执行文本提取
     *
     * @param file   上传文件（二进制内容）
     * @param mode   提取模式：TYPE_AND_CONTENT / TYPE_ONLY / CONTENT_ONLY
     * @param remark 备注信息
     * @return 提取结果 DTO
     * @throws IOException 文件读取异常
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ExtractResultDto extract(@RequestPart("file") MultipartFile file,
                                    @RequestParam(value = "mode", required = false,
                                            defaultValue = "TYPE_AND_CONTENT") String mode,
                                    @RequestParam(value = "remark", required = false) String remark)
            throws IOException {

        ExtractResultDto resultDto = new ExtractResultDto();
        resultDto.setFileName(file.getOriginalFilename());
        resultDto.setFileSize(file.getSize());
        resultDto.setRemark(remark);

        ExtractHistoryRecord historyRecord = new ExtractHistoryRecord();
        historyRecord.setFileName(file.getOriginalFilename());
        historyRecord.setFileSize(file.getSize());
        historyRecord.setRemark(remark);

        try {
            // 1. 读取文件字节数组
            byte[] bytes = file.getBytes();

            // 2. 调用原有工具类，不修改其内部实现
            TypeAndContent typeAndContent = GetTypeAndContent.getFileTypeAndContent(bytes);

            // 3. 从 TypeAndContent 中获取类型和内容
            String fileType = typeAndContent.getType();
            String content = typeAndContent.getContent();

            // 4. 如果类型为 UNKNOWN，则视为提取失败（即使内部返回了 content 也不认为是成功）
            boolean isUnknown = (fileType == null)
                    || "UNKNOWN".equalsIgnoreCase(fileType.trim());

            if (isUnknown) {
                // 统一标记为失败
                String errMsg = "未识别的文件类型（UNKNOWN），无法提取有效纯文本";

                resultDto.setSuccess(false);
                resultDto.setFileType(fileType);   // 前端仍然可以看到是 UNKNOWN
                resultDto.setContent(null);        // 不返回文本
                resultDto.setErrorMessage(errMsg);

                historyRecord.setSuccess(false);
                historyRecord.setFileType(fileType);
                historyRecord.setContent(null);
                historyRecord.setErrorMessage(errMsg);

            } else {
                // 5. 类型可识别的正常分支，再根据模式处理内容

                if ("TYPE_ONLY".equalsIgnoreCase(mode)) {
                    // 仅识别类型，不返回内容
                    content = null;
                } else if ("CONTENT_ONLY".equalsIgnoreCase(mode)) {
                    // 仅返回内容，类型仍然保留
                    // 不需要特别处理
                }
                // 其他情况默认 TYPE_AND_CONTENT：类型 + 内容 都返回

                // 成功结果
                resultDto.setSuccess(true);
                resultDto.setFileType(fileType);
                resultDto.setContent(content);

                historyRecord.setSuccess(true);
                historyRecord.setFileType(fileType);
                historyRecord.setContent(content);
            }

        } catch (Exception e) {
            // 异常情况下返回失败信息，并记录历史
            resultDto.setSuccess(false);
            resultDto.setErrorMessage(e.getMessage());

            historyRecord.setSuccess(false);
            historyRecord.setErrorMessage(e.getMessage());
        }

        // 6. 保存历史记录，并把 ID 回填到结果中
        historyRecord = historyService.save(historyRecord);
        resultDto.setHistoryId(historyRecord.getId());

        return resultDto;
    }

    /**
     * 历史记录分页查询接口
     *
     * @param pageNum      页码，从 1 开始
     * @param pageSize     每页大小
     * @param fileNameLike 文件名模糊匹配
     * @param fileType     文件类型精确匹配
     * @param success      成功状态（true/false），为空表示不过滤
     * @param startDate    开始日期，格式：yyyy-MM-dd
     * @param endDate      结束日期，格式：yyyy-MM-dd
     * @return 包含 records / total 的 Map，方便前端直接使用
     */
    @GetMapping("/history")
    public Map<String, Object> history(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                       @RequestParam(value = "fileNameLike", required = false) String fileNameLike,
                                       @RequestParam(value = "fileType", required = false) String fileType,
                                       @RequestParam(value = "success", required = false) Boolean success,
                                       @RequestParam(value = "startDate", required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                       @RequestParam(value = "endDate", required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        List<ExtractHistoryRecord> records =
                historyService.pageQuery(pageNum, pageSize, fileNameLike, fileType, success, startDate, endDate);
        long total = historyService.count(fileNameLike, fileType, success, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }

    /**
     * 根据 ID 获取单条历史记录详情
     *
     * @param id 历史记录主键 ID
     * @return 对应的历史记录对象
     */
    @GetMapping("/history/{id}")
    public ExtractHistoryRecord historyDetail(@PathVariable("id") Long id) {
        return historyService.findById(id);
    }

    /**
     * 统计接口，提供今日提取次数、今日失败次数、历史总文档数
     *
     * @return 统计信息对象
     */
    @GetMapping("/history/stats")
    public ExtractHistoryService.Stats stats() {
        return historyService.getStats();
    }
}
