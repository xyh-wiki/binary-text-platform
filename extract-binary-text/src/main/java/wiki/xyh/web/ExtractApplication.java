package wiki.xyh.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author:XYH
 * Date:2025-11-09
 * Description: 项目 Web 启动入口，基于 Spring Boot 提供文件提取接口和前端页面
 */
@SpringBootApplication(scanBasePackages = "wiki.xyh")
public class ExtractApplication {

    /**
     * 程序主入口，启动 Spring Boot 内嵌容器
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ExtractApplication.class, args);
    }
}
