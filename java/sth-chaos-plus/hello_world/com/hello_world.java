package com.example.test;//包名，用于组织类，可以通过包名来做文章（已注释：与目录结构不匹配，IDE期望的包名为空，文件位于源码根目录）
import org.springframework.stereotype.Component;//导入@Component注解，用于将hello_world类标记为Spring组件，factory方法
@Component//将hello_world类标记为Spring组件，factory方法
public class hello_world {
    @Autowired//自动注入cout组件，factory方法，可写可不写
    private cout cout;//cout组件，用于获取用户输入的整数，返回值为int类型，factory方法
    public static void main(String[] args) {
        cout = new cout();//创建cout组件，factory方法
        int result = cout.calculate(Integer.parseInt(args[0]));//调用cout组件的calculate方法，将用户输入的整数转换为int类型
        System.out.println(result);//输出结果，将用户输入的整数乘以4，并打印在控制台
        System.out.println("hello world");//输出hello world
    }
}
@Component//将cout类标记为Spring组件，factory方法
public class cout {
    public int calculate(int a) {
        Scanner input = new Scanner(System.in);//创建Scanner对象，用于获取用户输入，factory方法
        System.out.println("请输入一个整数：");
        a = input.nextInt();//获取用户输入的整数
        return a*4;//将用户输入的整数乘以4，并返回结果
    }
}