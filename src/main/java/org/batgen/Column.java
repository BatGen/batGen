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

    private FieldType type;
    private String    fldName;
    private String    colName;
    private String    comment;
    private Table     table;
    
    private boolean   isLinkGenerated = false;
    private boolean   key;
    private boolean   required;
    private boolean   sequenceDisable;
    private boolean   searchId;
    private boolean   sysTimestamp;

    public Column(Column another) {
        this.type = another.getType();
        this.fldName = another.getFldName();
        this.colName = another.getColName();
        this.comment = another.getComments();
        this.table = another.getTable();
        this.key = another.key;
        this.required = another.isRequired();
        this.sequenceDisable = another.isSequenceDisabled();
        this.searchId = another.isSearchId();
        this.sysTimestamp = another.isSysTimestamp();
      }
    
    public Column() {
        // TODO Auto-generated constructor stub
    }

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
            throw new IllegalStateException( "type was never assigned for: " + table.getDomName() + "." + colName );
        return type.getWrapperType();
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }

    public String getComments() {
        return comment;
    }

    public void setRequired() {
        required = true;
    }

    public boolean isRequired() {
        return required;
    }

    public void setKey() {
        key = true;
    }

    public boolean isKey() {
        return key;
    }

    public void setSequenceDisabled() {
        sequenceDisable = true;
    }

    public boolean isSequenceDisabled() {
        return sequenceDisable;
    }

    public void setSearchId() {
        searchId = true;
    }

    public boolean isSearchId() {
        return searchId;
    }
    
    public void setSysTimestamp() {
        sysTimestamp = true;
    }

    public boolean isSysTimestamp() {
        return sysTimestamp;
    }

    public void setTable( Table table ) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        return "Column: fldName=" + fldName + ", colName=" + colName + ", fldType=" + getFldType() + ", colType="
                + getSqlType() + ", comment=" + comment + ", required=" + isRequired() + ", key=" + isKey()
                + ", sequenceDisable=" + isSequenceDisabled() + ", searchable=" + isSearchId();
    }

    public boolean isLinkGenerated() {
        return isLinkGenerated;
    }

    public void setLinkGenerated(boolean isLinkGenerated) {
        this.isLinkGenerated = isLinkGenerated;
    }
}
