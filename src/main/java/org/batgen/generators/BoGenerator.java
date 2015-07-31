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

import org.batgen.Column;
import org.batgen.IndexNode;
import org.batgen.Table;

/**
 * This code generates the Business Objects.
 * 
 */
public class BoGenerator extends Generator {
    String boName = "";
    StringBuilder sb = new StringBuilder();

    String filePath;

    final String NEWLINE = "\n";
    private final String IMPORT_DATE = "import java.util.Date;";

    public BoGenerator( Table table ) {
        super( table );
        boName = table.getDomName() + "Bo";
        filePath = "src/main/java/" + packageToPath() + "/bo/" + boName + ".java";
    }

    public String createBo() {
        writePkg();
        writeImport();
        writeClass();
        writeList();
        writeManyManyJoin();
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
        for ( Column col : table.getKeyColumns() ) {
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
        write( " read( " + param );
        ;
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
        } else {
            write( TAB + TAB + "int result = 0;\n" );
        }

        write( NEWLINE );

        write( TAB + TAB + table.getDomName() + " param = new " + table.getDomName() + "();"
                + NEWLINE + NEWLINE );

        for ( Column col : table.getKeyColumns() ) {
            String paramCamel = col.getFldName().substring( 0, 1 ).toUpperCase()
                    + col.getFldName().substring( 1 );
            write( TAB + TAB + "param.set" + paramCamel + "( " + col.getFldName() + " );" + NEWLINE );
        }

        write( NEWLINE );
        write( TAB + TAB + "try {\n" );
        write( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );

        write( TAB + TAB + TAB + mapperName );
        write( " mapper = session.getMapper( " + mapperName + ".class );\n" );
        write( TAB + TAB + TAB + "result = mapper." + type + "( " );
        write( "param );" );

        write( NEWLINE );

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
            String methodName = "readByIndex" + toTitleCase( node.getIndexName() );
            String param = "";
            for ( Column col : node.getColumnList() ) {
                param += col.getFldType() + " " + col.getFldName() + ", ";
            }
            param = param.substring( 0, param.length() - 2 );
            sb.append( TAB + "public " + table.getDomName() + " " + methodName + "( " );
            sb.append( param + " ) throws BoException" );
            sb.append( "{\n" );

            sb.append( TAB + TAB + "SqlSession session = null;\n" );
            sb.append( TAB + TAB + table.getDomName() + " result;\n" );

            sb.append( TAB + TAB + "try {\n" );
            sb.append( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );
            sb.append( TAB + TAB + TAB + mapperName );
            sb.append( " mapper = session.getMapper( " + mapperName + ".class );\n" );
            param = "";
            for ( Column col : node.getColumnList() ) {
                param += col.getFldName() + ", ";
            }
            param = param.substring( 0, param.length() - 2 );
            sb.append( TAB + TAB + TAB + "result = mapper." + methodName + "( " + param + " );\n" );
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

    /**
     * Generate the getTable1ListByTable2Key and getTable2ListByTable1Key BO
     * methods for the many-to-many link tables
     */
    private void writeManyManyJoin() {

        if ( !table.isManyToMany() )
            return;

        String mapperName = table.getDomName() + "Dao";

        Table one = table.getTableOne();
        Table two = table.getTableTwo();

        // First join method - getTable2ListByTable1Key

        write( TAB );
        write( "public " );
        write( "List<" + two.getDomName() + "> get" );
        write( two.getDomName() + "ListBy" + one.getDomName() + "Key");
        write( "( " );

        String param = "";
        for ( Column col : table.getTableOne().getKeyColumns() ) {
            param += col.getFldType() + " " + col.getFldName() + ", ";
        }
        param = param.substring( 0, param.length() - 2 );
        sb.append( param );

        write( " ) throws BoException {\n" );

        write( TAB + TAB + "SqlSession session = null;\n" );
        write( TAB + TAB + "List<" + two.getDomName() + "> list;\n\n" );

        write( TAB + TAB + one.getDomName() + " param = new " + one.getDomName() + "();"
                + NEWLINE + NEWLINE );

        for ( Column col : one.getKeyColumns() ) {
            String paramCamel = col.getFldName().substring( 0, 1 ).toUpperCase()
                    + col.getFldName().substring( 1 );
            write( TAB + TAB + "param.set" + paramCamel + "( " + col.getFldName() + " );" + NEWLINE );
        }
        
        write( NEWLINE );
        
        write( TAB + TAB + "try {\n" );
        write( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );

        write( TAB + TAB + TAB + mapperName );
        write( " mapper = session.getMapper( " + mapperName + ".class );\n" );
        write( TAB + TAB + TAB + "list = mapper.get" );
        sb.append( two.getDomName() );
        sb.append( "ListBy" );
        sb.append( one.getDomName() + "Key" );
        write( "( " );

        write ( "param" );

        write( " );\n" );
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

        // Second join method - getTable1ListByTable2Key

        write( TAB );
        write( "public " );
        write( "List<" + one.getDomName() + "> get" );
        write( one.getDomName() + "ListBy" + two.getDomName() + "Key" );
        write( "( " );

        param = "";
        for ( Column col : table.getTableTwo().getKeyColumns() ) {
            param += col.getFldType() + " " + col.getFldName() + ", ";
        }
        param = param.substring( 0, param.length() - 2 );
        sb.append( param );

        write( " ) throws BoException {\n" );

        write( TAB + TAB + "SqlSession session = null;\n" );
        write( TAB + TAB + "List<" + one.getDomName() + "> list;\n\n" );

        write( TAB + TAB + two.getDomName() + " param = new " + two.getDomName() + "();"
                + NEWLINE + NEWLINE );

        for ( Column col : two.getKeyColumns() ) {
            String paramCamel = col.getFldName().substring( 0, 1 ).toUpperCase()
                    + col.getFldName().substring( 1 );
            write( TAB + TAB + "param.set" + paramCamel + "( " + col.getFldName() + " );" + NEWLINE );
        }
        
        write( NEWLINE );
        
        write( TAB + TAB + "try {\n" );
        write( TAB + TAB + TAB + "session = SessionFactory.getSession();\n" );

        write( TAB + TAB + TAB + mapperName );
        write( " mapper = session.getMapper( " + mapperName + ".class );\n" );
        write( TAB + TAB + TAB + "list = mapper.get" );
        sb.append( one.getDomName() );
        sb.append( "ListBy" );
        sb.append( two.getDomName() + "Key" );
        write( "( " );

        write ( "param" );

        write( " );\n" );
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

        imports.addImport( "import org.apache.ibatis.session.*;" );
        imports.addImport( "import " + table.getPackage() + ".dao.*;" );
        if ( hasJoin ) {
            imports.addImport( "import " + table.getPackage() + ".domain.*;" );
        } else {
            imports.addImport( "import " + table.getPackage() + ".domain." + table.getDomName()
                    + ";" );
        }

        imports.addImport( "import " + table.getPackage() + ".util." + "BoException;" );

        write( imports.toString() );

    }

    private void createBoException() {
        sb = new StringBuilder();
        String filePath = "src/main/java/" + packageToPath() + "/util/BoException.java";

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
