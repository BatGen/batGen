package org.batgen;

/**
 * Member variable types in a class.  We can have either
 * primitive types or wrapper class types based on whether
 * a field is required or not.   
 *
 */
public enum FieldType {
    INTEGER ( "INTEGER", "int",     "Integer",   "NUMBER" ),
    LONG    ( "LONG",    "long",    "Long",      "NUMBER" ),
    DOUBLE  ( "DOUBLE",  "double",  "Double",    "NUMBER" ),
    BOOLEAN ( "BOOLEAN", "boolean", "Boolean",   "CHAR" ),
    DATE    ( "DATE",    "Date",    "Date",      "DATE" ),
    BLOB    ( "BLOB",    "byte[]",  "byte[]",    "BLOB" ),
    CLOB    ( "CLOB",    "String",  "String",    "CLOB" ),
    STRING  ( "STRING",  "String",  "String",    "VARCHAR2" ),
    TIMESTAMP ("TIMESTAMP",  "Date",  "Date",    "TIMESTAMP"),
    VSTRING  ( "VSTRING",  "String",  "String",    "VARCHAR2" );

    private String name;
    private String primativeType;
    private String wrapperType;
    private String sqlType;

    private FieldType( String name, String primativeType, String wrapperType, String sqlType ) {
        this.name = name;
        this.primativeType = primativeType;
        this.wrapperType  = wrapperType;
        this.sqlType = sqlType;
        }

    public String getName() {
        return name;
    }

    public String getPrimativeType() {
        return primativeType;
    }

    public String getWrapperType() {
        return wrapperType;
    }

    public String getSqlType() {
        return sqlType;
    }
}
