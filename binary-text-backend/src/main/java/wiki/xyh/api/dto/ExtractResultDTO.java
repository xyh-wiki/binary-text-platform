package wiki.xyh.api.dto;

/**
 * Author:XYH
 * Date:2025-11-15
 * Description: 二进制文件文本提取接口返回结果 DTO，封装单个文件的基本信息和提取结果。
 */
public class ExtractResultDTO {

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节数）
     */
    private long fileSize;

    /**
     * 识别出的文件类型，如 PDF/WORD/TXT 等
     */
    private String fileType;

    /**
     * 提取出的纯文本内容
     */
    private String content;

    /**
     * 错误信息，如果解析失败则写明失败原因；成功时可以为 null
     */
    private String errorMsg;

    /**
     * 获取原始文件名。
     *
     * @return 文件名字符串
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置原始文件名。
     *
     * @param fileName 文件名字符串
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取文件大小（单位：字节）。
     *
     * @return 文件大小数值
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * 设置文件大小（单位：字节）。
     *
     * @param fileSize 文件大小数值
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取识别出的文件类型。
     *
     * @return 文件类型字符串
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * 设置识别出的文件类型。
     *
     * @param fileType 文件类型字符串
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * 获取提取出的纯文本内容。
     *
     * @return 文本内容字符串
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置提取出的纯文本内容。
     *
     * @param content 文本内容字符串
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取错误信息。
     *
     * @return 错误信息字符串
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * 设置错误信息。
     *
     * @param errorMsg 错误信息字符串
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
