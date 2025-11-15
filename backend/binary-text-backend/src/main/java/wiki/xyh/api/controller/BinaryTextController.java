package wiki.xyh.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wiki.xyh.api.dto.ExtractResultDTO;
import wiki.xyh.bean.TypeAndContent;
import wiki.xyh.utils.GetTypeAndContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:XYH
 * Date:2025-11-15
 * Description: 二进制文件文本提取控制器，对外暴露 HTTP 接口，
 *              接收前端上传的文件并调用 GetTypeAndContent 完成类型识别与文本提取。
 */
@RestController
@RequestMapping("/api/extract")
@CrossOrigin
public class BinaryTextController {

    /**
     * 单文件提取接口，接受一个文件并返回识别类型和提取内容。
     *
     * @param file 前端上传的文件对象
     * @return 包含文件名、大小、类型、内容、错误信息的响应实体
     */
    @PostMapping(
            value = "/single",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ExtractResultDTO> extractSingle(@RequestPart("file") MultipartFile file) {
        ExtractResultDTO result = handleFile(file);
        return ResponseEntity.ok(result);
    }

    /**
     * 多文件批量提取接口，前端一次性上传多个文件，在后端串行处理。
     * 实际并发控制可以交给前端，以减少服务端压力。
     *
     * @param files 前端上传的文件数组
     * @return 每个文件对应一个提取结果的列表
     */
    @PostMapping(
            value = "/batch",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ExtractResultDTO>> extractBatch(@RequestPart("files") MultipartFile[] files) {
        List<ExtractResultDTO> list = new ArrayList<ExtractResultDTO>();
        if (files != null) {
            for (MultipartFile file : files) {
                list.add(handleFile(file));
            }
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 实际处理单个文件的内部方法：
     * 1. 读取文件字节
     * 2. 调用 GetTypeAndContent.getFileTypeAndContent
     * 3. 构建统一的返回对象
     *
     * @param file 上传的单个文件
     * @return 统一封装后的提取结果
     */
    private ExtractResultDTO handleFile(MultipartFile file) {
        ExtractResultDTO dto = new ExtractResultDTO();
        dto.setFileName(file.getOriginalFilename());
        dto.setFileSize(file.getSize());

        try {
            byte[] bytes = file.getBytes();
            // 核心：完全复用你现在的 GetTypeAndContent 方法
            TypeAndContent typeAndContent = GetTypeAndContent.getFileTypeAndContent(bytes);
            if (typeAndContent != null) {
                dto.setFileType(typeAndContent.getType());
                dto.setContent(typeAndContent.getContent());
                dto.setErrorMsg(null);
            } else {
                dto.setFileType(null);
                dto.setContent(null);
                dto.setErrorMsg("解析结果为空");
            }
        } catch (Exception e) {
            dto.setFileType(null);
            dto.setContent(null);
            dto.setErrorMsg("解析异常: " + e.getMessage());
        }

        return dto;
    }
}
