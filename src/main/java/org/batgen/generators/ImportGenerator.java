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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for finding the protected import lines in the original file.
 * 
 * @param fileName
 *            filename and path of the file to be checked for protected import
 *            lines
 * 
 */
public class ImportGenerator {
    private List<List<String>> importLines = new ArrayList<List<String>>();
    private List<String> importJavaLines = new ArrayList<String>();
    private List<String> importJavaxLines = new ArrayList<String>();
    private List<String> importExternalLines = new ArrayList<String>();
    private List<String> importComLines = new ArrayList<String>();

    public ImportGenerator( String fileName ) {
        getImportLines( fileName );

    }

    public void getImportLines( String fileName ) {

        File exportFile = new File( fileName );
        BufferedReader br = null;

        try {

            br = new BufferedReader( new FileReader( exportFile ) );
            String line = "";

            // find start of protected imports
            while ( !line.contains( "import" ) ) {
                line = br.readLine();
                if ( line == null )
                    break;
            }

            while ( line != null ) {

                addImport( line );
                line = br.readLine();

            }

            br.close();

        } catch ( FileNotFoundException e ) {
            // Ignored if file doesn't exist.

        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void addImport( String str ) {

        if ( str.contains( "import javax" )
                && !importJavaxLines.contains( str + "\n" ) )
            importJavaxLines.add( str + "\n" );

        else if ( str.contains( "import java" )
                && !importJavaLines.contains( str + "\n" ) )
            importJavaLines.add( str + "\n" );

        else if ( str.contains( "import com" )
                && !importComLines.contains( str + "\n" ) )
            importComLines.add( str + "\n" );

        else if ( str.contains( "import" )
                && !importExternalLines.contains( str + "\n" )
                && !importJavaxLines.contains( str + "\n" )
                && !importJavaLines.contains( str + "\n" )
                && !importComLines.contains( str + "\n" ) )
            importExternalLines.add( str + "\n" );

    }

    @Override
    public String toString() {
        StringBuilder imports = new StringBuilder();

        importLines.add( importJavaLines );
        importLines.add( importJavaxLines );
        importLines.add( importExternalLines );
        importLines.add( importComLines );

        for ( List<String> lines : importLines ) {
            for ( String line : lines ) {
                imports.append( line );
            }

            if ( !lines.isEmpty() ) {
                imports.append( "\n" );
            }
        }

        return imports.toString();

    }

}
