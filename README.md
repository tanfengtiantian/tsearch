倒排搜索引擎

启动 example2
http://localhost:8888/_search?index=xinwen&q=百度中国

http://localhost:8888/_create?index=xinwen
{
    "indexmapping":[
        {
            "fieldname":"content",  //字段名称
            "fieldtype":2			 //字段类型
        }
   ]
}

fst 修正版