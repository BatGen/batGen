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

import java.util.regex.Pattern;

public class Token {

    private String value = "";

    public enum Type {
        COMMENT, ERROR, HEADER, LIST, NEWLINE, SPECIALCHAR, WORD
    }

    private Type type;

    public String toString() {
        return " [Token: " + value + ", Type: " + type + "]";
    }

    public void setValue( String name ) {
        this.value = name;
    }

    public String getValue() {
        return value;
    }

    public void setType( Type tokenType ) {
        this.type = tokenType;
    }

    public Type getType() {
        return type;
    }

    /**
     * Performs a case-insensitive comparison. This is a helper function to
     * reduce typing effort.
     * 
     * @param value
     * @return
     */
    public boolean equals( String value ) {
        return value.equalsIgnoreCase( this.value );
    }

    public boolean equals( FieldType type ) {
        return value.equalsIgnoreCase( type.getName() );
    }

    public boolean isComment() {
        return ( Type.COMMENT == type );
    }

    public boolean isError() {
        return ( Type.ERROR == type );
    }

    public boolean isHeader() {
        return ( Type.HEADER == type );
    }

    public boolean isNewLine() {
        return ( Type.NEWLINE == type );
    }

    public boolean isSpecial() {
        return ( Type.SPECIALCHAR == type );
    }

    public boolean isWord() {
        return ( Type.WORD == type );
    }

    public boolean isNumeric() {
        return Pattern.matches( "[0-9]+", value );
    }

    public boolean isOpenParen() {
        return "(".equals( value );
    }

    public boolean isCloseParen() {
        return ")".equals( value );
    }

    public boolean isComma() {
        return ",".equals( value );
    }

    public boolean isRequired() {
        return "*".equals( value );
    }

    public boolean isSequenceDisabled() {
        return "-".equals( value );
    }

    public boolean isKey() {
        return "!".equals( value );
    }

    public boolean isSearchId() {
        return "?".equals( value );
    }

    public boolean isSysTime(){
    	return "^".equals( value );
    }
}
