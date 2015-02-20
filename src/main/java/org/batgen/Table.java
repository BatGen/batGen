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

import java.util.ArrayList;
import java.util.List;

public class Table {

    private String comment;
    private String pkg;
    private String domName;
    private String tableName;
    private boolean hasSearch = false;
    private List<Column> columns = new ArrayList<Column>();
    private List<String> searchList = new ArrayList<String>();
    private List<IndexNode> indexList = new ArrayList<IndexNode>();

    int count = 0;

    public String toString() {
        String first = "The table, " + domName + " is located in " + pkg
                + ", its fields are: ";
        StringBuilder sb = new StringBuilder( first );

        for ( int i = 0; i < columns.size(); i++ ) {
            Column c = columns.get( i );
            sb.append( "\n " + c.toString() + " |" + c.getClass() );
        }

        return ( first = sb.toString() );
    }

    public void setComment( String c ) {
        comment = c;
    }

    public String getComment() {
        return comment;
    }

    public void setTableName( String c ) {
        tableName = c;
    }

    public String getTableName() {
        return tableName;
    }

    public void setPackage( String p ) {
        pkg = p;
    }

    public String getPackage() {
        return pkg;
    }

    public void setDomName( String c ) {
        domName = c;
    }

    public String getDomName() {
        return domName;
    }

    public void addColumn( Column c ) {
        columns.add( c );
    }

    public void addColumn( int num, Column c ) {
        columns.add( num, c );
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Column getColumn( int i ) {
        return columns.get( i );
    }

    private void setKeyToFirstElement() {

        Column column;
        for ( int i = 0; i < columns.size(); i++ ) {
            column = columns.get( i );

            if ( column.isKey() ) {
                if ( i == 0 )
                    return;

                columns.remove( i );
                columns.add( 0, column );
                return;
            }
        }

        // if we are still here, no key was found.
        LengthColumn keyCol = new LengthColumn();
        keyCol.setColName( "KEY" );
        keyCol.setType( FieldType.LONG );
        keyCol.setFldName( "key" );
        keyCol.setKey();
        keyCol.setColLen( "10" );
        if(columns.get( 0 ).isSequenceDisabled()){
            keyCol.setSequenceDisabled();
        }

        columns.add( 0, keyCol );
    }

    private void createSearchList() {
        searchList.clear();
        for ( Column column : columns ) {
            if ( column.isSearchId() ) {
                searchList.add( column.getFldName() );
                hasSearch = true;
            }
        }
    }

    public List<String> getSearchList() {
        return searchList;
    }

    public boolean hasSearch() {
        return hasSearch;
    }

    public void addIndex(IndexNode node){
        indexList.add( node );
    }
    public List<IndexNode> getIndexList() {
        return indexList;
    }

    /**
     * Goes through the columns and makes sure the key is the first column. If a
     * key doesn't exist, one is created.
     * 
     * Also creates the list of fields which can be used to search which is used
     * later in the code generators.
     * 
     */
    public void setup() {
        setKeyToFirstElement();
        createSearchList();
    }
}
