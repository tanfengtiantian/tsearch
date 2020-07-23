package com.tf.search.types;

import java.util.ArrayList;
import java.util.List;

public class DocumentIndexData {

    // 文档全文（必须是UTF-8格式），用于生成待索引的关键词
    public String Content;
    // 文档的关键词
    // 当Content不为空的时候，优先从Content中分词得到关键词。
    // Tokens存在的意义在于绕过悟空内置的分词器，在引擎外部
    // 进行分词和预处理。
    public List<Tokens> TokenData = new ArrayList<>();

    // 文档标签（必须是UTF-8格式），比如文档的类别属性等，这些标签并不出现在文档文本中
    public List<String> Labels = new ArrayList<>();
    // 文档的评分字段，可以接纳任何类型的结构体
    public Object Fields;

    public DocumentIndexData(String content) {
        this.Content = content;
    }

    public class Tokens {
        // 关键词的字符串
        public String Text;

        // 关键词的首字节在文档中出现的位置
        public List<Integer> Locations;
    }
}
