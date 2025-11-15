package wiki.xyh.utils;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;

public class PageParser {


    public static HtmlPage htmlParser(String source){
        WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
        HtmlPage htmlpage = null;
        try {
            StringWebResponse swr = new StringWebResponse(source, new URL("http://www.baidu.com/"));
            DefaultPageCreator dpc = new DefaultPageCreator();
            htmlpage = (HtmlPage)dpc.createPage(swr, client.getCurrentWindow());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlpage;
    }

    public static HtmlPage htmlParser(byte[] source, String charset) {
        HtmlPage htmlpage = null;

        try (final WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            client.getOptions().setThrowExceptionOnScriptError(false);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.getOptions().setActiveXNative(false);
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setRedirectEnabled(false);

            String htmlString = new String(source, charset);
            StringWebResponse swr = new StringWebResponse(htmlString, new URL("http://localhost/"));
            DefaultPageCreator dpc = new DefaultPageCreator();
            htmlpage = (HtmlPage) dpc.createPage(swr, client.getCurrentWindow());

        } catch (IOException e) {
            System.out.println("html解析异常 >> " + e.getMessage());
        }

        return htmlpage;
    }


    public static HtmlPage htmlParser(String source, String url){
        WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
        HtmlPage htmlpage = null;
        try {
            StringWebResponse swr = new StringWebResponse(source, new URL(url));
            DefaultPageCreator dpc = new DefaultPageCreator();
            htmlpage = (HtmlPage)dpc.createPage(swr, client.getCurrentWindow());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlpage;
    }

    public static HtmlPage htmlParser(WebClient client, String source, String url){
        HtmlPage htmlpage = null;
        try {
            StringWebResponse swr = new StringWebResponse(source, new URL(url));
            DefaultPageCreator dpc = new DefaultPageCreator();
            htmlpage = (HtmlPage)dpc.createPage(swr, client.getCurrentWindow());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlpage;
    }
    public static HtmlPage htmlParser(Page page, String source, String url){
        HtmlPage htmlpage = null;
        try {
            StringWebResponse swr = new StringWebResponse(source, new URL(url));
            DefaultPageCreator dpc = new DefaultPageCreator();
            htmlpage = (HtmlPage)dpc.createPage(swr, page.getEnclosingWindow());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlpage;
    }
}
