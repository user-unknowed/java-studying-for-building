import org.springframework.context.annotation.Configuration;//导入@Configuration注解，用于将cout类标记为Spring组件，factory方法
package com.example.test;//包名，用于组织类，可以通过包名来做文章，如想放置在Windows窗口内，需要在命令行中输入javac cout.java
import java.util.Scanner;//导入Scanner类，用于获取用户输入的整数，返回值为int类型，factory方法
/*
*main 方法，程序入口(惯用伎俩)
*cout 方法，用于获取用户输入的整数，返回值为int类型，factory方法
*cout 方法的参数为int类型，用于存储用户输入的整数
*cout 方法的返回值为int类型，用于存储用户输入的整数，用于返回给main方法
*/
@Override//重写cout方法，用于获取用户输入的整数，返回值为int类型，factory方法
class main {
//依旧程序入口，args为命令行参数，用于存储用户输入的命令行参数，如想放置在Windows窗口内，需要在命令行中输入java cout.java
  public static void main(String[] args)  {
    System.out.println("Hello, World!");
    //打印Hello, World!在控制台；如果需要打印在屏幕上，则需要加上System.out.print()
    cout c = new cout();
    //创建cout类的对象c，用于调用cout方法
    int a = c.cout(0);
    //调用cout方法，获取用户输入的整数，返回值为int类型，用于存储用户输入的整数
    //cout方法的参数为int类型，用于存储用户输入的整数
    //cout方法的返回值为int类型，用于存储用户输入的整数，用于返回给main方法
    System.out.println(a);
    //打印用户输入的整数在控制台；如果需要打印在屏幕上，则需要加上System.out.print()
  }
}
//cout类，用于获取用户输入的整数，返回值为int类型，factory方法
@Factory//factory方法，用于创建cout类的对象,也可以写成“@Component @Service @Controller @Repository @ComponentScan @Configuration @EnableAutoConfiguration @SpringBootApplication”
public class cout{
    public int cout(int a){
        System.out.println("print a:\n");
        //打印print a:\n在控制台；如果需要打印在屏幕上，则需要加上System.out.print()
        Scanner sc = new Scanner(System.in);
        //创建Scanner类的对象sc，用于获取用户输入的整数，返回值为int类型，用于存储用户输入的整数
        a = sc.nextInt();
        //获取用户输入的整数，返回值为int类型，用于存储用户输入的整数
        return a;
        //返回用户输入的整数，用于返回给main方法
    }
    public void print(int a){
        System.out.println(a);
    }
}