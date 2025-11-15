package wiki.xyh.test;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HtmlContentExtractor {

    public static class HtmlSection {
        public String tag;
        public String text;

        public HtmlSection(String tag, String text) {
            this.tag = tag;
            this.text = text.trim();
        }

        @Override
        public String toString() {
            return "[" + tag + "] " + text;
        }
    }

    /**
     * 提取 HTML 内容并分段返回结构化内容
     */
    public static List<HtmlSection> extractSections(byte[] htmlBytes, String charset) {
        List<HtmlSection> result = new ArrayList<>();
        try {
            String html = new String(htmlBytes, charset);
            Document doc = Jsoup.parse(html);

            // 可扩展的结构性标签列表
            String[] tags = {"h1", "h2", "h3", "h4", "h5", "p", "li", "blockquote"};

            for (String tag : tags) {
                Elements elements = doc.select(tag);
                for (Element el : elements) {
                    String text = el.text();
                    if (!text.isEmpty()) {
                        result.add(new HtmlSection(tag, text));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String extractPlainText(byte[] htmlBytes, String charset) {
        StringJoiner joiner = new StringJoiner("\n"); // 两个换行符表示段落分隔

        try {
            String html = new String(htmlBytes, charset);
            Document doc = Jsoup.parse(html);

            // 提取这些结构化标签作为段落
            String[] tags = {"h1", "h2", "h3", "h4", "h5", "p", "li", "blockquote"};

            for (String tag : tags) {
                Elements elements = doc.select(tag);
                for (Element el : elements) {
                    String text = el.text().trim();
                    if (!text.isEmpty()) {
                        joiner.add(text);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return joiner.toString();
    }

    // 测试示例
    public static void main(String[] args) throws IOException {
        String html = FileUtils.readFileToString(new File("E:\\05-iCloud\\iCloudDrive\\01-法研院\\10-文书\\nwws\\4.html"), StandardCharsets.UTF_8);
        String sections = extractPlainText(html.getBytes(StandardCharsets.UTF_8), "UTF-8");
        System.out.println(sections);
    }
}
