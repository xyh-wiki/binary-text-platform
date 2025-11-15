package wiki.xyh.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFWatermarkRemover extends PDFTextStripper {

    private final StringBuilder extractedText = new StringBuilder();
    private List<String> currentLine = new ArrayList<>();
    private float previousY = -1;

    public PDFWatermarkRemover() throws IOException {
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        // 检查文本的旋转角度，跳过倾斜文本（假设为水印）
        float angle = text.getDir();
        if (angle != 0 && angle != 90 && angle != 180 && angle != 270) {
            return;
        }

        // 检查 Y 位置是否改变（新行的指示），保留换行
        if (previousY != -1 && Math.abs(text.getY() - previousY) > 5) {
            appendCurrentLine();
        }
        previousY = text.getY();

        // 追加文本到当前行
        currentLine.add(text.getUnicode());
    }

    // 将当前行添加到提取文本并清空当前行
    private void appendCurrentLine() {
        if (!currentLine.isEmpty()) {
            String line = String.join("", currentLine);  // 合并当前行的字符
            if (line.trim().length() > 1) {  // 过滤掉只有一个字符的行
                extractedText.append(line).append("\n");
            }
            currentLine.clear();
        }
    }

    public static String removeWatermarkBasedOnAngle(byte[] pdfBytes) throws Exception {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IOException("输入的 PDF 数据为空");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document == null) {
                throw new IOException("无法加载 PDF 文档，可能格式不正确");
            }

            if (document.getNumberOfPages() == 0) {
                throw new IOException("PDF 页数为 0，可能为空文档");
            }

            PDFWatermarkRemover remover = new PDFWatermarkRemover();
            remover.setStartPage(1);
            remover.setEndPage(document.getNumberOfPages());

            remover.getText(document);
            return remover.extractedText.toString().trim();

        } catch (IOException e) {
            // 将 PDFBox 抛出的错误转成清晰异常往上传
            throw new IOException("PDF 解析失败：" + e.getMessage(), e);
        } catch (Exception e) {
            // 兜底未知错误
            throw new Exception("removeWatermarkBasedOnAngle 提取异常：" + e.getMessage(), e);
        }
    }

}
