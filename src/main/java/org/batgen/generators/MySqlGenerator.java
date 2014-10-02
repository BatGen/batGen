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
import org.batgen.DoubleColumn;
import org.batgen.LengthColumn;
import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

/**
 * Generates MySql version of the SQL. Limited testing has been performed on
 * this code.
 *
 */
public class MySqlGenerator extends Generator {
    private List<String> fKeys = new ArrayList<String>();
    private String key = "";
    private final int SPACE = 18;
    private String filePath;
    private String createPath;
    private String dropPath;

    public MySqlGenerator( Table table ) {
        super( table );

        filePath = "sql/" + table.getTableName() + ".sql";
        createPath = "sql/CreateTables.sql";
        dropPath = "sql/DropTables.sql";
    }

    public String createSql() {
        StringBuilder sb = new StringBuilder();

        sb.append( messageRemove() );
        sb.append( drop() );
        sb.append( messageCreate() );
        sb.append( createTable() );
        sb.append( messageSample() );
        sb.append( createSample() );

        writeToFile( filePath, sb.toString() );
        addToAggregateFiles();

        return filePath;
    }

    private String createTable() {
        StringBuilder sb = new StringBuilder();

        sb.append( "CREATE TABLE `" + table.getTableName().toLowerCase()
                + "`(\n" );
        sb.append( writeColumns() );

        return sb.toString();
    }

    private String writeColumns() {
        StringBuilder sb = new StringBuilder();
        for ( Column column : table.getColumns() ) {
            StringBuilder temp = new StringBuilder();

            String name = column.getClass().getSimpleName();
            if ( "KEY".equalsIgnoreCase( column.getColName() ) ) {
                key = column.getColName();
            }

            if ( !name.equals( "ListColumn" ) ) {
                temp.append( TAB + "`" + column.getColName() + "`" );
                temp.append( makeSpace( SPACE, temp.toString() ) );

                if ( name.equals( "LengthColumn" ) ) {
                    LengthColumn c = (LengthColumn) column;
                    if ( c.getColLen() != null ) {
                        if ( column.getSqlType().equals( "CHAR" ) ) {
                            temp.append( "char(1) " );
                        }
                        else {
                            if ( ( c.getFldType().equalsIgnoreCase( "LONG" ) )
                                    || ( c.getFldType()
                                            .equalsIgnoreCase( "INTEGER" ) ) ) {
                                temp.append( "int(" + c.getColLen() + ") " );
                            }
                            else if ( c.getFldType()
                                    .equalsIgnoreCase( "String" ) ) {
                                temp.append( "varchar(" + c.getColLen() + ") " );
                            }
                        }
                    }

                }
                else if ( name.equals( "DoubleColumn" ) ) {
                    DoubleColumn c = (DoubleColumn) column;
                    if ( c.getColLen() != null ) {
                        temp.append( c.getFldType().toLowerCase() + "("
                                + c.getColLen() + "," + c.getPrecision() + ") " );
                    }
                }
                else if ( name.equals( "BlobColumn" ) ) {
                    BlobColumn c = (BlobColumn) column;
                    temp.append( column.getSqlType().toLowerCase() );
                    if ( c.getColLen() != null )
                        temp.append( "(" + c.getColLen() + ") " );
                }
                else if ( name.equals( "Column" ) ) {
                    Column c = (Column) column;
                    if ( c.getFldType().equalsIgnoreCase( "LONG" ) )
                        temp.append( "int(10)" );
                    else
                        temp.append( column.getSqlType().toLowerCase() + " " );
                    if ( c.getTable() != null ) {
                        temp.append( " NOT NULL" );
                        if ( fKeys.size() == 0 ) {
                            fKeys.add( "KEY `Key_idx" + ( fKeys.size() + 1 )
                                    + "` (`" + c.getColName() + "`),\n" );
                            fKeys.add( "CONSTRAINT `"
                                    + table.getTableName().toLowerCase()
                                    + "_fk" + ( fKeys.size() )
                                    + "` FOREIGN KEY (`" + c.getColName()
                                    + "`) REFERENCES `"
                                    + c.getTable().getTableName().toLowerCase()
                                    + "` (`KEY`)" );
                        }
                        else {
                            fKeys.add( ",\n" + TAB + "KEY `Key_idx"
                                    + ( fKeys.size() ) + "` (`"
                                    + c.getColName() + "`),\n" );
                            fKeys.add( "CONSTRAINT `"
                                    + table.getTableName().toLowerCase()
                                    + "_fk" + ( fKeys.size() - 1 )
                                    + "` FOREIGN KEY (`" + c.getColName()
                                    + "`) REFERENCES `"
                                    + c.getTable().getTableName().toLowerCase()
                                    + "` (`KEY`)" );
                        }
                    }
                }
                if ( column.isRequired() ) {
                    temp.append( "NOT NULL" );
                }
                else if ( column.getTable() == null ) {
                    if ( !name.equals( "BlobColumn" ) ) {
                        temp.append( "DEFAULT NULL" );
                    }
                }
                if ( column.isKey()
                        && column.getSqlType().equalsIgnoreCase( "NUMBER" ) ) {
                    temp.append( " AUTO_INCREMENT" );
                }

                sb.append( temp.toString() + ",\n" );
            }
        }
        if ( key.equals( "" ) ) {
            System.out.println( "The key table is null" );
        }
        sb.append( "\n" + TAB + "PRIMARY KEY (`" + key + "`)" );
        if ( fKeys.size() > 0 ) {
            sb.append( ",\n" );
            sb.append( getFKeys() );
        }
        sb.append( "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8;" );
        return sb.toString();
    }

    private String createSample() {
        StringBuilder sb = new StringBuilder();
        sb.append( "select\n    " );

        for ( int i = 0; i < table.getColumns().size(); i++ ) {
            if ( !"ListColumn".equals( table.getColumn( i ).getClass()
                    .getSimpleName() ) ) {
                sb.append( "`" + table.getColumn( i ).getColName() + "`, " );
            }
        }

        sb.deleteCharAt( sb.length() - 2 );
        sb.append( "\nfrom " + "`" + table.getTableName().toLowerCase() + "`"
                + "\nwhere\n    " + "`" + key + "`" + " = 0;\n" );

        return sb.toString();
    }

    private String drop() {
        return "drop table `" + table.getTableName().toLowerCase() + "`;\n";
    }

    private String messageRemove() {
        return "-- Remove Original Table and Sequence\n\n";
    }

    private String messageCreate() {
        return "\n-- Create Table\n\n";
    }

    private String messageSample() {
        return "\n\n-- Sample Select Statement\n\n";
    }

    private String getFKeys() {
        StringBuilder foreignString = new StringBuilder();

        for ( int i = 0; i < fKeys.size(); i++ ) {
            foreignString.append( TAB + fKeys.get( i ) );
        }

        return foreignString.toString();
    }

    private void addToAggregateFiles() {

        // Write to the table creation file
        appendToFile( createPath, "\n\n" + createTable() + "\n\n" );

        // Write to the table drop file
        appendToFile( dropPath, "\n\n" + drop() + "\n\n" );

    }
}
