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

public class TestUtilGenerator {

    private String dbFilepath;
    private String genUtilFilepath;
    private String utilPkg;
    private String basePkg;

    private String database;
    private String genUtil;

    public TestUtilGenerator( String pkg ) {

        dbFilepath = "src/test/java/" + pkg.replace( ".", "/" ) + "/util/Database.java";
        genUtilFilepath = "src/test/java/" + pkg.replace( ".", "/" ) + "/util/GenUtil.java";

        basePkg = pkg;
        utilPkg = pkg + ".util";

        database = "package "
                + utilPkg
                + ";\n\nimport java.io.IOException;\nimport java.nio.file.Files;\nimport java.nio.file.Paths;\nimport java.sql.Connection;\nimport java.sql.PreparedStatement;\nimport java.sql.SQLException;\nimport java.util.ArrayList;\nimport java.util.HashMap;\nimport java.util.List;\n\nimport "
                + basePkg
                + ".dao.SessionFactory;\nimport static org.junit.Assert.fail;\n\npublic class Database {\n\t\n\tprivate static String create;\n\tprivate static String alter;\n\t\n\tprivate static List<Class<?>> used;\n\t\n\tstatic {\n\t\ttry {\n\t\t\tcreate = new String(Files.readAllBytes(Paths.get(\"sql/_CreateTables.sql\")));\n\t        alter = new String(Files.readAllBytes(Paths.get(\"sql/_AlterTables.sql\")));\n\t\t} catch (IOException e) {\n\t\t\te.printStackTrace();\n\t\t}\n\t}\n\t\n\tpublic static String getCreateTables() {\n\t\tif (create == null) {\n\t\t\tfail(\"Was unable to read from CreateTables file\");\n\t\t}\n\t\treturn create;\n\t}\n\t\n\t\n\tpublic static String getAlterTables() {\n\t\tif (alter == null) {\n\t\t\tfail(\"Was unable to read from AlterTables file\");\n\t\t}\n\t\treturn alter;\n\t}\n\t\n\t\n\tpublic static void initDatabase() {\n\t\tused = new ArrayList<Class<?>>();\n\t\t\n    \tSessionFactory.initializeForTest();\n    \ttry {  \t\t\n\t    \tConnection c = SessionFactory.getSession().getConnection();\n\t        String create = Database.getCreateTables();\n\t        String alter = Database.getAlterTables();\n\t        String drop = \"DROP ALL OBJECTS;\";\n\t        \n\t        PreparedStatement stmt = c.prepareStatement(drop);\n\t        stmt.execute();\n\t        stmt.close();\n\t        stmt = c.prepareStatement(create);\n\t        stmt.execute();\n\t        stmt.close();\n\t        stmt = c.prepareStatement(alter);\n\t        stmt.execute();\n\t        stmt.close();\n\t        \n\t    } \n    \tcatch (SQLException e)\n    \t{\n    \t\te.printStackTrace();\n    \t}\n\t}\n\t\n\tpublic static boolean hasBeenUsed(Class<?> className) {\n\t\treturn used.contains(className);\n\t}\n\t\n\tpublic static void setAsUsed(Class<?> className) {\n\t\tused.add(className);\n\t}\n\t\n}\n";
        genUtil = "package "
                + utilPkg
                + ";\n\nimport static org.junit.Assert.assertEquals;\n\nimport java.util.Calendar;\nimport java.util.Date;\n\npublic class GenUtil {\n\n    public static void assertDateEquals( Date one, Date two ) {\n        zeroTime( one );\n        zeroTime( two );\n        assertEquals( one.getTime(), two.getTime() );\n    }\n    \n    public static void zeroTime( Date val ) {\n        Calendar cal = Calendar.getInstance();\n        cal.setTime( val );\n        cal.set( Calendar.HOUR_OF_DAY, 0 );\n        cal.set( Calendar.MINUTE, 0 );\n        cal.set( Calendar.SECOND, 0 );\n        cal.set( Calendar.MILLISECOND, 0 );\n        val.setTime( cal.getTime().getTime() );\n    }\n\n}\n";
    }

    public String writeDatabase() {
        writeToFile( dbFilepath, database );
        return dbFilepath;
    }
    
    public String writeGenUtil() {
        writeToFile( genUtilFilepath, genUtil );
        return genUtilFilepath;
    }
    
}