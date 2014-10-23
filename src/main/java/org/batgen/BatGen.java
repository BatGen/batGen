/***
F * The MIT License (MIT) 
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.batgen.generators.BoGenerator;
import org.batgen.generators.DaoGenerator;
import org.batgen.generators.DomainGenerator;
import org.batgen.generators.MybatisConfigGenerator;
import org.batgen.generators.SessionFactoryGenerator;
import org.batgen.generators.SqlGenerator;
import org.batgen.generators.TestBoGenerator;
import org.batgen.generators.TestDaoGenerator;
import org.batgen.generators.XmlGenerator;

/**
 * The app that generates the code. Currently supports H2.
 * 
 */
public class BatGen {

    private String basePkg;
    private String configPath;

    private DatabaseType databaseType;
    private static final String TAB = "    ";

    private boolean allFiles;
    private int fileCount;

    /**
     * Initializes the code generator.
     * 
     * @param configPath
     *            Location of all the configuration files.
     * @param basePkg
     *            Base package name.
     * @param databaseType
     */
    public BatGen( String configPath, String basePkg, DatabaseType databaseType ) {
        this.configPath = configPath;
        this.basePkg = basePkg;
        this.databaseType = databaseType;

        File file = new File( configPath );
        if ( file.exists() == false || file.isDirectory() == false ) {
            throw new RuntimeException( configPath
                    + " is not a valid directory." );
        }
    }

    /**
     * Initializes the code generator. The database default is DatabaseType.H2.
     * 
     * @param configPath
     *            Location of all the configuration files.
     * @param basePkg
     *            Base package name.
     */
    public BatGen( String path, String pkg ) {
        this( path, pkg, DatabaseType.H2 );
    }

    /**
     * Performs the code generation.
     * 
     */
    public void run() {

        System.out.println( "Press [Enter] to process all config files or "
                + "enter a comma separate list of config files.\n\n"
                + "Available Files: \n\n" + getList() + "\n\n" );

        Scanner scan = new Scanner( System.in );
        String userInput = scan.nextLine();

        if ( userInput.isEmpty() ) {
            allFiles = true;
        }
        else {
            allFiles = false;
        }

        File file = new File( "sql/CreateTables.sql" );
        if ( file.isFile() )
            file.delete();

        file = new File( "sql/DropTables.sql" );
        if ( file.isFile() )
            file.delete();

        File[] files = new File( configPath ).listFiles();

        ArrayList<String> fileList = new ArrayList<String>();

        if ( allFiles ) {
            for ( int i = 0; i < files.length; i++ ) {
                if ( files[i].toString().contains( ".txt" ) ) {
                    fileList.add( files[i].toString() );
                }
            }

        }
        else {

            // call parse with a subset of available files in this directory
            String[] inputFiles = userInput.split( "," );
            for ( int i = 0; i < inputFiles.length; i++ ) {
                fileList.add( addPathToFile( inputFiles[i].trim() ) );
            }

        }
        processFiles( fileList );

        appendForeignKeySql( "sql/CreateTables.sql" );

    }

