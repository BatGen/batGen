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

import org.batgen.Column;
import org.batgen.FieldType;
import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

/**
 * Generates the domain classes
 * 
 */
public class DomainGenerator extends Generator {
    private StringBuilder sb = new StringBuilder();
    private StringBuilder flds = new StringBuilder();
    private StringBuilder getsSets = new StringBuilder();
    boolean date;

    private final int FLD_SPACE = 9;
    private final String SPACE = "    ";
    private final int PUB_SPACE = 9;
    private final String IMPORT_DATE = "import java.util.Date;";

    private String filePath;

    public DomainGenerator( Table table ) {
        super( table );
        filePath = "src/main/java/" + packageToPath() + "/domain/" + domName
                + ".java";
    }

    public String createDomain() {
        date = false;
        for ( Column col : table.getColumns() ) {
            writeColFields( col );
            getsSets.append( writeGet( col ) );
            getsSets.append( writeSet( col ) );

            if ( col.getFldType().equals( "Date" ) && date != true ) {
                date = true;
            }
        }
        writePackage();
        sb.append( "\n" );
        writeImport();
        skipLine();
        writeComment();
        sb.append( "\n" );
        writeClass();
        skipLine();

        sb.append( flds );
        sb.append( "\n" );
        sb.append( getsSets );

        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );

        return filePath;
    }

    private void writeComment() {
        if (comment == null){
            sb.append( "" );
        }
        else
            sb.append( comment );
    }

    private void writePackage() {
        sb.append( "package " );
        sb.append( pkg );
        sb.append( ".domain" );
        sb.append( ";" );
    }

    private void writeImport() {
        ImportGenerator imports = new ImportGenerator( filePath );
        if ( date )
            imports.addImport( IMPORT_DATE );
        sb.append( imports.toString() );

    }

    private void writeClass() {
        sb.append( "public class " );
        sb.append( domName );
        sb.append( " {" );
    }

    private void writeColFields( Column col ) {

        flds.append( SPACE );
        flds.append( "private " );

        String fldType = col.getFldType();

        flds.append( fldType );
        flds.append( makeSpace( FLD_SPACE, fldType ) );

        flds.append( col.getFldName() );
        flds.append( ";" );

        if ( col.getComments() != null ) {
            flds.append( makeSpace( 20, col.getFldName() ) );
            flds.append( col.getComments() );
        }

        flds.append( "\n" );
    }

    private String writeSet( Column col ) {
        StringBuilder str = new StringBuilder();

        str.append( SPACE );
        str.append( "public void      " );
        str.append( "set" );

        str.append( toTitleCase( col.getFldName() ) );
        str.append( "( " );

        if ( col.getType() == FieldType.BOOLEAN ) {
            str.append( "boolean value ) { " );
            str.append( col.getFldName() );
            str.append( " = value ? true : false; }\n" );

        }
        else {
            str.append( col.getFldType() );
            str.append( " value ) { " );
            str.append( col.getFldName() );
            str.append( " = value; }\n" );

        }

        return str.toString();
    }

    private String writeGet( Column col ) {
        StringBuilder str = new StringBuilder();

        str.append( SPACE );
        str.append( "public " );
        str.append( col.getFldType() );
        str.append( makeSpace( PUB_SPACE, col.getFldType() ) );

        str.append( "get" );
        str.append( toTitleCase( col.getFldName() ) );
        str.append( "() { return " );
        str.append( col.getFldName() );
        str.append( "; }\n" );

        return str.toString();
    }

    public void skipLine() {
        sb.append( "\n\n" );
    }

}
