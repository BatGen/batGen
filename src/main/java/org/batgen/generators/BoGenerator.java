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

import org.batgen.Column;
import org.batgen.IndexNode;
import org.batgen.Table;

/**
 * This code generates the Business Objects.
 * 
 */
public class BoGenerator extends Generator {
    String        boName  = "";
    StringBuilder sb      = new StringBuilder();
    final String  NEWLINE = "\n";
    private List<Column> keyColumns = new ArrayList<Column>();
    String        filePath;
    String        basePackage;

    public BoGenerator( Table table, String basePackage ) {
        super( table );
        this.basePackage = basePackage;
        boName = table.getDomName() + "Bo";
        filePath = "src/main/java/" + packageToPath() + "/bo/" + boName + ".java";

        for ( Column column : table.getColumns() ) {
            if ( column.isKey() ) {
                keyColumns.add(column);
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
        write( " {\n" );
        write( NEWLINE );
        write( TAB );
        write( "private static " );
        write( boName );
        write( " instance = new " );
        write( boName + "();\n" );
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
        writeMethodBodyCreateUpdate( "create" );

        write( NEWLINE );
        write( TAB + "public int update( " );
        write( table.getDomName() );
        write( " value ) throws BoException {\n" );
        writeMethodBodyCreateUpdate( "update" );
        
        String param = "";
        for(Column col : keyColumns){
        	param += col.getFldType() + " " + col.getFldName() + ", ";
        }
        param = param.substring( 0, param.length() - 2 );

        write( NEWLINE );
        write( TAB );
        write( "public int delete( " );
        write( param + " ) throws BoException {\n" );
        writeMethodBodyReadDelete( "delete" );
        write( NEWLINE );

        write( TAB );
        write( "public " );
        write( table.getDomName() );
        write( " read( " + param);;
        write( " ) throws BoException {\n" );
        writeMethodBodyReadDelete( "read" );
        write( NEWLINE );

        if ( !table.getIndexList().isEmpty() ) {
        	writeIndexKeys();
        }

    }

    private void writeMethodBodyCreateUpdate( String type ) {
        String mapperName = table.getDomName() + "Dao";
        write( TAB + TAB + "SqlSession session = null;\n" );
        write( TAB + TAB + "int result = 0;\n" );

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
        write( TAB + TAB + TAB + "if ( session != null )\n" );
        write( TAB + TAB + TAB + TAB + "session.close();\n" );
        write( TAB + TAB + "}\n\n" );
        write( TAB + TAB + "return result;\n" );
        write( TAB + "}\n" );
    }
    
    private void writeMethodBodyReadDelete( String type ) {
        String mapperName = table.getDomName() + "Dao";
        write( TAB + TAB + "SqlSession session = null;\n" );

        if ( type.equalsIgnoreCase( "read" ) ) {
            write( TAB + TAB + table.getDomName() + " result;\n" );
        }
        else {
            write( TAB + TAB + "int result = 0;\n" );
        }

        write( NEWLINE );
        write( TAB + TAB + "try {\n" );
        write( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );

        write( TAB + TAB + TAB + mapperName );
        write( " mapper = session.getMapper( " + mapperName + ".class );\n" );
        write( TAB + TAB + TAB + "result = mapper." + type + "( " );
        
        String param = "";
        for(Column col : keyColumns){
        	param += col.getFldName() + ", ";
        }
        param = param.substring( 0, param.length() - 2 );
        write( param + " );\n" );
        
        write( TAB + TAB + TAB + "session.commit();\n\n" );

        write( TAB + TAB + "} catch ( Exception e ) {\n" );
        write( TAB + TAB + TAB + "session.rollback();\n" );
        write( TAB + TAB + TAB + "throw new BoException( e );\n\n" );

        write( TAB + TAB + "} finally { \n" );
        write( TAB + TAB + TAB + "if ( session != null )\n" );
        write( TAB + TAB + TAB + TAB + "session.close();\n" );
        write( TAB + TAB + "}\n\n" );
        write( TAB + TAB + "return result;\n" );
        write( TAB + "}\n" );
    }

    private void writeIndexKeys() {
        String mapperName = table.getDomName() + "Dao";
        StringBuilder sb = new StringBuilder();

        for ( IndexNode node : table.getIndexList() ) {
        	String methodName = "readByIndex" + toTitleCase( node.getIndexName());
            String param = "";
            for(Column col : node.getColumnList()){
            	param += col.getFldType() + " " + col.getFldName() + ", ";
            }
            param = param.substring( 0, param.length() - 2 );
            sb.append( TAB + "public " + table.getDomName() + " " + methodName + "( "  );
            sb.append( param + " ) throws BoException" );
            sb.append( "{\n" );

            sb.append( TAB + TAB + "SqlSession session = null;\n" );
            sb.append( TAB + TAB + table.getDomName() + " result;\n" );

            sb.append( TAB + TAB + "try {\n" );
            sb.append( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );
            sb.append( TAB + TAB + TAB + mapperName );
            sb.append( " mapper = session.getMapper( " + mapperName + ".class );\n" );
            param = "";
            for(Column col : node.getColumnList()){
            	param += col.getFldName() + ", ";
            }
            param = param.substring( 0, param.length() - 2 );
            sb.append( TAB + TAB + TAB + "result = mapper." + methodName +"( " + param + " );\n" );
            sb.append( TAB + TAB + TAB + "session.commit();\n\n" );
            sb.append( TAB + TAB + "} catch ( Exception e ) {\n" );
            sb.append( TAB + TAB + TAB + "session.rollback();\n" );
            sb.append( TAB + TAB + TAB + "throw new BoException( e );\n\n" );
            sb.append( TAB + TAB + "} finally { \n" );
            sb.append( TAB + TAB + TAB + "if ( session != null )\n" );
            sb.append( TAB + TAB + TAB + TAB + "session.close();\n" );
            sb.append( TAB + TAB + "}\n\n" );
            sb.append( TAB + TAB + "return result;\n" );
            sb.append( TAB + "}\n" );
        }

        write( sb.toString() );
    }

    private void writeList() {
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

                write( TAB + TAB + "SqlSession session = null;\n" );
                write( TAB + TAB + "List<" + table.getDomName() + "> list;\n\n" );

                write( TAB + TAB + "try {\n" );
                write( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );

                write( TAB + TAB + TAB + mapperName );
                write( " mapper = session.getMapper( " + mapperName + ".class );\n" );
                write( TAB + TAB + TAB + "list = mapper." );
                write( "getListBy" );
                write( fieldName );
                write( "( key );\n" );
                write( TAB + TAB + TAB + "session.commit();\n\n" );

                write( TAB + TAB + "} catch ( Exception e ) {\n" );
                write( TAB + TAB + TAB + "session.rollback();\n" );
                write( TAB + TAB + TAB + "throw new BoException( e );\n\n" );

                write( TAB + TAB + "} finally { \n" );
                write( TAB + TAB + TAB + "if ( session != null )\n" );
                write( TAB + TAB + TAB + TAB + "session.close();\n" );
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
        
        imports.addImport( "import " + basePackage + ".SessionFactory;" );
        imports.addImport( "import " + basePackage + ".util." + "BoException;" );
        imports.addImport( "import " + table.getPackage() + ".dao.*;" );
        imports.addImport( "import " + table.getPackage() + ".domain." + table.getDomName() + ";" );

        write( imports.toString() );

    }

    private void createBoException() {
        sb = new StringBuilder();
        String filePath = "src/main/java/" + basePackage.replace( ".", "/" ) + "/util/BoException.java";

        sb.append( "package " );
        sb.append( basePackage );
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