    private void appendForeignKeySql( String fileName ) {
        StringBuilder sb = new StringBuilder();
        int colCount;

        for ( Table table : Parser.getTableMap() ) {
            colCount = 0;
            for ( Column column : table.getColumns() ) {
                if ( column.getTable() == null )
                    continue;

                colCount++;
                if ( databaseType.equals( "ORACLE" ) ) {

                    sb.append( "\n" );
                    sb.append( TAB );
                    sb.append( "KEY `key_idx_" );
                    sb.append( colCount );
                    sb.append( "` (`" );
                    sb.append( column.getColName() );
                    sb.append( "`),\n" );

                    sb.append( TAB );
                    sb.append( "CONSTRAINT `" );
                    sb.append( table.getTableName().toLowerCase() );
                    sb.append( "_fk_" );
                    sb.append( colCount );
                    sb.append( "` FOREIGN KEY (`" );
                    sb.append( column.getColName() );
                    sb.append( "`) REFERENCES `" );
                    sb.append( column.getTable().getTableName().toLowerCase() );
                    sb.append( "` (`KEY`);" );
                    continue;

                }

                if ( databaseType.equals( "H2" ) ) {
                    sb.append( "\n" );
                    sb.append( TAB );
                    sb.append( "ALTER TABLE " );
                    sb.append( table.getTableName() );
                    sb.append( " ADD FOREIGN KEY (" );
                    sb.append( column.getColName() );
                    sb.append( ") REFERENCES " );
                    sb.append( column.getTable().getTableName() );
                    sb.append( "(" );
                    sb.append( column.getTable().getColumn( 0 ).getColName()
                            .toUpperCase() );
                    sb.append( ");" );
                    continue;
                }

                // TODO: default is MySQL?!
                sb.append( "\n" );
                sb.append( TAB );
                sb.append( table.getTableName() );
                sb.append( "_FK_" );
                sb.append( colCount );
                sb.append( " foreign key (" );
                sb.append( column.getColName() );
                sb.append( ") references " );
                sb.append( column.getTable().getTableName() );
                sb.append( "(" );
                sb.append( column.getTable().getColumn( 0 ).getColName()
                        .toUpperCase() );
                sb.append( ");" );

            }
        }
    }

    /**
     * This method adds the current directory/file path to a filename, creating
     * an absolute path
     * 
     * @param filename
     *            - name of the file you want the full path for
     * @return - returns the full path for the given filename
     */
    private String addPathToFile( String filename ) {

        StringBuilder sb = new StringBuilder( configPath );

        sb.append( "/" );
        sb.append( filename );

        return sb.toString();
    }

    /**
     * This method is called by run() once it knows which files to pass along.
     * 
     * @param files
     *            - string[] consisting of a file name at each index
     */
    private void processFiles( List<String> files ) {
        Parser parser = new Parser();
        Table table = null;

        for ( String file : files ) {
            table = parser.parse( file );
            table.setPackage( basePkg );
            generateAll( table );
        }
        
        SessionFactoryGenerator sfg = new SessionFactoryGenerator( basePkg );
        printPath( sfg.createSession() );

        MybatisConfigGenerator mcg = new MybatisConfigGenerator( files,
                basePkg, databaseType );
        printPath( mcg.createConfiguration() );

        printPath( "sql/CreateTables.sql" );
        printPath( "sql/DropTables.sql" );

        System.out.println( "\nDone." );
    }

    protected String getList() {
        StringBuilder fileList = new StringBuilder();
        File[] files = new File( configPath ).listFiles();

        fileList.append( "     " );
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].getName().contains( ".txt" ) ) {
                fileList.append( files[i].getName() );
                if ( i < ( files.length - 1 ) ) {
                    fileList.append( ", " );
                }
                if ( i % 5 == 0 && i != 0 ) {
                    fileList.append( "\n     " );
                }
            }

        }

        return fileList.toString();
    }

    /**
     * This method calls the appropriate generators for BO's, XMLs, DAOs,
     * Domains, SQL and session factory, along with all relevant tests.
     * 
     * @param table
     */
    protected void generateAll( Table table ) {

        XmlGenerator xml = new XmlGenerator( table, databaseType );
        printPath( xml.createXml() );

        BoGenerator bo = new BoGenerator( table );
        printPath( bo.createBo() );

        DaoGenerator dao = new DaoGenerator( table );
        printPath( dao.createDao() );

        DomainGenerator domain = new DomainGenerator( table );
        printPath( domain.createDomain() );

        // SqlGenerator sql = new SqlGenerator( table, databaseType );
        SqlGenerator sql = new SqlGenerator( table );
        printPath( sql.createSql() );

        TestDaoGenerator testDao = new TestDaoGenerator( table );
        printPath( testDao.createTestDao() );

        TestBoGenerator testBo = new TestBoGenerator( table );
        printPath( testBo.createTestBo() );

    }

    private void printPath( String file ) {
        fileCount++;
        if ( file == null ) {
            System.out.println( fileCount
                    + ". MyBatis configuration already exists." );
            return;
        }

        System.out.println( fileCount + ". " + file );
    }
}
