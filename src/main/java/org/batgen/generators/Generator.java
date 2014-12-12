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

import java.io.*;
import java.util.List;

import org.batgen.Column;
import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

/**
 * The base class for all generators. Protected values are provided for
 * sub-classes to ease typing effort.
 * 
 */
public class Generator {
    public static final String   PROTECTED_CODE = "PROTECTED CODE";
    public static final String   TAB            = "    ";

    protected final Table        table;
    protected final String       comment;
    protected final String       pkg;
    protected final String       domName;
    protected final String       tableName;
    protected final List<Column> columns;
    protected final List<String> searchList;
    protected final boolean      hasSearch;

    public Generator( Table table ) {
        this.table = table;
        searchList = table.getSearchList();
        comment = table.getComment();
        pkg = table.getPackage();
        domName = table.getDomName();
        tableName = table.getTableName();
        columns = table.getColumns();
        hasSearch = table.hasSearch();
    }

    /**
     * Use this method to uppercase the first letter of a string i.e.
     * "exampleString" will change to "ExampleString"
     * 
     * @param val
     *            String you want to change
     * @return returns the input with the first character in uppercase
     */
    public static String toTitleCase( String val ) {
        return Character.toTitleCase( val.charAt( 0 ) )
                + val.substring( 1, val.length() );
    }

    public static void appendToFile( String filePath, String content ) {

        File file = new File( filePath );
        file = new File( file.getParent() );

        if ( !file.exists() ) {
            file.mkdirs();
        }

        FileWriter fw = null;

        try {

            fw = new FileWriter( filePath, true );
            fw.write( content );
            fw.close();

        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * checkDir creates the necessary folders to a file if they do not exist
     * 
     * @param path
     *            path of the file
     * @return returns whether the file exists or not
     */
    public static boolean checkDir( String path ) {
        File file = new File( path );

        return file.exists();
    }

    /**
     * @return a path derived from the passed in package.
     */
    protected String packageToPath() {
        return table.getPackage().replace( ".", "/" );
    }

    public static String makeSpace( int spaceCount, String word ) {
        int i = spaceCount - word.length();
        String spaces = " ";
        while ( i > 0 ) {
            spaces = spaces.concat( " " );
            i--;
        }
        return spaces;
    }

    public static String getProtectedJavaLines( String fn ) {
        StringBuffer sb = new StringBuffer();

        if ( fn.contains( ".sql" ) ) {
            sb.append( "\n-- " );
            sb.append( PROTECTED_CODE );
            sb.append( " -->" );
        }

        else if ( fn.contains( ".xml" ) ) {
            sb.append( "\n\n\n    <!-- " );
            sb.append( PROTECTED_CODE );
            sb.append( " -->" );
        }

        else {
            sb.append( TAB );
            sb.append( "// " );
            sb.append( PROTECTED_CODE );
            sb.append( " -->" );
        }

        List<String> lines = getProtectedLines( fn );
        if ( lines.isEmpty() && !fn.contains( ".sql" ) ) {
            sb.append( "\n\n}" );
        }

        for ( String line : lines ) {
            sb.append( line );
        }

        return sb.toString();
    }
}