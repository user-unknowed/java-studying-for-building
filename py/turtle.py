import turtle
# 创建一个画笔对象
t = turtle.Turtle()
# 设置画笔速度
t.speed(5)
# 设置画笔颜色
t.pencolor("yellow")
# 循环画五角星的五条边
for i in range(5):
    t.forward(200)      # 向前移动200像素
    t.right(144)        # 向右旋转144度

# 点击窗口时关闭
turtle.done()
