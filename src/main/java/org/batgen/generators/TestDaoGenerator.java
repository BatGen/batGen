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

import java.util.Random;

import org.batgen.Column;
import org.batgen.LengthColumn;
import org.batgen.Table;

public class TestDaoGenerator extends Generator {

    private int     countRead        = 0, countLoop = 0, countDelete = 1;
    private String  daoName          = "";
    private boolean getCreatedCalled = false;
    private String  filePath;
    private String  keyName;

    public TestDaoGenerator( Table table ) {
        super( table );
        this.daoName = "Test" + table.getDomName() + "Dao";
        filePath = "src/test/java/" + packageToPath() + "/dao/" + daoName + ".java";
        for ( Column column : table.getColumns() ) {
            if ( column.isKey() ) {
                keyName = table.getColumn( 0 ).getFldName();
                keyName = toTitleCase( keyName );
                break;
            }
        }
    }

    public String createTestDao() {
        StringBuilder sb = new StringBuilder();

        sb.append( createHeading() );
        sb.append( createImports() );
        sb.append( createClassHeader() );
        sb.append( createStaticVariables() );
        sb.append( createTest() );
        sb.append( createMethod( table.getDomName() ) );
        sb.append( createCompareRecordsMethod( table.getDomName() ) );
        sb.append( createModifyRecordsMethod( table.getDomName() ) );
        sb.append( createRandomNumber() );
        sb.append( createRandomString() );
        sb.append( createRandomByte() );
        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );

