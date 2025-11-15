package wiki.xyh.utils;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ofdrw.reader.ContentExtractor;
import org.ofdrw.reader.OFDReader;
import org.slf4j.LoggerFactory;
import wiki.xyh.bean.TypeAndContent;

import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @Author: XYH
 * @Date: 2023/4/18
 * @Description: 读取不同文件类型的纯文本内容
 */
public class GetTypeAndContent {

    public static TypeAndContent getFileTypeAndContent(byte[] bytes) {

        String encode = EncodeUtils.detectEncoding(bytes, true);

        FileTypeDetector.FileType fileType = FileTypeDetector.FileType.UNKNOWN;
        try {
            fileType = detectFileType(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String content = null;

        // 根据 fileType 判断解析方式
        switch (fileType) {
            case TXT:
                content = readTXT(bytes, encode);
                break;
            case RTF:
                content = readRTF(bytes, encode);
                break;
            case DOC:
            case XLS:
                content = tryReadWordDocOrExcel(bytes, encode);
                break;
            case DOCX:
            case XLSX:
                content = tryReadWordDocxOrExcel(bytes);
                break;
            case HTML:
                content = extractHtmlJsoup(bytes, encode);
                break;
            case PDF:
                content = readPDF(bytes);
                break;
            case OFD:
                content = extractTextFromOFD(bytes);
                break;
            default:
                // 对于未知类型，暂不处理
                //            default:
//                // UNKNOWN 类型时，逐步尝试多种解析方式，直到成功
                String[] tryOrder = {"TXT", "RTF", "DOC", "DOCX", "HTML", "PDF", "OFD"};
                for (String type : tryOrder) {
                    switch (type) {
                        case "TXT":
                            content = readTXT(bytes, encode);
                            break;
                        case "RTF":
                            content = readRTF(bytes, encode);
                            break;
                        case "DOC":
                            content = tryReadWordDocOrExcel(bytes, encode);
                            break;
                        case "DOCX":
                            content = tryReadWordDocxOrExcel(bytes);
                            break;
                        case "HTML":
                            content = extractHtmlJsoup(bytes, encode);
                            break;
                        case "PDF":
                            content = readPDF(bytes);
                            break;
                        case "OFD":
                            content = extractTextFromOFD(bytes);
                            break;
                    }
                    if (!WsTextUtils.isMess(content)) {
                        fileType = FileTypeDetector.FileType.valueOf(type);
                        break;
                    }
                }
                break;
        }

        if (content != null && !content.isEmpty()) {
            content = formatContent(content);  // 格式化
        }

        return new TypeAndContent(fileType.toString().toLowerCase(), content);
    }

    public static FileTypeDetector.FileType detectFileType(byte[] bytes) throws Exception {

        return FileTypeDetector.detectFileType(bytes);
    }


    private static String formatContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        return content;
        // 使用正则表达式替换多个连续的换行符为一个 \n
//        return content.replaceAll("(\r\n|\r|\n)+", "\\\\n");
    }

    // 检测文件类型，支持根据扩展名和文件头识别
    public static FileTypeDetector.FileType detectFileType(byte[] bytes, String nr) throws Exception {
        FileTypeDetector.FileType fileType = FileTypeDetector.detectFileType(bytes);

        if (fileType == FileTypeDetector.FileType.UNKNOWN && nr.contains(".")) {
            String[] parts = nr.split("\\.");
            if (parts.length > 1) {
                try {
                    return FileTypeDetector.FileType.valueOf(parts[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    return FileTypeDetector.FileType.UNKNOWN;
                }
            }
        }

        return fileType;
    }

    // 处理 Word 和 Excel 文件的混合读取逻辑
    private static String tryReadWordDocOrExcel(byte[] bytes, String encode) {
        String docContent = readWordDoc(bytes);
        return docContent != null ? docContent : readExcelXls(bytes, encode);
    }

    private static String tryReadWordDocxOrExcel(byte[] bytes) {
        String docxContent = readWordDocx(bytes);
        return docxContent != null ? docxContent : readExcelXlsx(bytes);
    }

    public static String readWordDocx(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            return extractor.getText().trim();
        } catch (Exception e) {
            return null;  // 返回 null 以进行 Excel 文件处理
        }
    }

    public static String readWordDoc(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {

            return extractor.getText().trim();
        } catch (Exception e) {
            return null;  // 返回 null 以进行 Excel 文件处理
        }
    }

    public static String readTXT(byte[] bytes, String charset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            return IOUtils.toString(reader).trim();
        } catch (IOException e) {
            // 捕获异常并返回 null
            return null;
        }
    }

    public static String readPDF(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            return PDFWatermarkRemover.removeWatermarkBasedOnAngle(bytes);
        } catch (Exception ignored) {
        }
        return null;
    }

