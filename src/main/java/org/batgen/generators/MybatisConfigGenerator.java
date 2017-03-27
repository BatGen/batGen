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

import static org.batgen.generators.GenUtil.fileExists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.batgen.DatabaseType;

public class MybatisConfigGenerator {

    private StringBuilder sb;

    /**
     * Basic resources stored as string to be used in the program.
     */
    private String packagePath;
    private String fileNameWithPath = "src/main/resources/";
    private String fileType = ".xml";
    private List<String> classNames;

    private static final String TAB = "    ";

    /**
     * Sets the default environment to be written into the mybatis-config.xml
     * file
     */
    private String defaultEnvironment;

    /**
     * @param classNames
     *            - list of all class names from the .txt files under settings
     * @param packagePath
     *            - this must be the String result of a "table.getPackagePath()"
     *            call
     */
    public MybatisConfigGenerator( List<String> classNames, String packagePath,
            DatabaseType databaseType ) {

        this.packagePath = packagePath;
        this.classNames = classNames;
        this.defaultEnvironment = databaseType.toString();
        fileNameWithPath += packagePath + "/dao/mybatis-config.xml";
        fileNameWithPath = fileNameWithPath.replace( ".", "/" );
        char[] path = fileNameWithPath.toCharArray();
        path[path.length - 4] = '.';
        fileNameWithPath = new String( path );
    }

    /**
     * This method calls the other methods within this class as necessary to
     * create the text of the mybatis-configuration file, which it then writes
     * to file.
     */
    public String createConfiguration() {
        
        if ( fileExists( fileNameWithPath ) ) {
            return fileNameWithPath + " already exists, not generated.";
        }
        
        sb = new StringBuilder();

        createHeader();
        createEnvironments();
        createMappers();
        sb.append( createFooter() );
        writeToFile( sb.toString() );

        return fileNameWithPath;

    }

    /**
     * This method iterates through the sample environment information as
     * defined in the String[] constants. The result is a collection of
     * "environment" elements in the mybatis-config.xml file that can be useful
     * samples, or ready to be used in a production environment with minimal
     * changes made to the output file. This method adds in descriptions of what
     * each element is to aid the user in knowing what values to put where.
     */
    private void createEnvironments() {

        sb.append( TAB + "<environments default=\"" + defaultEnvironment
                + "\">\n\n" );

        DatabaseEnvironment samples = DatabaseEnvironment.createH2Environment();
        sb.append( samples.createEnvironment() + "\n" );

        samples = DatabaseEnvironment.createOracleEnvironment();
        sb.append( samples.createEnvironment() + "\n" );

        samples = DatabaseEnvironment
                .createTestEnvironment( defaultEnvironment );
        sb.append( samples.createEnvironment() + "\n" );

        sb.append( samples.createJNDIEnvironment() + "\n" );

        sb.append( TAB + "</environments>\n\n" );

    }

    /**
     * @return - returns the full set of mappers that this file will contain
     */
    private String createMappers() {

        sb.append( TAB + "<mappers>\n" );

        for ( String name : classNames ) {
            sb.append( TAB + TAB + "<mapper resource=\"" + getMapperPath()
                    + "/dao/" + name + "Dao" + fileType + "\" />\n" );
        }
        sb.append( TAB + "</mappers>\n\n" );

        return sb.toString();
    }

    /**
     * This method returns the path for this table as it is for the resulting
     * package. This methods contents were copied and pasted from
     * SessionFactoryGenerator's method "getPackagePath()", which was
     * unreachable as it is declared private
     * 
     * @param filePath
     *            - this is the path where the .txt config file is being stored
     * @return filepath for this mapper, e.g.
     *         "com/simoncomputing/sample/xml/Employee"
     */
    private String getMapperPath() {

        packagePath = packagePath.replace( ".", "/" );
        return packagePath;

    }

    /**
     *
     * @return - creates the standard xml header for this file, and appends the
     *         first element for the file, "configuration"
     */
    private String createHeader() {

        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" );
        sb.append( "<!DOCTYPE configuration\n" );
        sb.append( TAB
                + "PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n" );
        sb.append( "<configuration>\n\n" );

        return sb.toString();

    }

    /**
     *
     * @return - simply returns closing part of the "configuration" element that
     *         was appended in the header
     */
    private String createFooter() {

        return "</configuration>";

    }

    /**
     * This method was copied and pasted from Generator.java. This was done to
     * prevent the need for a method for passing in the "String[] files", so
     * that that String[] could simply be passed in via the constructor. By
     * doing that, this method was the *only* one needed from Generator.
     * Therefore, the code was copied over to decrease the coupling between this
     * generator and its cousins. (However it has been modified slightly)
     * 
     * @param content
     *            - String to be written to the file
     */
    public void writeToFile( String content ) {
        File file = new File( fileNameWithPath );
        file = new File( file.getParent() );

        if ( file.exists() == false ) {
            file.mkdirs();
        }
        try {

            PrintWriter pw = new PrintWriter( fileNameWithPath );
            pw.write( content );
            pw.close();

        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
    }

}
