import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Demo6：网络编程基础 —— Socket = 两台机器间的"双向字节管道"。
 *
 * 本 Demo 在同一进程内：后台线程起"服务端"，主线程当"客户端"，
 * 真实走完 TCP 连接 → 发送 → 应答 → 关闭 全流程。
 *
 * 运行：java -Dfile.encoding=UTF-8 -cp out Demo6_Network
 */
public class Demo6_Network {

    public static void main(String[] args) throws Exception {
        System.out.println("▶ TCP 回显服务端启动（端口由系统自动分配）……");

        var server = new ServerSocket(0);          // 0 = 让系统挑一个空闲端口
        int port = server.getLocalPort();
        System.out.println("  监听端口: " + port);
        System.out.println("  注意：真实上位机程序里，这个端口通常对应\"下位机/设备服务端\"的角色");

        Thread serverThread = new Thread(() -> {
            try (var s = server.accept();
                 var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 var out = new PrintWriter(s.getOutputStream(), true)) {
                System.out.println("[server] 收到客户端连接: " + s.getRemoteSocketAddress());
                String line;
                while ((line = in.readLine()) != null) {
                    String resp = "ECHO: " + line.toUpperCase();
                    out.println(resp);
                    System.out.println("[server] " + line + "  →  " + resp);
                    if (line.equalsIgnoreCase("bye")) {
                        break;
                    }
                }
                System.out.println("[server] 连接关闭");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // ---- 客户端：连接 → 发三条消息 → 收三条应答 ----
        try (var s = new Socket("127.0.0.1", port);
             var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             var out = new PrintWriter(s.getOutputStream(), true)) {
            for (String msg : new String[]{"hello", "modbus over tcp", "bye"}) {
                out.println(msg);                  // 发送
                String resp = in.readLine();       // 接收
                System.out.println("[client] 发送: " + msg + " | 收到: " + resp);
            }
        }
        serverThread.join();
        server.close();
        System.out.println();
        System.out.println("✓ Demo6 完成：TCP 全流程真实跑通（连接→发送→应答→关闭）");
        System.out.println("  上位机教程《06》里，这套 Socket 就是与设备通信的高速公路");
    }
}