    // 使用PDFBox解析PDF文本
    private static String readPDFWithPDFBox(byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(50);  // 限制到前50页

            // 提取文本内容
            String rawText = stripper.getText(document);

            // 去除只有一个字符的行并保留其余行
            String filteredText = Arrays.stream(rawText.split("\n"))
                    .filter(line -> line.trim().length() > 1)  // 去除仅包含一个字符的行
                    .collect(Collectors.joining("\n"));  // 重新合并为字符串，保留换行符

            return filteredText.trim();  // 返回处理后的文本

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 使用iText解析PDF文本
    public static String readPDFWithIText(byte[] pdfBytes) throws Exception {
        StringBuilder sb = new StringBuilder();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes);
             PdfReader reader = new PdfReader(bis);
             PdfDocument pdfDoc = new PdfDocument(reader)) {

            int numPages = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= numPages; i++) {
                String pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                sb.append(pageText).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public static String readRTF(byte[] bytes, String charset) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {

            RTFEditorKit rtfParser = new RTFEditorKit();
            javax.swing.text.Document document = rtfParser.createDefaultDocument();
            rtfParser.read(reader, document, 0);
            return document.getText(0, document.getLength()).trim();
        } catch (Exception e) {
            // 捕获异常并返回 null
            return null;
        }
    }

    public static String readHTML(byte[] bytes, String charset) {
        try {
            HtmlPage page = PageParser.htmlParser(bytes, charset);
            return page.asNormalizedText().trim();
        } catch (Exception e) {
            // 捕获异常并返回 null
            return null;
        }
    }


    /**
     * 仅提取页面上可见的文本内容（自动过滤脚本、样式、隐藏元素等）
     */
    public static String extractHtmlJsoup(byte[] htmlBytes, String charset) {
        StringJoiner joiner = new StringJoiner("\n");

        try {
            // 1️⃣ 文件大小限制，防止大文件拖垮内存
            if (htmlBytes.length > 50 * 1024 * 1024) {
                throw new IllegalArgumentException("HTML 文件过大，超过 50MB，禁止解析");
            }

            // 2️⃣ 字符编码转换
            String html = new String(htmlBytes, charset);

            // 3️⃣ 预清理 - 删除无关标签（script / style / title / noscript）
            html = html.replaceAll("(?is)<script.*?>.*?</script>", "")
                    .replaceAll("(?is)<style.*?>.*?</style>", "")
                    .replaceAll("(?is)<title.*?>.*?</title>", "")
                    .replaceAll("(?is)<noscript.*?>.*?</noscript>", "");

            // 4️⃣ 使用 Jsoup 解析
            Document doc = Jsoup.parse(html);

            // 删除不可见元素（隐藏标签）
            // 常见隐藏标签及属性包括：display:none、visibility:hidden、hidden 属性
            doc.select("[style*=display:none], [style*=visibility:hidden], [hidden]").remove();

            // 5️⃣ 获取 body 中的可见文本
            Element body = doc.body();
            if (body != null) {
                // Jsoup 会自动拼接 body 中所有可见文字
                String visibleText = body.text().trim();

                if (!visibleText.isEmpty()) {
                    joiner.add(visibleText);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ HTML 提取失败: " + e.getMessage());
        }

        return joiner.toString();
    }


    public static String readExcelXlsx(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            StringBuilder sb = new StringBuilder();
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(cell.toString().trim()).append("\t");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            // 捕获异常并返回 null
            return null;
        }
    }

    public static String readExcelXls(byte[] bytes, String charset) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {

            StringBuilder sb = new StringBuilder();
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(cell.toString().trim()).append("\t");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            // 捕获异常并返回 null
            return null;
        }
    }


    public static String extractTextFromOFD(byte[] ofdBytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(ofdBytes);
             OFDReader reader = new OFDReader(inputStream)) {

            ContentExtractor extractor = new ContentExtractor(reader);
            StringBuilder sb = new StringBuilder();
            for (String content : extractor.extractAll()) {
                sb.append(content).append("\n");  // 保留换行符
            }
            return sb.toString();
        } catch (Exception e) {
            // 捕获异常并返回 null
            return null;
        }
    }
}
