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

import org.batgen.*;

import static org.batgen.generators.GenUtil.*;

/**
 * This code generates the Business Objects.
 *
 */
public class BoGenerator extends Generator {
    String boName = "";
    StringBuilder sb = new StringBuilder();
    final String NEWLINE = "\n";
    Column keyColumn;
    String filePath;

    public BoGenerator( Table table ) {
        super( table );
        boName = table.getDomName() + "Bo";
        filePath = "src/main/java/" + packageToPath() + "/bo/" + boName
                + ".java";

        for ( Column column : table.getColumns() ) {
            if ( column.isKey() ) {
                keyColumn = column;
                break;
            }
        }
    }

    public String createBo() {
        writePkg();
        writeImport();
        writeClass();
        writeList();
        sb.append( getProtectedJavaLines( filePath ) );
        writeToFile( filePath, sb.toString() );
        createBoException();

        return filePath;
    }

    private void writeClass() {
        String boName = table.getDomName() + "Bo";

        write( "public class " );
        write( boName );
        write( " { \n" );
        write( NEWLINE );
        write( TAB );
        write( "private static " );
        write( boName );
        write( " instance = new " );
        write( boName + "(); \n" );
        write( NEWLINE );
        write( TAB );
        write( "public static " );
        write( boName );
        write( " getInstance() {\n" );
        write( TAB + TAB + "return instance;\n" );
        write( TAB + "} \n" );
        write( NEWLINE );
        write( TAB );
        write( "private " + boName + "() {\n" + TAB + "} \n\n" );

        writeCrud();
    }

    void writeCrud() {
        write( TAB + "public int create( " );
        write( table.getDomName() );
        write( " value ) throws BoException {\n" );
        writeMethodBody( "create" );

        write( NEWLINE );
        write( TAB + "public int update( " );
        write( table.getDomName() );
        write( " value ) throws BoException {\n" );
        writeMethodBody( "update" );

        write( NEWLINE );
        write( TAB );
        write( "public int delete( " );
        write( keyColumn.getFldType() );
        write( " value ) throws BoException {\n" );
        writeMethodBody( "delete" );
        write( NEWLINE );

        write( TAB );
        write( "public " );
        write( table.getDomName() );
        write( " read( " );
        write( keyColumn.getFldType() );
        write( " value ) throws BoException {\n" );
        writeMethodBody( "read" );
        write( NEWLINE );

    }

    private void writeMethodBody( String type ) {
        String mapperName = table.getDomName() + "Dao";
        write( TAB + TAB + "SqlSession session = null; \n" );

        if ( type.equalsIgnoreCase( "read" ) ) {
            write( TAB + TAB + table.getDomName() + " result; \n" );
        }
        else {
            write( TAB + TAB + "int result = 0; \n" );
        }

        write( NEWLINE );
        write( TAB + TAB + "try {\n" );
        write( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );

        write( TAB + TAB + TAB + mapperName );
        write( " mapper = session.getMapper( " + mapperName + ".class );\n" );
        write( TAB + TAB + TAB + "result = mapper." + type + "( value );\n" );
        write( TAB + TAB + TAB + "session.commit();\n\n" );

        write( TAB + TAB + "} catch ( Exception e ) {\n" );
        write( TAB + TAB + TAB + "session.rollback();\n" );
        write( TAB + TAB + TAB + "throw new BoException( e );\n\n" );

        write( TAB + TAB + "} finally { \n" );
        write( TAB + TAB + TAB + "if ( session != null ) \n" );
        write( TAB + TAB + TAB + TAB + "session.close();\n" );
        write( TAB + TAB + "}\n\n" );
        write( TAB + TAB + "return result;\n" );
        write( TAB + "}\n" );
    }

    void writeList() {
        String mapperName = table.getDomName() + "Dao";

        for ( Column column : table.getColumns() ) {
            if ( column.isSearchId() ) {
                // do this to make sure the created method is properly
                // camelCased
                String fieldName = column.getFldName();
                fieldName = toTitleCase( fieldName );

                write( TAB );
                write( "public " );
                write( "List<" + table.getDomName() + "> getListBy" );
                write( fieldName );
                write( "( " );
                write( column.getFldType() );
                write( " key ) throws BoException {\n" );

                write( TAB + TAB + "SqlSession session = null; \n" );
                write( TAB + TAB + "List<" + table.getDomName() + "> list;\n\n" );

                write( TAB + TAB + "try {\n" );
                write( TAB + TAB + TAB
                        + "session = SessionFactory.getSession();\n" );

                write( TAB + TAB + TAB + mapperName );
                write( " mapper = session.getMapper( " + mapperName
                        + ".class );\n" );
                write( TAB + TAB + TAB + "list = mapper." );
                write( "getListBy" );
                write( fieldName );
                write( "( key );\n" );
                write( TAB + TAB + TAB + "session.commit();\n\n" );

                write( TAB + TAB + "} catch ( Exception e ) {\n" );
                write( TAB + TAB + TAB + "session.rollback();\n" );
                write( TAB + TAB + TAB + "throw new BoException( e );\n\n" );

                write( TAB + TAB + "} finally { \n" );
                write( TAB + TAB + TAB + "if ( session != null ) \n" );
                write( TAB + TAB + TAB + TAB + "session.close(); \n" );
                write( TAB + TAB + "}\n\n" );
                write( TAB + TAB + "return list;\n" );
                write( TAB + "}\n\n" );
            }
        }
    }

    private void writeImport() {
        ImportGenerator imports = new ImportGenerator( filePath );
        if ( hasSearch )
            imports.addImport( "import java.util.List;" );

        imports.addImport( "import org.apache.ibatis.session.*;" );
        imports.addImport( "import " + table.getPackage() + ".dao.*;" );
        imports.addImport( "import " + table.getPackage() + ".domain."
                + table.getDomName() + ";" );
        imports.addImport( "import " + table.getPackage() + ".util."
                + "BoException;" );

        write( imports.toString() );

    }

    private void createBoException() {
        sb = new StringBuilder();
        String filePath = "src/main/java/" + packageToPath()
                + "/util/BoException.java";

        sb.append( "package " );
        sb.append( table.getPackage() );
        sb.append( ".util;\n\n" );

        sb.append( "public class BoException extends Exception {\n" );

        sb.append( TAB );
        sb.append( "private static final long serialVersionUID = 1L;\n\n" );

        sb.append( TAB );
        sb.append( "public BoException( Throwable e ) {\n" );

        sb.append( TAB ).append( TAB );
        sb.append( "super( e );\n" );

        sb.append( TAB );
        sb.append( "}\n\n" );

        sb.append( TAB );
        sb.append( "public BoException( String msg ) {\n" );

        sb.append( TAB ).append( TAB );
        sb.append( "super( msg );\n" );

        sb.append( TAB );
        sb.append( "}\n\n" );

        sb.append( TAB );
        sb.append( "public BoException( String msg, Throwable e ) {\n" );

        sb.append( TAB ).append( TAB );
        sb.append( "super( msg, e );\n" );

        sb.append( TAB );
        sb.append( "}\n\n" );

        sb.append( getProtectedJavaLines( filePath ) );
        writeToFile( filePath, sb.toString() );
    }

    private void writePkg() {
        write( "package " + table.getPackage() + ".bo" + ";\n\n" );
    }

    private void write( String str ) {
        sb.append( str );
    }
}
