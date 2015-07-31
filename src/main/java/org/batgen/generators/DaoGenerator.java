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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.batgen.Column;
import org.batgen.IndexNode;
import org.batgen.Table;

/**
 * Generates a basic set of DAO classes.
 */
public class DaoGenerator extends Generator {
    private Table table;
    private String daoName = "";

    private StringBuilder sb = new StringBuilder();
    private String filePath;
    private List<Column> keyColumns = new ArrayList<Column>();

    private final String TAB = "    ";
    private final String IMPORT_DATE = "import java.util.Date;";

    public DaoGenerator( Table table ) {
        super( table );
        this.table = table;
        daoName = table.getDomName() + "Dao";
        filePath = "src/main/java/" + packageToPath() + "/dao/" + daoName + ".java";
        for ( Column column : table.getColumns() ) {
            if ( column.isKey() ) {
                keyColumns.add( column );
            }
        }
    }

    public String createDao() {

        writePkg();
        writeImport();
        writeInterface();
        writeList();
        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );
        createDaoExceptions();

        return filePath;
    }

    private void createDaoExceptions() {

        sb = new StringBuilder();
        String filePath = "src/main/java/" + packageToPath() + "/util/DaoException.java";
        File file = new File( filePath );

        if ( !file.exists() ) {
            sb.append( "package " + table.getPackage() + ".util;\n" + "\n"
                    + "public class DaoException extends Exception {\n" + TAB + "\n" + TAB
                    + "private static final long serialVersionUID = 1L;\n" + "\n" + TAB
                    + "public DaoException(Throwable e) {\n" + TAB + TAB + "super(e);\n" + TAB
                    + "}\n" + "\n" + TAB + "public DaoException(String msg) {\n" + TAB + TAB
                    + "super(msg);\n" + TAB + "}\n" + "\n" + TAB
                    + "public DaoException(String msg, Throwable e) {\n" + TAB + TAB
                    + "super(msg, e);\n" + TAB + "}\n" + "\n\n" );
            sb.append( getProtectedJavaLines( filePath ) );
            writeToFile( filePath, sb.toString() );
        }

    }

    private void writeImport() {
        ImportGenerator imports = new ImportGenerator( filePath );
        if ( hasSearch || hasJoin )
            imports.addImport( "import java.util.List;" );

        boolean date = false;
        for ( Column col : table.getColumns() ) {
            if ( col.getFldType().equals( "Date" ) ) {
                date = true;
            }
        }
        if ( date ) {
            imports.addImport( IMPORT_DATE );
        }

        if ( hasJoin ) {
            imports.addImport( "import " + table.getPackage() + ".domain.*;" );
        } else {
            imports.addImport( "import " + table.getPackage() + ".domain." + table.getDomName()
                    + ";" );
        }
        imports.addImport( "import " + table.getPackage() + ".util." + "DaoException;" );
        imports.addImport( "import org.apache.ibatis.annotations.Param;" );

        write( imports.toString() );
    }

    private void writePkg() {
        write( "package " + table.getPackage() + ".dao;" );
        write( "\n\n" );
    }

    private void writeInterface() {
        write( "public interface " + daoName + " { \n\n" );
        write( TAB + "public int create( " + table.getDomName() );
        write( " value ) throws DaoException;\n\n" );
        write( TAB + "public int update( " + table.getDomName() );
        write( " value ) throws DaoException;\n\n" );

        String param = table.getDomName() + " param";
        write( TAB + "public int delete( " + param + " ) throws DaoException;\n\n" );
        write( TAB + "public " + table.getDomName() + " read( " + param
                + " ) throws DaoException;\n\n" );

        if ( table.isManyToMany() ) {
            writeManyToMany();
        }

        for ( IndexNode node : table.getIndexList() ) {
            String methodName = "readByIndex" + toTitleCase( node.getIndexName() );

            write( TAB + "public " + table.getDomName() + " " + methodName + "( " + param
                    + " ) throws DaoException;\n\n" );
        }

    }

    /** 
     * Generate the getTable1ListByTable2Key and getTable2ListByTable1Key
     * DAO methods for the many-to-many link tables
     */
    private void writeManyToMany() {

        Table one = table.getTableOne();
        Table two = table.getTableTwo();

        String param = two.getDomName() + " param";
        
        write( TAB + "public " );
        write( "List<" + one.getDomName() + "> get" );
        sb.append( one.getDomName() );
        sb.append( "ListBy" );
        sb.append( two.getDomName() + "Key" );
        write( "( " );
        write( param );
        write( " ) throws DaoException;\n" );

        write( "\n" );

        param = one.getDomName() + " param";
        
        write( TAB + "public " );
        write( "List<" + two.getDomName() + "> get" );
        sb.append( two.getDomName() );
        sb.append( "ListBy" );
        sb.append( one.getDomName() + "Key" );
        write( "( " );
        write( param );
        write( " ) throws DaoException;\n" );
        
    }

    private void writeList() {
        for ( Column column : table.getColumns() ) {
            if ( column.isSearchId() ) {
                write( TAB + "public " );
                write( "List<" + table.getDomName() + "> getListBy" );
                write( toTitleCase( column.getFldName() ) );
                write( "( " );
                write( column.getFldType() );
                write( " key ) throws DaoException;\n" );
            }

        }
        write( "\n" );
    }

    private void write( String str ) {
        sb.append( str );
    }

}
