dict = {1:"Monday",
        2:"Tuesday",
        3:"Wednesday",
        4:"Thursday",
        5:"Friday",
        6:"Saturday",
        7:"Sunday"}
#dict的格式是{键:值}，键是唯一的，值可以重复，存储方式是键-值对的形式，类似于链表？
n = int(input("请输入一个星期几：\n"))
print(dict[n])