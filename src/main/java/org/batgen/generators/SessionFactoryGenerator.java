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

import static org.batgen.generators.GenUtil.*;

import java.util.List;

/**
 * Generates the SessionFactory class in the DAO package.
 *
 */
public class SessionFactoryGenerator {

    private StringBuilder sb = new StringBuilder();
    private String pkg;
    private String pkgPath;
    String fileName;

    public SessionFactoryGenerator( String pkg ) {
        this.pkg = pkg;
        pkgPath = packageToPath( pkg );
        fileName = pkgPath + "/dao/SessionFactory.java";
    }

    public String createSession() {
        
        if ( fileExists( fileName ) ) {
            return fileName + " already exists, not generated.";
        }
        
        sb = new StringBuilder();

        sb.append( createPackage() );
        sb.append( createImports() );
        sb.append( createHeader() );
        sb.append( createBody() );
        sb.append( createProtectedLines() );

        writeToFile( fileName, sb.toString() );

        return fileName;
    }

    private String createProtectedLines() {
        StringBuilder sb = new StringBuilder();
        List<String> lines = getProtectedLines( fileName );

        if ( !lines.isEmpty() ) {
            for ( String line : lines ) {
                sb.append( line );
            }
        }
        else {
            sb.append( "\n" );
            sb.append( TAB + "//" );
            sb.append( PROTECTED_CODE );
            sb.append( "\n}\n" );
        }

        return sb.toString();
    }

    private String createPackage() {
        StringBuilder sb = new StringBuilder();

        sb.append( "package " + pkg + ".dao" + ";" );
        sb.append( "\n\n" );

        return sb.toString();
    }

    private String createImports() {
        StringBuilder sb = new StringBuilder();

        sb.append( "import java.io.*;\n" );
        sb.append( "import org.apache.ibatis.io.Resources;\n" );
        sb.append( "import org.apache.ibatis.session.*;\n" );
        sb.append( "\n" );

        return sb.toString();
    }

    private String createHeader() {
        return "public class SessionFactory {\n";
    }

    private String createBody() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "public static SqlSessionFactory ssf;\n" );
        sb.append( TAB + "private static String ENVIRONMENT;\n" );

        String packagePath = pkgPath.replace( "src/main/java/", "" );

        sb.append( TAB + "private static String resource = \"" + packagePath
                + "/dao/mybatis-config.xml\";\n" );

        sb.append( "\n" + TAB + "private static void setup(){\n" );
        sb.append( TAB
                + TAB
                + "InputStream inputStream = null;\n\n"
                + TAB
                + TAB
                + "try {\n"
                + TAB
                + TAB
                + TAB
                + "inputStream = Resources.getResourceAsStream( resource );\n"
                + TAB
                + TAB
                + "} catch ( IOException e ) {\n"
                + TAB
                + TAB
                + TAB
                + "e.printStackTrace();\n\t\t}\n\n"
                + TAB
                + TAB
                + "if ( inputStream==null )\n"
                + TAB
                + TAB
                + TAB
                + "throw new RuntimeException( \"Cannot load myBatis resource \" + resource );\n\n"
                + TAB
                + TAB
                + "ssf = new SqlSessionFactoryBuilder().build( inputStream , ENVIRONMENT );\n"
                + "\n" + TAB + "}\n" + "\n" + TAB
                + "public static SqlSession getSession() {\n" + TAB + TAB
                + "return ssf.openSession();\n" + TAB + "}\n" + "\n" + TAB
                + "public static void initialize(){\n" + TAB + TAB
                + "setup();\n" + TAB + "}\n" + "\n" + TAB
                + "public static void initializeForTest(){\n" + TAB + TAB
                + "ENVIRONMENT = \"TESTING\";\n" + TAB + TAB + "setup();\n"
                + TAB + "}" );

        return sb.toString();
    }

}
