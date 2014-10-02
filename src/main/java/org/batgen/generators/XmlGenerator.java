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

import org.batgen.Column;
import org.batgen.DatabaseType;
import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

public class XmlGenerator extends Generator {
    public static String NEWLINE = "\n";

    private static final int columnSpace = 45;
    private List<String> sqlVariables = null;
    private List<String> javaVariables = null;
    private List<Column> searchableColumns = null;

    private String daoName = "";
    private String filePath;
    private DatabaseType databaseType;

    public XmlGenerator( Table table, DatabaseType databaseType ) {
        super( table );
        this.daoName = table.getDomName() + "Dao";
        this.databaseType = databaseType;
        filePath = "src/main/resources/" + packageToPath() + "/dao/" + daoName
                + ".xml";
        filePath = filePath.replace( "_", "" );
    }

    public String createXml() {
        sqlVariables = new ArrayList<String>();
        javaVariables = new ArrayList<String>();
        searchableColumns = new ArrayList<Column>();

        StringBuilder sb = new StringBuilder();

        sb.append( createHeading() );
        sb.append( createCol() );
        sb.append( createRead() );
        sb.append( createInsert() );
        sb.append( createGetListBy() );
        sb.append( createUpdate() );
        sb.append( createDelete() );
        sb.append( createProtected() );

        writeToFile( filePath, sb.toString() );
        return filePath;
    }

    private String createHeading() {
        StringBuilder sb = new StringBuilder();

        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" );
        sb.append( "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n"
                + TAB + "\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">" );

        return sb.toString();
    }

    private String createCol() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" );

        sb.append( "<mapper namespace=\"" + table.getPackage() + ".dao."
                + table.getDomName() + "Dao\">\n" );
        sb.append( "\n" + TAB + "<resultMap id=\"" + table.getDomName()
                + "Mapper\" type=\"" + table.getPackage() + ".domain."
                + table.getDomName() + "\">" );

        for ( int i = 0; i < table.getColumns().size(); i++ ) {
            StringBuilder string = new StringBuilder();

            string.append( TAB + TAB + "<result column =\""
                    + table.getColumn( i ).getColName().toUpperCase() + "\" " );
            string.append( makeSpace( columnSpace, string.toString() ) );
            string.append( "property = \"" + table.getColumn( i ).getFldName()
                    + "\" />" );
            sqlVariables.add( table.getColumn( i ).getColName().toUpperCase() );
            javaVariables.add( table.getColumn( i ).getFldName() );

            sb.append( "\n" + string.toString() );

            if ( table.getColumn( i ).isSearchId() ) {
                searchableColumns.add( table.getColumn( i ) );
            }
        }

        sb.append( "\n" + TAB + "</resultMap>" );

        return sb.toString();
    }

    private String createRead() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" );
        sb.append( "\n" );

        if ( ( table.getColumn( 0 ).getFldType().equalsIgnoreCase( "string" ) ) ) {
            sb.append( TAB + "<select id=\"read\" parameterType=\""
                    + table.getColumn( 0 ).getFldType().toLowerCase()
                    + "\" resultMap=\"" + table.getDomName() + "Mapper\">\n" );
        }
        else {
            sb.append( TAB + "<select id=\"read\" parameterType=\"_"
                    + table.getColumn( 0 ).getFldType().toLowerCase()
                    + "\" resultMap=\"" + table.getDomName() + "Mapper\">\n" );
        }

        if ( databaseType == DatabaseType.MYSQL ) {
            sb.append( TAB + TAB + "select * from "
                    + table.getTableName().toLowerCase() + "\n" + TAB + TAB
                    + "where `" + sqlVariables.get( 0 ) + "` = #{"
                    + javaVariables.get( 0 ) + "}\n" );
        }
        else {
            sb.append( TAB + TAB + "select * from "
                    + table.getTableName().toUpperCase() + "\n" + TAB + TAB
                    + "where " + sqlVariables.get( 0 ) + " = #{"
                    + javaVariables.get( 0 ) + "}\n" );
        }

