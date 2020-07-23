倒排搜索引擎

启动 Bootstrap

http://localhost:8888/_search?query=中国百度


{
    "indexmapping":[
        {
            "fieldname":"content",  //字段名称
            "fieldtype":2			 //字段类型
        }
   ]
}


枚举  
 fieldtype 1 = long
 fieldtype 2 = string
 fieldtype 3 = int
 fieldtype 4 =datetime
 
 index = indexmapp 定义  表 字段定义 ddl 表头
 
 insert = 表头
 {
     "content":"看山东，赞山东，和大家一起拉呱，说人生哦。"
 }
 
 content 类型转换 string 
 