        return filePath;
    }

    private String createHeading() {
        return "package " + pkg + ".dao;\n\n";
    }

    private String createImports() {

        ImportGenerator imports = new ImportGenerator( filePath );
        imports.addImport( "import static org.junit.Assert.*;" );

        if ( searchTableColumns( "Date" ) ) {
            imports.addImport( "import java.util.Date;" );
        }

        imports.addImport( "import java.util.Random;" );

        if ( hasSearch ) {
            imports.addImport( "import java.util.List;" );
        }

        imports.addImport( "import org.junit.*;" );
        imports.addImport( "import org.apache.ibatis.session.SqlSession;" );
        imports.addImport( "import " + pkg + ".domain.*;" );

        return imports.toString();
    }

    private String createClassHeader() {
        return "public class Test" + table.getDomName() + "Dao {\n\n";
    }

    private String createStaticVariables() {
        StringBuilder sb = new StringBuilder();

        sb.append( TAB + "private static StringBuilder sb = new StringBuilder();\n" );
        sb.append( TAB
                + "private static String chars = \"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890\";\n" );
        sb.append( TAB + "private static Random random = new Random();\n" );

        return sb.toString();
    }

    private String createTest() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "@Before\n" );
        sb.append( TAB + "public void setup() {\n" );
        sb.append( TAB + TAB + "SessionFactory.initializeForTest();\n" );
        sb.append( TAB + "}\n" );

        sb.append( "\n" + TAB + "@Test\n" );
        sb.append( TAB + "public void test() throws Exception {\n" );
        sb.append( "\n" + TAB + TAB + "SqlSession session = SessionFactory.getSession();\n" );
        sb.append( TAB + TAB + table.getDomName() + "Dao " + toJavaCase( table.getDomName() )
                + "Dao = session.getMapper( " + table.getDomName() + "Dao.class );\n" );
        sb.append( "\n" + TAB + TAB + "try {\n" );
        sb.append( getCreate( table.getDomName() ) );
        sb.append( getRead( table.getDomName() ) );
        sb.append( getCompareRecord( table.getDomName() ) );
        sb.append( getListBy( table.getDomName() ) );
        sb.append( getModifyRecord( table.getDomName() ) );
        sb.append( getRead( table.getDomName() ) );
        sb.append( getCompareRecord( table.getDomName() ) );
        sb.append( getDeleteRecord( table.getDomName() ) );
        sb.append( getDeleteVerified( table.getDomName() ) );

        sb.append( "\n" + TAB + TAB + "} finally {\n" );
        sb.append( TAB + TAB + TAB + "if ( session != null ) {\n" );
        sb.append( TAB + TAB + TAB + TAB + "session.rollback();\n" );
        sb.append( TAB + TAB + TAB + TAB + "session.close();\n" + TAB + TAB + TAB + "}\n" + TAB + TAB + "}\n" + TAB
                + "}\n" );
        return sb.toString();
    }

    private String getCreate( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + TAB + TAB + variable + " " + toJavaCase( variable ) + " = Test" + variable
                + "Dao.create" + variable + "();\n" );

        if ( !getCreatedCalled ) {
            sb.append( "\n" + TAB + TAB + TAB + "int count = " + toJavaCase( variable ) + "Dao.create( "
                    + toJavaCase( variable ) + " );\n" );
            getCreatedCalled = true;
        }
        else {
            sb.append( "\n" + TAB + TAB + TAB + "count = " + toJavaCase( variable ) + "Dao.create( "
                    + toJavaCase( variable ) + " );\n" );
        }
        sb.append( TAB + TAB + TAB + getAssertEquals() );
        sb.append( TAB + TAB + TAB + getNotNullEquals( variable, variable ) );
        return sb.toString();
    }

    private String getRead( String variable ) {
        StringBuilder sb = new StringBuilder();

        if ( countRead == 0 ) {
            sb.append( "\n" + TAB + TAB + TAB + variable + " readRecord = " + toJavaCase( variable ) + "Dao.read( "
                    + toJavaCase( variable ) + ".get" + keyName + "() );\n" );
            sb.append( TAB + TAB + TAB + getNotNullEquals( "readRecord", variable ) );

            if ( variable.equalsIgnoreCase( table.getDomName() ) ) {
                countRead++;
            }
        }
        else if ( variable.equalsIgnoreCase( table.getDomName() ) ) {
            sb.append( "\n" + TAB + TAB + TAB + "readRecord = " + toJavaCase( variable ) + "Dao.read( "
                    + toJavaCase( variable ) + ".get" + keyName + "() );\n" );
            sb.append( TAB + TAB + TAB + getNotNullEquals( "readRecord", variable ) );
        }
        return sb.toString();
    }

    private String getCompareRecord( String variable ) {
        return "\n" + TAB + TAB + TAB + "compareRecords( " + toJavaCase( variable ) + ", readRecord );\n";
    }

    private String getModifyRecord( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + TAB + TAB + "modifyRecord( " + toJavaCase( variable ) + " );\n" );

        sb.append( TAB + TAB + TAB + "count = " + toJavaCase( variable ) + "Dao.update( " + toJavaCase( variable )
                + " );\n" );
        sb.append( TAB + TAB + TAB + getAssertEquals() );
        return sb.toString();
    }

    private String getDeleteRecord( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + TAB + TAB + "count = " + toJavaCase( variable ) + "Dao.delete( "
                + toJavaCase( variable ) + ".get" + keyName + "() );\n" );
        sb.append( TAB + TAB + TAB + getAssertEquals() );
        return sb.toString();
    }

    private String getDeleteVerified( String variable ) {
        StringBuilder sb = new StringBuilder();

        if ( variable.equalsIgnoreCase( table.getDomName() ) ) {
            sb.append( "\n" + TAB + TAB + TAB + "readRecord = " + toJavaCase( variable ) + "Dao.read( "
                    + toJavaCase( variable ) + ".get" + keyName + "() );\n" );
            sb.append( TAB + TAB + TAB + getNullEquals( "readRecord" ) );

        }
        else {
            sb.append( "\n" + TAB + TAB + TAB + "readRecord" + countDelete + " = " + toJavaCase( variable )
                    + "Dao.read( " + toJavaCase( variable ) + ".get" + keyName + "() );\n" );
            sb.append( TAB + TAB + TAB + getNullEquals( "readRecord" + countDelete ) );
            countDelete++;
        }
        return sb.toString();
    }

    private String getListBy( String variable ) {
        StringBuilder sb = new StringBuilder();
        if ( hasSearch ) {
            for ( int i = 1; i < searchList.size() + 1; i++ ) {

                sb.append( "\n" + TAB + TAB + TAB + "List<" + variable + "> list" + i + "= " );
                sb.append( toJavaCase( variable ) + "Dao.getListBy" + toTitleCase( searchList.get( i - 1 ) ) );
                sb.append( "( " + toJavaCase( variable ) + ".get" + toTitleCase( searchList.get( i - 1 ) ) );
                sb.append( "() ) ; \n" );

                sb.append( TAB + TAB + TAB + "assertEquals( 1, list" + i + ".size() );\n" );

                sb.append( TAB + TAB + TAB + "compareRecords( " + toJavaCase( variable ) );
                sb.append( ", list" + i + ".get( 0 ) );\n" );
            }
        }
        return sb.toString();
    }

    private String createMethod( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "public static " + variable + " create" + variable + "() {\n" );
        sb.append( TAB + TAB + variable + " " + toJavaCase( variable ) + " = new " + variable + "();\n\n" );
        sb.append( getColumnContextSet( variable, false ) );
        sb.append( "\n" + TAB + TAB + "return " + toJavaCase( variable ) + ";\n" + TAB + "}" );
        return sb.toString();
    }

    private String createCompareRecordsMethod( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n\n" + TAB + "public static void compareRecords( " + variable + " " + toJavaCase( variable )
                + ", " + variable + " readRecord ) {\n\n" );

        for ( Column column : table.getColumns() ) {
            if ( column.isKey() ) {
                continue;
            }
            if ( column.getClass().getSimpleName().equals( "VirtualStringColumn" ) ) {
                continue;
            }
            else if ( column.getSqlType().equalsIgnoreCase( "DATE" ) || column.getSqlType().equalsIgnoreCase( "BLOB" )
                    || column.getSqlType().equalsIgnoreCase( "CLOB" ) ) {
                sb.append( TAB + TAB + "assertNotSame( " + toJavaCase( variable ) + ".get"
                        + toCamelCase( column.getFldName() ) + "(), readRecord.get" + toCamelCase( column.getFldName() )
                        + "() );\n" );

            }
            else {
                sb.append( TAB + TAB + "assertEquals( " + toJavaCase( variable ) + ".get"
                        + toCamelCase( column.getFldName() ) + "(), readRecord.get" + toCamelCase( column.getFldName() )
                        + "() );\n" );
            }
        }

        sb.append( "\n" + TAB + "}\n" );
        return sb.toString();
    }

    private String createModifyRecordsMethod( String variable ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "\n" + TAB + "public static void modifyRecord( " + variable + " " + toJavaCase( variable )
                + " ) {\n\n" );

        sb.append( getColumnContextSet( variable, true ) );
        sb.append( "\n" + TAB + "}\n" );
        return sb.toString();
    }

    private String createRandomNumber() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "public static int randomNumber() {\n\n" );
        sb.append( TAB + "" + TAB + "return (int) ( Math.random() * 10 ) + 0;\n\n" );
        sb.append( TAB + "}\n" );
        return sb.toString();
    }

    private String createRandomString() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "public static String randomString( String fldName, int length ) {\n\n" );
        sb.append( TAB + TAB + "if ( fldName.length() >= length ) {\n" );
        sb.append( TAB + TAB + TAB + "return fldName.substring( 0, length );\n" );
        sb.append( TAB + TAB + "}\n\n" );
        sb.append( TAB + TAB + "sb.setLength( 0 );\n" );
        sb.append( TAB + TAB + "sb.append( fldName );\n" );
        sb.append( TAB + TAB + "for ( int i = fldName.length(); i < length; i++ ) {\n" );
        sb.append( TAB + TAB + TAB + "sb.append( chars.charAt( random.nextInt( chars.length() ) ) );\n" );
        sb.append( TAB + TAB + "}\n" );
        sb.append( TAB + TAB + "return sb.toString();\n" );
        sb.append( TAB + "}\n" );
        return sb.toString();
    }

    private String createRandomByte() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "public static byte[] randomByteArray( int length ) {\n\n" );
        sb.append( TAB + TAB + "byte[] byteArray = new byte[length];\n" );
        sb.append( TAB + TAB + "random.nextBytes( byteArray );\n" );
        sb.append( TAB + TAB + "return byteArray;\n" );
        sb.append( TAB + "}\n" );
        return sb.toString();
    }

    private String getColumnContextSet( String variable, Boolean modifiy ) {
        StringBuilder sb = new StringBuilder();

        for ( Column column : table.getColumns() ) {
            if ( column.getClass().getSimpleName().equals( "VirtualStringColumn" ) ) {
                continue;
            }
            if ( column.isKey() && modifiy){
            	continue;
            }

            if ( column.getColName().equalsIgnoreCase( "Key" ) ) {
                if ( column.getFldType().equalsIgnoreCase( "string" ) ) {
                    countLoop++;
                    if ( column.getClass().getSimpleName().equals( "LengthColumn" ) && ( !( countLoop % 2 == 0 ) ) ) {
                        LengthColumn c = (LengthColumn) column;
                        sb.append( TAB + TAB + toJavaCase( variable ) + ".set" + toCamelCase( column.getFldName() )
                                + "( " );
                        sb.append( " randomString( \"" + column.getFldName() + "\", " + c.getColLen() + " ) );\n" );
                    }
                    else {
                        continue;
                    }
                }
                else {
                    continue;
                }
            }

            sb.append( TAB + TAB + toJavaCase( variable ) + ".set" + toCamelCase( column.getFldName() ) + "(" );

            if ( column.getFldType().equalsIgnoreCase( "Boolean" ) ) {
                sb.append( " true " );
            }
            else if ( column.getFldType().equalsIgnoreCase( "String" ) ) {
                if ( column.getClass().getSimpleName().equals( "LengthColumn" ) ) {
                    LengthColumn c = (LengthColumn) column;
                    sb.append( " randomString( \"" + column.getFldName() + "\", " + c.getColLen() + " )" );
                }
                else if ( column.getSqlType().equalsIgnoreCase( "CLOB" ) ) {
                    sb.append( " randomString( \"" + column.getFldName() + "\", "
                            + ( 1 + ( new Random() ).nextInt( 10 ) ) + " )" );
                }
            }
            else if ( column.getFldType().equalsIgnoreCase( "Date" ) ) {
                sb.append( " new Date()" );
            }
            else if ( column.getFldType().equalsIgnoreCase( "byte[]" ) ) {
                sb.append( " randomByteArray( 10 )" );
            }
            else if ( column.getFldType().equalsIgnoreCase( "Double" ) ) {
                sb.append( " (double) randomNumber()" );
            }
            else if ( column.getFldType().equalsIgnoreCase( "Integer" ) ) {
                sb.append( " randomNumber()" );
            }
            else if ( column.getFldType().equalsIgnoreCase( "Long" ) ) {
                sb.append( " (long) 0" );
            }
            sb.append( " );\n" );
        }
        return sb.toString();
    }

    private boolean searchTableColumns( String keyword ) {
        for ( Column column : table.getColumns() ) {
            if ( column.getFldType().equalsIgnoreCase( keyword ) ) {
                return true;
            }
        }
        return false;
    }

    private String getAssertEquals() {
        return "assertEquals( 1, count );\n";
    }

    private String getNotNullEquals( String variable, String type ) {
        return "assertNotNull( " + toJavaCase( variable ) + ".get" + keyName + "() );\n";
    }

    private String getNullEquals( String variable ) {
        return "assertNull( " + toJavaCase( variable ) + " );\n";
    }

    private String toJavaCase( String value ) {
        String line = value;

        if ( value.length() > 0 )
            line = line.substring( 0, 1 ).toLowerCase() + line.substring( 1 );
        return line;
    }

    private String toCamelCase( String value ) {
        String line = value;

        line = line.substring( 0, 1 ).toUpperCase() + line.substring( 1 );
        return line;
    }
}
