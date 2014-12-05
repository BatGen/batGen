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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.batgen.Token.Type;

public class Tokenizer {

    private String         line;
    private int            lineCount, lastPos, pos;
    private BufferedReader bf;
    private final String   SPACE    = "\t\f ";
    private final String   SPECIAL  = "!*,()-?";
    private StringBuilder  sb       = new StringBuilder( "" );
    private int            holdPos;

    // TODO: Need comment on what the purpose of readNext is...
    private boolean        readNext = true;

    public Tokenizer( BufferedReader bf ) {
        this.bf = bf;
    }

    public Tokenizer( String s ) {
        Reader r = new StringReader( s );
        bf = new BufferedReader( r );
    }

    public Token getToken() {
        Token t = new Token();

        if ( readNext ) {
            readLine();
            if ( line == null )
                return t = null;

            checkPos();
        }

        while ( pos < line.length() ) {
            readNext = false;
            if ( pos == -1 ) {
                t.setValue( "\n" );
                t.setType( Type.NEWLINE );
                readNext = true;

                return t;
            }

            if ( isComment( String.valueOf( line.charAt( pos ) ) ) ) {
                sb.setLength( 0 );
                t = readComment();

                return t;
            }

            if ( isHeader( String.valueOf( line.charAt( pos ) ) ) ) {
                t.setValue( readHeader() );
                if ( t.getValue().startsWith( "Error:" ) ) {
                    t.setType( Type.ERROR );
                }
                else
                    t.setType( Type.HEADER );

                return t;
            }

            if ( isSpecial( String.valueOf( line.charAt( pos ) ) ) ) {
                if ( line.charAt( pos ) == '(' ) {
                    String p = checkParen();
                    if ( p.startsWith( "Error:" ) ) {
                        t.setValue( p );
                        t.setType( Type.ERROR );
                        return t;
                    }
                }

                t.setValue( String.valueOf( line.charAt( pos ) ) );
                t.setType( Type.SPECIALCHAR );

                increment();
                checkPos();

                return t;
            }

            if ( isWord( String.valueOf( line.charAt( pos ) ) ) ) {
                sb.setLength( 0 );
                t.setValue( readWord() );
                if ( t.getValue().startsWith( "Error:" ) )
                    t.setType( Type.ERROR );
                else
                    t.setType( Type.WORD );

                checkPos();
                return t;
            }

            if ( isSpace( String.valueOf( line.charAt( pos ) ) ) ) {
                readSpace();
                checkPos();
            }
        }

        return null;
    }

    public String getLine() {
        return line;
    }

    public int getRow() {
        return lineCount;
    }

    public int getCol() {
        return lastPos;
    }

