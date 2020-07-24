package com.tf.search.types;

/**
 * Mappings Type
 */
public enum IdxType {

    /**
     * 字符型索引[全词匹配]
     */
    IDX_TYPE_STRING(1),
    /**
     * 字符型索引[切词匹配，全文索引,hash存储倒排]
     */
    IDX_TYPE_STRING_SEG(2),
    /**
     * 字符型索引[列表类型，分号切词，直接切分,hash存储倒排]
     */
    IDX_TYPE_STRING_LIST(3),
    /**
     * 字符型索引[单字切词]
     */
    IDX_TYPE_STRING_SINGLE(4),
    /**
     * 数字型索引，只支持整数，数字型索引只建立正排
     */
    IDX_TYPE_NUMBER(5),
    /**
     * 日期型索引 '2015-11-11 00:11:12'，日期型只建立正排，转成时间戳存储
     */
    IDX_TYPE_DATE(6);

    public final short code;

    IdxType(int code) {
        this.code = (short) code;
    }

    public static IdxType valueOf(short code) {
        switch (code) {
            case 0:
                return IDX_TYPE_STRING_SEG;
            case 1:
                return IDX_TYPE_STRING_LIST;
            case 2:
                return IDX_TYPE_STRING_SINGLE;
            case 3:
                return IDX_TYPE_NUMBER;
            case 4:
                return IDX_TYPE_DATE;
            default:
                return IDX_TYPE_STRING;
        }
    }

}
