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

import static org.batgen.generators.GenUtil.writeToFile;

import java.util.ArrayList;
import java.util.List;

import org.batgen.BlobColumn;
import org.batgen.Column;
//import org.batgen.DatabaseType;
import org.batgen.DoubleColumn;
import org.batgen.IndexNode;
import org.batgen.LengthColumn;
import org.batgen.Table;
import org.batgen.VirtualStringColumn;

public class SqlGenerator extends Generator {

    private final int SPACE = 18;
    private String filePath;
    private List<IndexNode> indexList = new ArrayList<IndexNode>();
    private List<String> keyList = new ArrayList<String>();
    private boolean buildSequence;

    public SqlGenerator( Table table ) {
        super( table );

        filePath = "sql/" + table.getTableName() + ".sql";
    }

    public String createSql() {
        buildSequence = sequenceDisabled();
        StringBuilder sb = new StringBuilder();
        String table = writeColumns();

        sb.append( messageRemove() );
        sb.append( drop() );
        sb.append( messageCreate() );
        sb.append( table );
        sb.append( messageSample() );
        sb.append( createSample() );
        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );

        GenUtil.appendToFile( "sql/_CreateTables.sql", table );

        return filePath;
    }

    private String writeColumns() {
        StringBuilder sb = new StringBuilder();
        sb.append( "\nCREATE TABLE " + table.getTableName() + " (\n" );

        for ( Column column : table.getColumns() ) {

            String name = column.getClass().getSimpleName();
            sb.append( "    " + column.getColName().toUpperCase()
                    + makeSpace( SPACE, column.getColName() ) );

            if ( name.equals( "BlobColumn" ) ) {
                BlobColumn c = ( BlobColumn ) column;
                sb.append( column.getSqlType() );
                if ( c.getColLen() != null )
                    sb.append( "(" + c.getColLen() + ")" );

            } else if ( name.equals( "LengthColumn" ) ) {
                LengthColumn c = ( LengthColumn ) column;
                if ( c.getColLen() != null ) {
                    sb.append( column.getSqlType() + "(" + c.getColLen() + ")" );
                }
            } else if ( name.equals( "DoubleColumn" ) ) {
                DoubleColumn c = ( DoubleColumn ) column;
                if ( c.getColLen() != null ) {
                    sb.append( column.getSqlType()
                            + "("
                            + ( Integer.parseInt( c.getColLen() ) + Integer.parseInt( c
                                    .getPrecision() ) ) + "," + c.getPrecision() + ")" );
                }
            } else if ( name.equals( "Column" ) ) {
                Column c = column;
                sb.append( c.getSqlType() );
            } else if ( name.equals( "VirtualStringColumn" ) ) {
                VirtualStringColumn c = ( VirtualStringColumn ) column;
                sb.append( column.getSqlType() + "(" + c.getColLen() + ") as (" );
                sb.append( c.getSqlCommand() + ")" );
            }
            if ( column.isRequired() ) {
                sb.append( " NOT NULL" );
            }
            if ( column.isKey() ) {
                keyList.add( column.getColName().toUpperCase() );
                sb.append( " NOT NULL" );
            }
            sb.append( ",\n" );
        }

        sb.append( "    CONSTRAINT " + table.getTableName() + "_PK PRIMARY KEY ( " );
        for ( String key : keyList ) {
            sb.append( key.toUpperCase() + ", " );
        }
        sb.deleteCharAt( sb.length() - 2 );
        sb.append( ")" );

        sb.append( ");\n" );

        if ( buildSequence ) {
            sb.append( "\nCREATE SEQUENCE " + table.getTableName() + "_SEQ;\n" );
        }

        sb.append( writeIndexes() );

        return sb.toString();
    }

    private String writeIndexes() {
        StringBuilder sb = new StringBuilder();
        indexList = table.getIndexList();
        for ( IndexNode node : indexList ) {
            String param = "";
            for ( Column col : node.getColumnList() ) {
                param += col.getColName() + ", ";
            }
            param = param.substring( 0, param.length() - 2 );

            sb.append( "\nCREATE INDEX " + node.getIndexName() );
            sb.append( " ON " + table.getTableName() + "( " + param + " );" );

        }

        // index for primary key(s)
        String param = "";
        for ( Column col : table.getColumns() ) {
            if ( col.isKey() ) {
                param += col.getColName() + ", ";
            }
        }
        param = param.substring( 0, param.length() - 2 );

        sb.append( "\nCREATE INDEX " + table.getTableName() + "_PK" );
        sb.append( " ON " + table.getTableName() + "( " + param + " );" );

        if ( table.isManyToMany() ) {
            Table one = table.getTableOne();
            Table two = table.getTableTwo();

            param = "";
            for ( Column col : one.getColumns() ) {
                if ( col.isKey() ) {
                    param += one.getTableName() + "_" + col.getColName() + ", ";
                }
            }
            param = param.substring( 0, param.length() - 2 );

            sb.append( "\nCREATE INDEX " + table.getTableName() + "_" + one.getTableName() );
            sb.append( " ON " + table.getTableName() + "( " + param + " );" );

            param = "";
            for ( Column col : two.getColumns() ) {
                if ( col.isKey() ) {
                    param += two.getTableName() + "_" +col.getColName() + ", ";
                }
            }
            param = param.substring( 0, param.length() - 2 );

            sb.append( "\nCREATE INDEX " + table.getTableName() + "_" + two.getTableName() );
            sb.append( " ON " + table.getTableName() + "( " + param + " );" );
        }

        sb.append( "\n" );
        return sb.toString();
    }

    private String createSample() {
        StringBuilder sb = new StringBuilder();
        sb.append( "SELECT\n    " );

        for ( Column col : table.getColumns() ) {
            if ( !"ListColumn".equals( col.getClass().getSimpleName() ) ) {
                sb.append( col.getColName().toUpperCase() + ", " );
            }
        }

        sb.deleteCharAt( sb.length() - 2 );
        if ( keyList.isEmpty() ) {
            sb.append( "\nfrom " + table.getTableName() + "\nWHERE\n    KEY = 0;\n" );
        } else {
            sb.append( "\nfrom " + table.getTableName() + "\nWHERE\n    " + keyList.get( 0 )
                    + " = 0;\n" );
        }
        return sb.toString();
    }

    private String drop() {
        StringBuilder sb = new StringBuilder();
        sb.append( "DROP TABLE " + table.getTableName() + ";\n" );
        if ( buildSequence ) {
            sb.append( "DROP SEQUENCE " + table.getTableName() + "_SEQ;\n" );
        }
        return sb.toString();
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

    private boolean sequenceDisabled() {
        return ( !table.getColumn( 0 ).isSequenceDisabled()
                && !table.getColumn( 0 ).getFldType().equalsIgnoreCase( "string" )
                && table.getColumn( 0 ).isKey() && !table.isManyToMany() );

    }
}
