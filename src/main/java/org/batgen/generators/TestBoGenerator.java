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

import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

public class TestBoGenerator extends Generator {

    private String daoName = "";
    private boolean getCreatedCalled = false;
    private String filePath;

    public TestBoGenerator( Table table ) {
        super( table );
        this.daoName = "Test" + table.getDomName() + "Bo";
        filePath = "src/test/java/" + packageToPath() + "/bo/" + daoName
                + ".java";

    }

    public String createTestBo() {
        StringBuilder sb = new StringBuilder();

        sb.append( createHeading() );
        sb.append( createImports() );
        sb.append( createClassHeader() );
        sb.append( createTest() );
        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );
        return filePath;
    }

    private String createHeading() {
        return "package " + pkg + ".bo;\n\n";
    }

    private String createImports() {

        ImportGenerator imports = new ImportGenerator( filePath );
        imports.addImport( "import static org.junit.Assert.*;" );
        imports.addImport( "import org.junit.*;" );

        if ( hasSearch ) {
            imports.addImport( "import java.util.List;" );
        }

        imports.addImport( "import " + pkg + ".domain.*;" );
        imports.addImport( "import " + pkg + ".dao.*;" );
        imports.addImport( "import " + pkg + ".util.*;" );

        return imports.toString();
    }

    private String createClassHeader() {
        return "public class Test" + table.getDomName() + "Bo {\n";
    }

    private String createTest() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "@Before\n" );
        sb.append( TAB + "public void setup() {\n" );
        sb.append( TAB + TAB + "SessionFactory.initializeForTest();\n" );
        sb.append( TAB + "}\n\n" );

        sb.append( TAB + "@Test\n" );
        sb.append( TAB + "public void test() throws BoException {\n" );

        sb.append( "\n" + TAB + TAB + table.getDomName() + "Bo "
                + toJavaCase( table.getDomName() ) + "Bo = "
                + table.getDomName() + "Bo.getInstance();\n" );

        sb.append( getCreate( table.getDomName() ) );
        sb.append( getRead( table.getDomName() ) );
        sb.append( getCompareRecord( table.getDomName() ) );
        sb.append( getListBy( table.getDomName() ) );
        sb.append( getModifyRecord( table.getDomName() ) );
        sb.append( getDeleteRecord( table.getDomName() ) );
        sb.append( getDeleteVerified( table.getDomName() ) );

        sb.append( "\n" + TAB + "}\n" );

        return sb.toString();
    }

    private String getCreate( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + TAB + variable + " " + toJavaCase( variable )
                + " = " + "Test" + variable + "Dao.create" + variable + "();\n" );

        if ( !getCreatedCalled ) {
            sb.append( TAB + TAB + "int count = " + toJavaCase( variable )
                    + "Bo.create( " + toJavaCase( variable ) + " );\n" );
            getCreatedCalled = true;
        }
        else {
            sb.append( TAB + TAB + "count = " + toJavaCase( variable )
                    + "Bo.create( " + toJavaCase( variable ) + " );\n" );
        }

        sb.append( TAB + TAB + getAssertEquals() );

        return sb.toString();
    }

    private String getRead( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + TAB + variable + " readRecord = "
                + toJavaCase( variable ) + "Bo.read( " + toJavaCase( variable )
                + ".get"
                + toTitleCase( table.getColumn( 0 ).getFldName().toString() )
                + "() );\n" );
        sb.append( TAB + TAB + getNotNullEquals( "readRecord", variable ) );

        return sb.toString();
    }

    private String getCompareRecord( String variable ) {
        return "\n" + TAB + TAB + "Test" + variable + "Dao.compareRecords( "
                + toJavaCase( variable ) + ", readRecord );\n";

    }

    private String getModifyRecord( String variable ) {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + TAB + "Test" + variable + "Dao.modifyRecord( "
                + toJavaCase( variable ) + " );\n" );

        sb.append( TAB + TAB + "count = " + toJavaCase( variable )
                + "Bo.update( " + toJavaCase( variable ) + " );\n" );
        sb.append( TAB + TAB + getAssertEquals() );

        return sb.toString();
    }

    private String getDeleteRecord( String variable ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "\n" + TAB + TAB + "count = " + toJavaCase( variable )
                + "Bo.delete( " + toJavaCase( variable ) + ".get"
                + toTitleCase( table.getColumn( 0 ).getFldName().toString() )
                + "() );\n" );
        sb.append( TAB + TAB + getAssertEquals() + "\n" );

        return sb.toString();
    }

    private String getDeleteVerified( String variable ) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        count++;

        if ( variable.equalsIgnoreCase( table.getDomName() ) ) {
            sb.append( TAB
                    + TAB
                    + "readRecord = "
                    + toJavaCase( variable )
                    + "Bo.read( "
                    + toJavaCase( variable )
                    + ".get"
                    + toTitleCase( table.getColumn( 0 ).getFldName().toString() )
                    + "() );\n" );
            sb.append( TAB + TAB + getNullEquals( "readRecord" ) );

        }
        else {
            sb.append( TAB
                    + TAB
                    + "readRecord = "
                    + toJavaCase( variable )
                    + "Bo.readf( "
                    + toJavaCase( variable )
                    + ".get"
                    + toTitleCase( table.getColumn( 0 ).getFldName().toString() )
                    + "() );\n" );
            sb.append( TAB + TAB + getNullEquals( "readRecord" + count ) );
        }

        return sb.toString();
    }

    private String getListBy( String variable ) {
        StringBuilder sb = new StringBuilder();
        if ( hasSearch ) {
            for ( int i = 1; i < searchList.size() + 1; i++ ) {

                sb.append( "\n" + TAB + TAB + "List<" + variable + "> list" + i
                        + "= " );
                sb.append( toJavaCase( variable ) + "Bo.getListBy"
                        + toTitleCase( searchList.get( i - 1 ) ) );
                sb.append( "( " + toJavaCase( variable ) + ".get"
                        + toTitleCase( searchList.get( i - 1 ) ) );
                sb.append( "() ) ; \n" );

                sb.append( TAB + TAB + "assertEquals( 1 , list" + i
                        + ".size() );\n" );

            }
        }
        return sb.toString();
    }

    private String getAssertEquals() {
        return "assertEquals( 1, count );\n";
    }

    private String getNotNullEquals( String variable, String type ) {
        return "assertNotNull( " + toJavaCase( variable ) + ".get"
                + toTitleCase( table.getColumn( 0 ).getFldName().toString() )
                + "() );\n";
    }

    private String getNullEquals( String variable ) {
        return "assertNull( " + toJavaCase( variable ) + " );\n";
    }

    private String toJavaCase( String value ) {
        String line = value;

        line = line.substring( 0, 1 ).toLowerCase() + line.substring( 1 );
        return line;
    }

}
