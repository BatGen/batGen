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
package org.batgen.generators;

import java.util.ArrayList;
import java.util.List;

import org.batgen.BlobColumn;
import org.batgen.Column;
//import org.batgen.DatabaseType;
import org.batgen.DoubleColumn;
import org.batgen.IndexNode;
import org.batgen.LengthColumn;
import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

public class SqlGenerator extends Generator {

    private String          key       = "KEY";
    private final int       SPACE     = 18;
    private String          filePath;
    private List<IndexNode> indexList = new ArrayList<IndexNode>();
    private List<String>    fieldList = new ArrayList<String>();

    public SqlGenerator( Table table ) {
        super( table );

        filePath = "sql/" + table.getTableName() + ".sql";
    }

    public String createSql() {
        StringBuilder sb = new StringBuilder();

        sb.append( messageRemove() );
        sb.append( drop() );
        sb.append( messageCreate() );
        sb.append( createTable() );
        sb.append( messageSample() );
        sb.append( createSample() );
        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );

        appendToFile( "sql/CreateTables.sql", writeColumns() );
        writeDropsFile();

        return filePath;
    }

    private String createTable() {
        StringBuilder sb = new StringBuilder();
        sb.append( writeColumns() );

        return sb.toString();
    }

    private String writeColumns() {
        StringBuilder sb = new StringBuilder();
        sb.append( "\nCREATE TABLE " + table.getTableName() + " (\n" );

        for ( Column column : table.getColumns() ) {

            String name = column.getClass().getSimpleName();
            if ( !name.equals( "ListColumn" ) ) {
                sb.append( "    " + column.getColName().toUpperCase()
                        + makeSpace( SPACE, column.getColName() ) );

                if ( name.equals( "BlobColumn" ) ) {
                    BlobColumn c = (BlobColumn) column;
                    sb.append( column.getSqlType() );
                    if ( c.getColLen() != null )
                        sb.append( "(" + c.getColLen() + ")" );

                }
                else if ( name.equals( "LengthColumn" ) ) {
                    LengthColumn c = (LengthColumn) column;
                    if ( c.getColLen() != null ) {
                        if ( column.getSqlType().equals( "CHAR" ) ) {
                            sb.append( column.getSqlType() );
                        }
                        else {
                            sb.append( column.getSqlType() + "("
                                    + c.getColLen() + ")" );
                        }
                    }

                }
                else if ( name.equals( "DoubleColumn" ) ) {
                    DoubleColumn c = (DoubleColumn) column;
                    if ( c.getColLen() != null ) {
                        sb.append( column.getSqlType() + "(" + c.getColLen()
                                + "," + c.getPrecision() + ")" );
                    }

                }
                else if ( name.equals( "Column" ) ) {
                    Column c = column;
                    sb.append( c.getSqlType() );
                }

                if ( column.isRequired() )
                    sb.append( " NOT NULL" );

                if ( column.isKey() )
                    key = column.getColName().toUpperCase();

                sb.append( ",\n" );
            }
        }

        sb.append( "    CONSTRAINT " + table.getTableName()
                + "_PK PRIMARY KEY (" + key.toUpperCase() + ")" );

        sb.append( ");\n" );
        sb.append( "\nCREATE SEQUENCE " + table.getTableName() + "_SEQ;\n" );

        sb.append( writeIndexes() );

        return sb.toString();
    }

    private String writeIndexes() {
        StringBuilder sb = new StringBuilder();
        indexList = table.getIndexList();
        for ( int i = 0; i < indexList.size(); i++ ) {
            sb.append( "\nCREATE INDEX " + indexList.get( i ).getIndexName() );
            sb.append( " ON " + table.getTableName() + "( " );
            fieldList = indexList.get( i ).getFieldList();
            sb.append( fieldList.get( 0 ) );
            for ( int j = 1; j < fieldList.size(); j++ ) {
                sb.append( ", " + fieldList.get( j ) );
            }
            sb.append( " );" );
        }
        sb.append( "\n" );
        return sb.toString();
    }

    private String createSample() {
        StringBuilder sb = new StringBuilder();
        sb.append( "SELECT\n    " );

        for ( int i = 0; i < table.getColumns().size(); i++ ) {
            if ( !"ListColumn".equals( table.getColumn( i ).getClass()
                    .getSimpleName() ) ) {
                sb.append( table.getColumn( i ).getColName().toUpperCase()
                        + ", " );
            }
        }

        sb.deleteCharAt( sb.length() - 2 );
        sb.append( "\nfrom " + table.getTableName() + "\nWHERE\n    " + key
                + " = 0;\n" );

        return sb.toString();
    }

    private void writeDropsFile() {
        appendToFile( "sql/DropTables.sql", drop() + "\n\n" );
    }

    private String drop() {
        return "DROP TABLE " + table.getTableName() + ";\nDROP SEQUENCE "
                + table.getTableName() + "_SEQ;\n";
    }

    private String messageRemove() {
        return "-- Remove Original Table and Sequence\n\n";
    }

    private String messageCreate() {
        return "-- Create Table\n\n";
    }

    private String messageSample() {
        return "\n-- Sample Select Statement\n\n";
    }
}