    private void readLine() {
        try {
            line = bf.readLine();
            lineCount++;
            pos = 0;
            lastPos = 0;

        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private boolean isComment( String s ) {
        return ( "/".equals( s ) );
    }

    private boolean isHeader( String s ) {
        return ( "[".equals( s ) );
    }

    private boolean isWord( String s ) {
        return ( !"/".equals( s ) && !SPECIAL.contains( s ) && !SPACE
                .contains( s ) );
    }

    private boolean isSpecial( String s ) {
        return ( SPECIAL.contains( s ) );
    }

    private boolean isSpace( String s ) {
        return ( SPACE.contains( s ) );
    }

    private boolean isBadChar( char c ) {
        return ( !String.valueOf( line.charAt( pos ) ).matches(
                "^[a-zA-Z0-9_\\.\\$]" ) );
    }

    private Token readComment() {
        Token t = new Token();
        holdPos = pos;
        if ( pos == line.length() || pos == ( line.length() - 1 ) ) {
            increment();
            t.setValue( "Error: '/' must be followed by '/' or '*'." );
            t.setType( Type.ERROR );
            return t;
        }

        if ( line.charAt( pos + 1 ) == '/' ) {
            t.setValue( line.substring( pos ) );
            pos = -1;

        }
        else if ( line.charAt( pos + 1 ) == '*' ) {
            t.setValue( readCommentBlock() );

        }
        else {
            increment();
            t.setValue( "Error: '/' must be followed by '/' or '*'." );
            t.setType( Type.ERROR );
            return t;
        }

        if ( t.getValue().startsWith( "Error:" ) ) {
            t.setType( Type.ERROR );
            return t;
        }

        checkPos();
        t.setType( Type.COMMENT );
        lastPos = holdPos;
        return t;
    }

    private String readWord() {
        String s = null;
        holdPos = pos;
        while ( pos < line.length()
                && isWord( String.valueOf( line.charAt( pos ) ) ) ) {
            if ( isBadChar( line.charAt( pos ) ) ) {
                if ( isSpace( String.valueOf( line.charAt( pos - 1 ) ) )
                        || line.charAt( pos - 1 ) == ']'
                        || line.charAt( pos - 1 ) == ')' ) {
                    increment();
                    return s = "Error: Floating '" + line.charAt( pos - 1 )
                            + "' not allowed.";
                }
                increment();
                return s = "Error: Words cannot contain '"
                        + line.charAt( pos - 1 ) + "'.";
            }
            sb.append( String.valueOf( line.charAt( pos ) ) );
            increment();
        }
        s = sb.toString();
        lastPos = holdPos;
        return s;
    }

    private String readCommentBlock() {
        String comment = null;
        sb.append( line.substring( pos ) );
        int i = sb.indexOf( "*/" );

        holdPos = pos;
        String holdLine = sb.toString();
        int holdCount = lineCount, holdLastPos = lastPos;

        if ( i != -1 ) {
            int a = line.indexOf( "*/" );
            comment = line.substring( pos, a + 2 );
            lastPos = pos;
            pos = line.indexOf( "*/" ) + 2;

        }
        else {
            sb.append( "\n" );
            readLine();
            while ( i == -1 ) {
                sb.append( line );
                sb.append( "\n" );
                readLine();

                if ( line == null ) {
                    line = holdLine;
                    lineCount = holdCount;
                    lastPos = holdLastPos;
                    increment();
                    return comment = "Error: '/*' must end with a '*/'. No '*/' was found in the input.";
                }

                i = line.indexOf( "*/" );

                if ( i != -1 ) {
                    sb.append( line.substring( 0, i + 2 ) );
                    comment = sb.toString();
                    lastPos = pos;
                    pos = line.indexOf( "*/" ) + 2;
                }
            }
        }
        lastPos = holdPos;
        return comment;
    }

    private String readHeader() {
        String s = null;
        holdPos = pos;
        int i = line.indexOf( ']' );

        if ( i != -1 ) {
            s = line.substring( pos, ( i + 1 ) );
            lastPos = pos;
            pos = pos + ( ( i - pos ) + 1 );

            checkPos();

            int e = s.length() - 1;
            String sub = s.substring( 1, e );

            if ( sub.equalsIgnoreCase( "Settings" )
                    || sub.equalsIgnoreCase( "Fields" )
                    || sub.equalsIgnoreCase( "Indexes" )
                    || sub.equalsIgnoreCase( "ForeignKeys" ) ) {
                return s;
            }
            else {
                s = "Error: The brackets must contain only 'Settings', 'Fields', or 'ForeignKeys'.";
            }

        }
        else {
            s = "Error: '[' must have an ending ']' present on the same line. ";
        }

        lastPos = holdPos;
        return s;
    }

    private String checkParen() {
        holdPos = pos;
        String paren = line.substring( pos );
        int i = paren.indexOf( ")" );
        if ( i == -1 ) {
            increment();
            paren = "Error: '(' must have an ending ')' present on the same line.";
        }

        lastPos = holdPos;
        return paren;
    }

    private void readSpace() {
        increment();
    }

    private void checkPos() {
        if ( pos == line.length() ) {
            pos = -1;
        }
    }

    private void increment() {
        lastPos = pos;
        pos++;
    }
}
