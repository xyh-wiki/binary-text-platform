package wiki.xyh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author:XYH
 * Date:2025-11-15
 * Description: 二进制文件纯文本提取后端 API 启动类，精简实现，仅负责暴露 HTTP 接口，
 *              具体解析逻辑全部委托给 extract-binary-text 模块中的工具类。
 */
@SpringBootApplication
public class BinaryTextApiApplication {

    /**
     * 应用入口方法，启动 Spring Boot 容器。
     *
     * @param args 启动参数数组
     */
    public static void main(String[] args) {
        SpringApplication.run(BinaryTextApiApplication.class, args);
    }
}
