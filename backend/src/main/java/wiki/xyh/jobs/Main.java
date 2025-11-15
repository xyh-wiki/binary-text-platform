package wiki.xyh.jobs;


import org.apache.commons.io.FileUtils;
import wiki.xyh.bean.TypeAndContent;
import wiki.xyh.utils.GetTypeAndContent;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * @Author: XYH
 * @Date: 5/10/2024 10:56
 * @Description: TODO
 */
public class Main {
    public static void main(String[] args) throws Exception {

        LogManager.getLogManager().reset();
        Logger.getLogger("").setLevel(Level.OFF);
        String filePath = "/Users/xyh/Desktop/成功2.pdf";
        byte[] bytes = FileUtils.readFileToByteArray(new File(filePath));
        TypeAndContent fileTypeAndContent = GetTypeAndContent.getFileTypeAndContent(bytes);
        System.out.println(fileTypeAndContent.getType());
        System.out.println(fileTypeAndContent.getContent());
//        System.out.println(GetTypeAndContent.extractHtmlJsoup(bytes, "utf-8"));

//        EncodeUtils.getEncode(new BufferedInputStream(new FileInputStream()))

//        System.out.println(GetTypeAndContent.detectFileType(FileUtils.readFileToByteArray(new File(filePath))));


//        System.out.println(GetTypeAndContent.readHTML(FileUtils.readFileToByteArray(new File(filePath)), "gbk"));


//        System.out.println(GetTypeAndContent.getFileContent(bytes));
//        System.out.println(GetTypeAndContent.readPDF(bytes));
//        System.out.println(GetTypeAndContent.readWordDoc(bytes));
//        System.out.println(PDFWatermarkRemover.removeWatermarkBasedOnAngle(bytes));
    }
}
