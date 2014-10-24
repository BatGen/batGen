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
package org.batgen;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

/**
 * A reusable parser class. You can instantiate once and run parse() on multiple
 * files.
 */
public class Parser {
    private static HashMap<String, Table> tableMap = new HashMap<String, Table>();

    private String fileName;

    private Table table;
    private Tokenizer tokenizer;

    // private boolean parentParse;

    private boolean fStop;

    /**
     * Initialize - initializes all global variables - this function must reset
     * all member variables to ensure that subsequent calls to parse() will not
     * have side effects from previous calls.
     * 
     * @param fileName
     *            the file to parse.
     */
    private void initialize( String fileName ) {

        this.fileName = fileName;
        table = new Table();

        BufferedReader br = getBufferedReader( fileName );
        tokenizer = new Tokenizer( br );

        fStop = false;
        // parentParse = false;
    }

    /**
     * Parse a single file and return a table object.
     * 
     * @param fileName
     * @return table
     */
    public Table parse( String fileName ) {
        boolean fSettings = true;

        this.fileName = fileName;
        initialize( fileName );

        Token token = getNextToken();

        while ( token != null ) {

            if ( isNewLine( token ) ) {
                // do nothing

            }
            else if ( token.isComment() ) {
                table.setComment( token.getValue() );

            }
            else if ( token.equals( "[Settings]" ) ) {
                fSettings = true;

            }
            else if ( token.equals( "[Fields]" ) ) {
                fSettings = false;

            }
            else if ( fSettings ) {
                parseSettings( token );
                if ( fStop )
                    return table;

            }
            else {
                parseFields( token );
            }

            token = getNextToken();
        }

        table.setup();

        return table;
    }

    /**
     * Parses tokens for SETTINGS header.
     * 
     * @param token
     *            current token
     */
    private void parseSettings( Token token ) {

        if ( token.equals( "CLASS" ) ) {
            token = getNextToken();

            if ( isNewLine( token ) )
                throwException( "Expected a class name, recieved new line." );

            setClass( toCamelCase(token) );

            // check for optional table name
            token = getNextToken();
            if ( isNewLine( token ) )
                return;
            table.setTableName( token.getValue() );

        }
        else {
            throwException( "Unexpected token, expected CLASS" );

        }
    }

    /**
     * Sets the table class information
     * 
     * @param token
     *            current token
     */
    private void setClass( Token token ) {
        Table existingTable = null;
        String value = token.getValue();

        table.setDomName( value );
        table.setTableName( camelToCaps( value ) );

        // if table already exists, stop parsing.
        existingTable = tableMap.get( value );
        if ( existingTable != null ) {
            fStop = true;
            table = existingTable;
            return;
        }
        tableMap.put( value, table );

    }

    /**
     * Parses the fields.
     * 
     * @param token
     *            current token
     */
    private void parseFields( Token token ) {

        if ( !token.isWord() ) {
            throwException( "Expecting type." );

        }
        else {
            parseCommonFields( token );
        }
    }

    private void parseCommonFields( Token token ) {
        Column column = null;

        if ( token.equals( FieldType.DATE ) ) {
            column = new Column();
            column.setType( FieldType.DATE );
            token = getNextToken();

        }
        else if ( token.equals( FieldType.BOOLEAN ) ) {
            LengthColumn lenCol = new LengthColumn();
            lenCol.setType( FieldType.BOOLEAN );
            lenCol.setColLen( "1" );
            token = getNextToken();

            column = lenCol;

        }
        else if ( token.equals( FieldType.DOUBLE ) ) {
            DoubleColumn doubleColumn = new DoubleColumn();
            doubleColumn.setType( FieldType.DOUBLE );

            token = getNextToken();
            if ( !token.isOpenParen() )
                throwException( "Expecting open paren." );

            token = getNextToken();
            if ( !token.isNumeric() )
                throwException( "Expecting length of DOUBLE value." );

            doubleColumn.setColLen( token.getValue() );

            // check for precision
            token = getNextToken();
            if ( token.isComma() ) {
                token = getNextToken();

                if ( !token.isNumeric() )
                    throwException( "Expecting precision field." );

                doubleColumn.setPrecision( token.getValue() );
            }

            token = getNextToken();
            if ( !token.isCloseParen() )
                throwException( "Expecting close paren." );

            column = doubleColumn;
            token = getNextToken();

        }
        else if ( token.equals( FieldType.BLOB ) ) {
            column = new BlobColumn();
            column.setType( FieldType.BLOB );

            token = getNextToken();

        }
        else {
            column = getLengthColumn( token );
            token = getNextToken();

        }

        parseColumnEnd( column, token );
        table.addColumn( column );

    }