        sb.append( TAB + "</select>\n" );

        return sb.toString();
    }

    private String createInsert() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" );
        sb.append( TAB + "<insert id=\"create\" parameterType=\""
                + table.getPackage() + ".domain." + table.getDomName()
                + "\">\n" );
        
        
        //
        if(!(table.getColumn(0).getFldType().equalsIgnoreCase("string"))){
            sb.append(TAB+TAB+"<selectKey resultType=\"_"+table.getColumn(0).getFldType().toLowerCase()+"\" keyProperty=\""+table.getColumn(0).getFldName()+"\" order=\"BEFORE\">\n");
            
            switch( databaseType ) {
                case MYSQL:
                    sb.append( TAB );
                    sb.append( TAB );
                    sb.append( TAB );
                    sb.append(  "select AUTO_INCREMENT FROM information_schema.tables WHERE TABLE_NAME = \"" );
                    sb.append( table.getTableName().toLowerCase() );
                    sb.append( "\"\n" );
                    break;
                case H2:
                case ORACLE:
                    sb.append( TAB );
                    sb.append( TAB );
                    sb.append( TAB );
                    sb.append("select " );
                    sb.append( table.getTableName().toUpperCase() );
                    sb.append( "_SEQ.nextval from dual\n" );
            }
            
            sb.append(TAB+TAB+"</selectKey>\n");
        }
        //

        sb.append( TAB + TAB + "insert into "
                + table.getTableName().toUpperCase() + "\n" );
        sb.append( TAB + TAB + "(\n" );
        sb.append( TAB + TAB + TAB + getVariablesList() + "\n" + TAB + TAB
                + ")\n" + TAB + TAB + "values\n" + TAB + TAB + "(\n" );
        sb.append( TAB + TAB + TAB + getjavaList() + "\n" );
        sb.append( TAB + TAB + ")\n" );
        sb.append( TAB + "</insert>\n" );

        return sb.toString();
    }

    private String createGetListBy() {
        StringBuilder sb = new StringBuilder();

        for ( Column column : searchableColumns ) {
            sb.append( "\n" );
            sb.append( TAB );
            sb.append( "<select id=\"getListBy" );
            sb.append( toCamelCase( column.getFldName() ) );
            sb.append( "\" parameterType=\"" );
            sb.append( column.getFldType().toLowerCase() );
            sb.append( "\" resultMap=\"" );
            sb.append( table.getDomName() );
            sb.append( "Mapper\">\n" );

            sb.append( TAB );
            sb.append( TAB );
            sb.append( "select * from " );
            sb.append( table.getTableName() );

            sb.append( "\n" );
            sb.append( TAB );
            sb.append( TAB );

            switch (databaseType) {
            case MYSQL:
                sb.append( "where `" );
                sb.append( column.getColName() );
                sb.append( "` = #{" );
                sb.append( column.getFldName() );
                sb.append( "}\n" );
                break;

            case H2:
            case ORACLE:
                sb.append( "where " );
                sb.append( column.getColName() );
                sb.append( " = #{" );
                sb.append( column.getFldName() );
                sb.append( "}\n" );
            }

            sb.append( TAB );
            sb.append( "</select>" );

        }
        sb.append( "\n" );

        return sb.toString();
    }

    private String createUpdate() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" );
        sb.append( "\n" + TAB + "<update id=\"update\" parameterType=\""
                + table.getPackage() + ".domain." + table.getDomName()
                + "\">\n" );
        sb.append( TAB + TAB + "update " + table.getTableName().toUpperCase()
                + " set\n" );
        sb.append( getCombinedList() + "\n" );

        switch (databaseType) {
        case MYSQL:
            sb.append( TAB + TAB + "WHERE `" + sqlVariables.get( 0 ) + "` = #{"
                    + javaVariables.get( 0 ) + "}\n" );
            break;
        case H2:
        case ORACLE:
            sb.append( TAB + TAB + "where " + sqlVariables.get( 0 ) + " = #{"
                    + javaVariables.get( 0 ) + "}\n" );
        }

        sb.append( TAB + "</update>" );

        return sb.toString();
    }

    private String createDelete() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" );
        if ( ( table.getColumn( 0 ).getFldType().equalsIgnoreCase( "string" ) ) ) {
            sb.append( "\n" + TAB + "<delete id=\"delete\" parameterType=\""
                    + table.getColumn( 0 ).getFldType().toLowerCase() + "\">\n" );
        }
        else {
            sb.append( "\n" + TAB + "<delete id=\"delete\" parameterType=\"_"
                    + table.getColumn( 0 ).getFldType().toLowerCase() + "\">\n" );
        }

        sb.append( TAB + TAB + "delete from "
                + table.getTableName().toUpperCase() + "\n" );

        switch (databaseType) {
        case MYSQL:
            sb.append( TAB + TAB + "where `" + sqlVariables.get( 0 ) + "` = #{"
                    + javaVariables.get( 0 ) + "}\n" );
            break;
        case H2:
        case ORACLE:
            sb.append( TAB + TAB + "where " + sqlVariables.get( 0 ) + " = #{"
                    + javaVariables.get( 0 ) + "}\n" );
        }
        sb.append( TAB + "</delete>" );

        return sb.toString();
    }

    private String createProtected() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n\n\n    <!-- " );
        sb.append( PROTECTED_CODE );
        sb.append( " -->\n" );

        List<String> lines = getProtectedLines( filePath );

        // no protected code, close the mapper
        if ( lines.isEmpty() ) {
            sb.append( "\n\n</mapper>" );
        }
        else {
            for ( String str : lines )
                sb.append( str );
        }

        return sb.toString();
    }

    private String getVariablesList() {
        StringBuilder list = new StringBuilder();

        for ( int i = 0; i < sqlVariables.size(); i++ ) {
            if ( i % 5 == 0 && i != 0 )
                list.append( "\n" + TAB + TAB + TAB );

            switch (databaseType) {
            case MYSQL:
                list.append( "`" + sqlVariables.get( i ) + "`" );
                break;
            case H2:
            case ORACLE:
                list.append( sqlVariables.get( i ) );
            }
            if ( i != sqlVariables.size() - 1 )
                list.append( " , " );
        }

        return list.toString();
    }

    private String getjavaList() {
        StringBuilder list = new StringBuilder();

        for ( int i = 0; i < javaVariables.size(); i++ ) {
            if ( i % 5 == 0 && i != 0 )
                list.append( "\n" + TAB + TAB + TAB );
            list.append( "#{" );
            list.append( javaVariables.get( i ) );
            list.append( "}" );
            if ( i != javaVariables.size() - 1 )
                list.append( " , " );
        }

        return list.toString();
    }

    private String getCombinedList() {
        StringBuilder total = new StringBuilder();

        for ( int i = 0; i < javaVariables.size(); i++ ) {
            StringBuilder list = new StringBuilder();

            switch (databaseType) {
            case MYSQL:
                list.append( "`" + sqlVariables.get( i ) + "`" );
                break;
            case H2:
            case ORACLE:
                list.append( sqlVariables.get( i ) );
            }

            list.append( makeSpace( 35, list.toString() ) );
            list.append( "= #{" + javaVariables.get( i ) + "}" );
            if ( i != javaVariables.size() - 1 ) {
                list.append( " , " );
                list.append( "\n" );
            }
            total.append( TAB + TAB + TAB + list );
        }

        return total.toString();
    }

    private String toCamelCase( String value ) {
        String line = value;

        line = line.substring( 0, 1 ).toUpperCase() + line.substring( 1 );

        return line;
    }
}
