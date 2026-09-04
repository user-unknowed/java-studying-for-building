import javax.swing.JOptionPane;//窗口化管理的包，用于显示对话框

public class CnyToHkConverter//必须和文件名一致，否则不能执行该命令
 {
    public static void main(String[] args) {
        String rateStr = JOptionPane.showInputDialog(null, "请输入人民币兑港币汇率（如0.93）：", "汇率输入", JOptionPane.QUESTION_MESSAGE);
        //JOptionPane.是前提条件，保证窗口化能够正常运行,QUESTION_MESSAGE是提示框的样式，用于输入数字
        if (rateStr == null) return;
        //0返回条件，保证不输入的时候会再原地等待
        String cnyStr = JOptionPane.showInputDialog(null, "请输入需兑换的人民币数（元）：", "人民币输入", JOptionPane.QUESTION_MESSAGE);
        if (cnyStr == null) return;
        //0返回条件，保证不输入的时候会再原地等待
        try {
            double rate = Double.parseDouble(rateStr.trim());
            double cny = Double.parseDouble(cnyStr.trim());
            double hk = cny * rate;
            String result = String.format("%.2f 元人民币 = %.2f 港币", cny, hk);
            JOptionPane.showMessageDialog(null, result, "兑换结果", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "输入格式有误，请输入有效的数字。", "错误", JOptionPane.ERROR_MESSAGE);
            //异常处理，保证不会输入非数字字符
            }
    }
}