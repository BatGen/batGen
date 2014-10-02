/***
 * The MIT License (MIT) 
 * 
 * Copyright (c) 2014 SimonComputing, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.batgen;

public class Column {
    /**
     * Flag values are as follows:
     * 
     * bit 1 = required bit 1&2 = key bit 3 = sequence disabled bit 8 = search
     * id
     * 
     */
    public static final int REQUIRED = 1;
    public static final int KEY = 2;
    public static final int SEQUENCE_DISABLED = 4;
    public static final int SEARCH_ID = 8;

    private FieldType type;

    private String fldName;
    private String colName;
    private String comment;

    private Table table;

    private int flags;

    public void setType( FieldType type ) {
        this.type = type;
    };

    public FieldType getType() {
        return type;
    }

    public void setColName( String colName ) {
        this.colName = colName;
    }

    public String getColName() {
        return colName;
    }

    public void setFldName( String fldName ) {
        this.fldName = fldName;
    }

    public String getFldName() {
        return fldName;
    }

    public String getSqlType() {
        return type.getSqlType();
    }

    public String getFldType() {
        if ( type == null )
            throw new IllegalStateException( "type was never assigned for: "
                    + table.getDomName() + "." + colName );

        return isRequired() ? type.getPrimativeType() : type.getWrapperType();
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }

    public String getComments() {
        return comment;
    }

    public void setRequired() {
        flags |= REQUIRED;
    }

    public boolean isRequired() {
        return ( flags & KEY ) != 0;
    }

    public void setKey() {
        flags |= KEY | REQUIRED;
    }

    public boolean isKey() {
        return ( flags & KEY ) != 0;
    }

    public void setSequenceDisabled() {
        flags |= SEQUENCE_DISABLED;
    }

    public boolean isSequenceDisabled() {
        return ( flags & SEQUENCE_DISABLED ) != 0;
    }

    public void setSearchId() {
        flags |= SEARCH_ID;
    }

    public boolean isSearchId() {
        return ( flags & SEARCH_ID ) != 0;
    }

    public void setTable( Table table ) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        return "Column [fldName=" + fldName + ", colName=" + colName
                + ", fldType=" + getFldType() + ", colType=" + getSqlType()
                + ", comment=" + comment + ", required="
                + isRequired() + ", key=" + isKey() + "searchable=" +isSearchId() +"]";
    }
}
