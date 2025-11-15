package wiki.xyh.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: TypeAndContent
 * @Time: 2025/5/26 15:34
 * @Author: XYH
 * @Description: TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeAndContent {
    private String type; // 文件类型
    private String content; // 文件内容
}
