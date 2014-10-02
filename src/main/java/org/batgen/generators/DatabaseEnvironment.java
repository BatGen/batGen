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

/**
 * Generates a sample set of environments file. This file is intended to be
 * updated with specific connection details.
 * 
 */
public class DatabaseEnvironment {

    private String environmentName;
    private String driver;
    private String url;
    private String typeOfDataSource;
    private String dataSource;
    private String transactionManager;
    private String TAB = "    ";

    private static final String[] h2Parameters = { "H2", "org.h2.Driver",
            "jdbc:h2:tcp://localhost:9096/sample/testDB", "POOLED", "", "JDBC" };

    private static final String[] jndiParameters = { "JNDI", "", "", "JNDI",
            "java:/comp/env/jdbc/sample/testDB", "JDBC" };

    private static final String[] mysqlParameters = { "MYSQL",
            "com.mysql.jdbc.Driver",
            "jdbc:mysql://localhost:9096/sample/testDB", "POOLED", "", "JDBC" };

    private static final String[] oracleParameters = { "ORACLE",
            "oracle.jdbc.OracleDriver",
            "jdbc:oracle:thin:@localhost:9096:sample/testDB", "POOLED", "",
            "JDBC" };

    /**
     * This constructor takes an array of arguments. To avoid having to figure
     * out what an argument is, we assume ALL potential arguments will be
     * provided, and arguments that are not used or are irrelevant for this
     * particular environment will simply provide an empty string (""), rather
     * than a null string.
     * 
     * @param arguments
     */
    public DatabaseEnvironment( String[] arguments ) {

        this.environmentName = arguments[0];
        this.driver = arguments[1];
        this.url = arguments[2];
        this.typeOfDataSource = arguments[3];
        this.dataSource = arguments[4];
        this.transactionManager = arguments[5];

    }

    /**
     * @param params
     *            - String[] of parameters containing one entry for each
     *            potential element.
     * @return - a sample database environment set up for a commonly used
     *         database.
     */
    public static DatabaseEnvironment createH2Environment() {
        return new DatabaseEnvironment( h2Parameters );
    }

    public static DatabaseEnvironment createTestEnvironment( String environment ) {
        String[] temp = null;
        String[] dbEnvironment = null;

        if ( "H2".equalsIgnoreCase( environment ) ) {
            temp = h2Parameters;
        }
        else if ( "JNDI".equalsIgnoreCase( environment ) ) {
            temp = jndiParameters;
        }
        else if ( "MYSQL".equalsIgnoreCase( environment ) ) {
            temp = mysqlParameters;
        }
        else if ( "ORACLE".equalsIgnoreCase( environment ) ) {
            temp = oracleParameters;
        }

        dbEnvironment = new String[temp.length];

        for ( int i = 0; i < temp.length; i++ ) {
            if ( i == 0 ) {
                dbEnvironment[0] = "TESTING";
            }
            else {
                dbEnvironment[i] = temp[i];
            }
        }

        return new DatabaseEnvironment( dbEnvironment );
    }

    public static DatabaseEnvironment createMySqlEnvironment() {
        return new DatabaseEnvironment( mysqlParameters );
    }

    public static DatabaseEnvironment createJndiEnvironment() {
        return new DatabaseEnvironment( jndiParameters );
    }

    public static DatabaseEnvironment createOracleEnvironment() {
        return new DatabaseEnvironment( oracleParameters );
    }

    /**
     * Autogenerated getter and setter methods
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName( String environmentName ) {
        this.environmentName = environmentName;
    }

    public String getDriver() {
        if ( this.driver == null )
            return "";
        return driver;
    }

    public void setDriver( String driver ) {
        this.driver = driver;
    }

    public String getUrl() {
        if ( this.url == null )
            return "";
        return url;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public String getTypeOfDataSource() {
        return typeOfDataSource;
    }

    public void setTypeOfDataSource( String typeOfDataSource ) {
        this.typeOfDataSource = typeOfDataSource;
    }

    public String getDataSource() {
        if ( dataSource == null )
            return "";
        return dataSource;
    }

    public void setDataSource( String dataSource ) {
        this.dataSource = dataSource;
    }

    public String getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager( String transactionManager ) {
        this.transactionManager = transactionManager;
    }

    /**
     * This method uses the currently stored arguments for this instance of a
     * database environment to return a fully formatted string that can be used
     * in the mybatis-config.xml file.
     * 
     * @return
     */
    public String createEnvironment() {

        // returns a string containing a fully written out environment for this
        // datasource
        StringBuilder sb = new StringBuilder();

        sb.append( TAB + TAB + "<environment id=\"" + this.environmentName
                + "\">\n" );
        sb.append( TAB + TAB + TAB + "<transactionManager type=\""
                + this.transactionManager + "\" />\n" );
        sb.append( TAB + TAB + TAB + "<dataSource type=\""
                + this.typeOfDataSource + "\">\n" );

        if ( this.dataSource.equals( "" ) == false ) {
            sb.append( TAB + TAB + TAB + TAB
                    + "<property name=\"data_source\" value=\""
                    + this.dataSource + "\" />\n" );
        }
        if ( this.driver.equals( "" ) == false ) {
            sb.append( TAB + TAB + TAB + TAB
                    + "<property name=\"driver\" value=\"" + this.driver
                    + "\" />\n" );
        }
        if ( this.url.equals( "" ) == false ) {
            sb.append( TAB + TAB + TAB + TAB
                    + "<property name=\"url\" value=\"" + this.url + "\" />\n" );
        }
        if ( this.typeOfDataSource.equals( "JNDI" ) == false ) {
            sb.append( createLoginInfo() );
        }
        sb.append( TAB + TAB + TAB + "</dataSource>\n" );
        sb.append( TAB + TAB + "</environment>\n" );

        return sb.toString();
    }

    /**
     * This method returns standard initial login information
     * 
     * @param username
     * @param password
     */
    private String createLoginInfo() {

        return TAB + TAB + TAB + TAB
                + "<property name=\"username\" value=\"sa\" />\n" + TAB + TAB
                + TAB + TAB + "<property name=\"password\" value=\"123\" />\n";

    }

}
