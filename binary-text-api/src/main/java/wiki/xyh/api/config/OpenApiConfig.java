package wiki.xyh.api.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author:XYH
 * Date:2025-11-15
 * Description: OpenAPI( Swagger ) 配置类，便于通过网页查看和调试接口。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 创建 OpenAPI Bean，配置基础信息。
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Binary Text Extract API")
                        .version("1.0.0")
                        .description("二进制文件纯文本提取接口，封装 GetTypeAndContent 的 HTTP 调用能力。"));
    }
}
