package com.tf.search.types;

public class SimpleFieldInfo {

    public String FieldName;

    public IdxType FieldType;

    public SimpleFieldInfo(String FieldName, IdxType FieldType){
        this.FieldName = FieldName;
        this.FieldType = FieldType;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleFieldInfo field = (SimpleFieldInfo) o;

        if (FieldName != field.FieldName) return false;

        if (FieldType != field.FieldType) return false;

        return true;
    }
}
