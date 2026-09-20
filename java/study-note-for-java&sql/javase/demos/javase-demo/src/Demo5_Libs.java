import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Demo5：常用外部函数库实战 —— 全部真实编译运行。
 *
 * 依赖（lib/ 目录）：
 *   jackson-databind / jackson-core / jackson-annotations —— JSON 序列化
 *   slf4j-api / slf4j-simple                             —— 日志门面 + 简单实现
 *   commons-lang3                                         —— 工具箱
 *
 * 编译：javac -encoding UTF-8 -cp "lib/*" -d out src/Demo5_Libs.java
 * 运行：java -Dfile.encoding=UTF-8 -cp "out:lib/*" Demo5_Libs
 */
public class Demo5_Libs {

    private static final Logger log = LoggerFactory.getLogger(Demo5_Libs.class);

    /** 被序列化的数据模型（record + Jackson 是 2020 年代的标准组合）。 */
    record Device(String name, double temp, boolean online) {
    }

    public static void main(String[] args) throws Exception {
        section("[1] Jackson：Java 对象 ⇄ JSON（事实标准）");
        var mapper = new ObjectMapper();
        var d = new Device("温室传感器-A", 23.9, true);

        String json = mapper.writeValueAsString(d);
        System.out.println("对象→JSON: " + json);

        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(d);
        System.out.println("美化输出:\n" + pretty);

        Device back = mapper.readValue(pretty, Device.class);
        System.out.println("JSON→对象: " + back);
        System.out.println("往返一致: " + d.equals(back));

        List<Device> list = List.of(d, new Device("传感器-B", 21.5, false));
        String arr = mapper.writeValueAsString(list);
        System.out.println("List→JSON: " + arr);
        List<Device> back2 = mapper.readValue(arr, new TypeReference<List<Device>>() {
        });
        System.out.println("JSON→List: 大小=" + back2.size() + ", 第二项=" + back2.get(1));

        Map<String, Object> m = mapper.readValue(
                "{\"id\":1,\"tags\":[\"a\",\"b\"]}", new TypeReference<Map<String, Object>>() {
                });
        System.out.println("JSON→Map: " + m);

        section("[2] SLF4J：日志门面（换实现不改代码）");
        log.info("服务启动完成, 端口={}", 8080);                    // {} 占位符，不用字符串拼接
        log.warn("库存不足: productId={}, 剩余={}", 7, 0);
        log.error("模拟异常日志", new RuntimeException("连接超时(演示用)"));
        System.out.println("（上方日志由 slf4j-simple 输出：时间 级别 类名 - 消息）");

        section("[3] Apache Commons Lang3：补 JDK 的短板");
        System.out.println("isBlank(\" \")        = " + StringUtils.isBlank(" "));
        System.out.println("leftPad(\"7\",2,'0')  = " + StringUtils.leftPad("7", 2, '0'));
        System.out.println("abbreviate(...,8)  = \"" + StringUtils.abbreviate("Java上位机开发实战教程", 8) + "\"");
        System.out.println("toInt(null,-1)     = " + NumberUtils.toInt(null, -1));
        System.out.println("随机设备ID(8位)    = " + RandomStringUtils.randomAlphanumeric(8));

        System.out.println();
        System.out.println("✓ Demo5 完成：3 个代表性外部库（JSON/日志/工具箱）实测通过");
    }

    static void section(String title) {
        System.out.println();
        System.out.println("──────────────────────────────────────────────");
        System.out.println("▶ " + title);
        System.out.println("──────────────────────────────────────────────");
    }
}