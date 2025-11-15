package wiki.xyh.web.bean;

import java.time.LocalDateTime;

/**
 * Author:XYH
 * Date:2025-11-09
 * Description: 文件提取历史记录实体类，用于前端展示和历史查询
 */
public class ExtractHistoryRecord {

    /**
     * 主键ID，自增生成
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;

    /**
     * 识别出的文件类型，例如：PDF/WORD/EXCEL/OFD/TXT/UNKNOWN 等
     */
    private String fileType;

    /**
     * 提取出的纯文本内容（可选，可以根据实际需要控制是否返回完整内容）
     */
    private String content;

    /**
     * 提取是否成功
     */
    private boolean success;

    /**
     * 错误信息，当 success = false 时记录异常描述
     */
    private String errorMessage;

    /**
     * 备注，例如案件号、业务标识等
     */
    private String remark;

    /**
     * 创建时间（提取时间）
     */
    private LocalDateTime createTime;

    // ========== Getter / Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
