package wiki.xyh.web.dto;

/**
 * Author:XYH
 * Date:2025-11-09
 * Description: 文件提取结果返回 DTO，用于上传接口返回前端展示
 */
public class ExtractResultDto {

    /**
     * 提取是否成功
     */
    private boolean success;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 识别出的文件类型
     */
    private String fileType;

    /**
     * 提取出的文本内容
     */
    private String content;

    /**
     * 错误信息（当 success=false 时有效）
     */
    private String errorMessage;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 对应的历史记录 ID，方便前端跳转查看详情
     */
    private Long historyId;

    // ========== Getter / Setter ==========

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
}