    private Column getLengthColumn( Token token ) {
        LengthColumn column = new LengthColumn();

        if ( token.equals( FieldType.LONG ) ) {
            column.setType( FieldType.LONG );

        }
        else if ( token.equals( FieldType.INTEGER ) ) {
            column.setType( FieldType.INTEGER );

        }
        else if ( token.equals( FieldType.STRING ) ) {
            column.setType( FieldType.STRING );

        }
        else {
            throwException( "Unhandled Type. Expected DOUBLE, STRING, LONG, INTEGER, BLOB, BOOLEAN or DATE" );
        }

        token = getNextToken();
        if ( !token.isOpenParen() )
            throwException( "Expecting open paren." );

        token = getNextToken();
        if ( !token.isNumeric() )
            throwException( "Numeric length expected." );

        column.setColLen( token.getValue() );

        token = getNextToken();
        if ( !token.isCloseParen() )
            throwException( "Expecting close paren." );

        return column;
    }

    /**
     * Parse the values at the end of each column. There is a field name, and
     * optional column name and comment.
     * 
     * @param column
     *            the column to set field name, column name and comment to
     * @param token
     *            the first token in the column line of the config file.
     */
    private void parseColumnEnd( Column column, Token token ) {

        while ( !isNewLine( token ) ) {

            if ( token.isKey() ) {
                column.setKey();

            }
            else if ( token.isRequired() ) {
                column.setRequired();

            }
            else if ( token.isSequenceDisabled() ) {
                column.setSequenceDisabled();

            }
            else if ( token.isComment() ) {
                column.setComment( token.getValue() );

            }
            else if ( token.isSearchId() ) {
                column.setSearchId();

            }
            else if ( token.isWord() ) {
                if ( column.getFldName() == null ) {
                    column.setFldName( token.getValue() );
                    column.setColName( camelToCaps( token ) );

                }
                else {
                    column.setColName( token.getValue() );

                }
            }

            token = getNextToken();
        }

    }

    private boolean isNewLine( Token token ) {

        if ( token == null ) {
            return true;

        }
        else if ( token.isNewLine() ) {
            return true;

        }

        return false;
    }

    /**
     * Throws an exception with an indication of where the error is in the input
     * file.
     * 
     * @param additionalMsg
     */
    private void throwException( String additionalMsg ) {
        StringBuilder caret = new StringBuilder();

        for ( int i = 0; i < tokenizer.getCol(); i++ ) {
            caret.append( " " );
        }

        caret.append( "^" );
        throw new IllegalArgumentException( "\nError in file: " + fileName
                + " on line:" + tokenizer.getRow() + " at col:"
                + tokenizer.getCol() + ".\n" + additionalMsg + "\n"
                + tokenizer.getLine() + " \n" + caret.toString() );
    }

    private Token getNextToken() {
        Token token = tokenizer.getToken();

        if ( token == null ) {
            return null;

        }
        else if ( token.isError() ) {
            throwException( "Input contains syntax error" );
        }

        return token;
    }

    /**
     * Gets the buffered reader from the specified fileName.
     * 
     * @param file
     * @return
     */
    public BufferedReader getBufferedReader( String fileName ) {

        File file = new File( fileName );

        BufferedReader br = null;

        try {
            Reader reader = new FileReader( file );
            br = new BufferedReader( reader );

        } catch ( FileNotFoundException e ) {
            throwException( "File: " + fileName + " not found." );
        }

        return br;
    }

    /**
     * Extracts the path part of the filename including the last separator char.
     * 
     * @param fileName
     * @return
     */
    public static String getPath( String fileName ) {
        int index = fileName.lastIndexOf( File.separatorChar );
        return fileName.substring( 0, index + 1 );
    }

    /**
     * this function convert java case so only 1st leter is caps
     * 
     * @param value
     * @return
     */
    private Token toCamelCase( Token token ) {
        
        String line = token.getValue();
        line = line.substring( 0, 1 ).toUpperCase() + line.substring( 1 );

        token.setValue( line );
        return token;
    }

    /**
     * This function converts camel case to all capitals with words separated by
     * underscore.
     * 
     */
    public static String camelToCaps( String camelString ) {
        StringBuilder sb = new StringBuilder();

        char c;
        for ( int i = 0; i < camelString.length(); i++ ) {
            c = camelString.charAt( i );
            if ( c >= 'A' && c <= 'Z' && i != 0 )
                sb.append( "_" );
            sb.append( c );
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Converts CamelCase words to Uppercase with underscore between words. This
     * is to convert the case convention from Java class to a table name.
     * 
     * @param token
     * @return
     */
    public static String camelToCaps( Token token ) {
        String word = token.getValue();
        return camelToCaps( word );
    }

    /**
     * @return the tableMap which accumulates all the tables that were parsed.
     */
    public static Collection<Table> getTableMap() {
        return tableMap.values();
    }

}