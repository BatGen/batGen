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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.batgen.Column;
import org.batgen.ForeignNode;
import org.batgen.Table;

import static org.batgen.generators.GenUtil.*;

public class TestBoGenerator extends Generator {

    private String daoName = "";
    private boolean getCreatedCalled = false;
    private String filePath;
    private String compoundKeys = "";


    public TestBoGenerator( Table table ) {
        super( table );
        this.daoName = "Test" + table.getDomName() + "Bo";
        filePath = "src/test/java/" + packageToPath() + "/bo/" + daoName
                + ".java";
        for ( Column column : table.getColumns() ) {
            if ( column.isKey() ) {
            	compoundKeys += toJavaCase(table.getDomName()) + ".get" + toTitleCase(column.getFldName()) + "(), ";           
            }
        }
        compoundKeys = compoundKeys.substring(0, compoundKeys.length() - 2);
    }

    public String createTestBo() {
        StringBuilder sb = new StringBuilder();

        sb.append( createHeading() );
        sb.append( createImports() );
        sb.append( createClassHeader() );
        sb.append( createFields() );
        sb.append( createTestAll() );
        sb.append( getInit() );
        sb.append( getPopulateAndVerify() );
        sb.append( getUpdateAndVerify() );
        sb.append( getDeleteAndVerify() );
        sb.append( getJoinAndVerify() );
        sb.append( getMakeItem() );
        sb.append( getTestEquals() );
        sb.append( getProtectedJavaLines( filePath ) );

        writeToFile( filePath, sb.toString() );
        return filePath;
    }

    private String createHeading() {
        return "package " + pkg + ".bo;\n\n";
    }

    private String createImports() {

        ImportGenerator imports = new ImportGenerator( filePath );
        imports.addImport( "import static org.junit.Assert.*;" );
        imports.addImport( "import org.junit.*;" );


        imports.addImport( "import " + pkg + ".domain.*;" );
        imports.addImport( "import " + pkg + ".dao.*;" );
        imports.addImport( "import " + pkg + ".util.*;" );
        imports.addImport( "import java.util.Date;" );
        imports.addImport( "import java.util.List;" );
        imports.addImport("import test.app.sample.util.Database;");
        return imports.toString();
    }

    private String createClassHeader() {
        return "public class Test" + table.getDomName() + "Bo {\n";
    }
    
    private String createFields() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAB + "private " + table.getDomName() + " " + "itemOne;\n");
        sb.append(TAB + "private " + table.getDomName() + " " + "itemTwo;\n");
        sb.append("\n");
        
        for ( String str : table.getForeignDomNames()) {
        	sb.append(TAB + "private Test" + str + "Bo " + 
        			str  + "TestBo;\n");
        }
        return sb.toString();
    }

    private String createTestAll() {
        StringBuilder sb = new StringBuilder();

        sb.append( "\n" + TAB + "@Test\n" );
        sb.append( TAB + "public void testAll() {\n" );
        sb.append( TAB + TAB + "Database.initDatabase();\n" );
        sb.append( TAB + TAB + "init();\n" );
        sb.append( "\n" );
        sb.append( TAB + TAB + "populateAndVerify();\n" );
        sb.append( TAB + TAB + "updateAndVerify();\n" );
        if(table.isManyToMany()) {
        	sb.append( TAB + TAB + "joinAndVerify();\n" );
        }
        sb.append( TAB + TAB + "deleteAndVerify();\n" );
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }
    
    private String getInit() {
        StringBuilder sb = new StringBuilder();

        sb.append( TAB + "public void init() {\n" );
        
        
        for ( String str : table.getForeignDomNames() ) {
        	sb.append(TAB + TAB + str + "TestBo = new Test" + 
        			str + "Bo();\n");
        	if (!str.equals(table.getDomName())) {
        			sb.append(TAB + TAB + str + "TestBo.init();\n");
        	}
        }
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }

    private String getPopulateAndVerify() {
        StringBuilder sb = new StringBuilder();

        sb.append( TAB + "public void populateAndVerify() {\n" );
       
        sb.append( TAB + TAB + "if ( Database.hasBeenUsed( this.getClass() ) ) {\n" );
        sb.append( TAB + TAB + TAB + "return;\n" );
        sb.append( TAB + TAB + "}\n\n" );
        sb.append( TAB + TAB + "Database.setAsUsed( this.getClass() );\n\n" );
        
        sb.append( TAB + TAB + table.getDomName() + "Bo bo = " + table.getDomName() + "Bo.getInstance();\n\n" );
        

        for ( String str : table.getForeignDomNames() ) {
        	sb.append(TAB + TAB + str + "TestBo.populateAndVerify();\n");
        }
        
        
        sb.append( TAB + TAB + "itemOne = " + getMakeItemCall(1) + ";\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "int result = bo.create( itemOne );\n" );
        sb.append( TAB + TAB + TAB + "assertEquals( result, 1 );\n" );
        sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + table.getDomName() + " item = null;\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "item = bo.read( ");
        
        //get all keys for args to bo.read()
        ArrayList<Column> cols = table.getKeyColumns();
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemOne.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
        sb.append( TAB + TAB + "} catch (BoException e) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "assertNotNull( item );\n\n" );
        sb.append( TAB + TAB + "testEquals( itemOne, item );\n\n" );
        
        
        
        
        sb.append( TAB + TAB + "itemTwo = " + getMakeItemCall(2) + ";\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "int result = bo.create( itemTwo );\n" );
        sb.append( TAB + TAB + TAB + "assertEquals( result, 1 );\n" );
        sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "item = null;\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "item = bo.read( ");
        
        //get all keys for args to bo.read()
        cols = table.getKeyColumns();
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemTwo.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
        sb.append( TAB + TAB + "} catch (BoException e) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "assertNotNull( item );\n\n" );
        sb.append( TAB + TAB + "testEquals( itemTwo, item );\n\n" );
        
        
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }
    
    private String getUpdateAndVerify() {
        StringBuilder sb = new StringBuilder();

        sb.append( TAB + "public void updateAndVerify() {\n" );
       
        sb.append( TAB + TAB + table.getDomName() + "Bo bo = " + table.getDomName() + "Bo.getInstance();\n\n" );
        
        //set random values for all non-primary key values
        List<Column> allCols = table.getColumns();

        
        for ( int i = 0; i < allCols.size(); i++) {
        	if ( !allCols.get(i).isKey() ) {
        		
        		String str = getLast(allCols.get(i));
        		sb.append( TAB + TAB + "itemOne.set" + capitalize( allCols.get(i).getFldName()) + 
        				"( " + str + " );\n" );
        	}
        }
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "int result = bo.update( itemOne );\n" );
        sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + table.getDomName() + " item = null;\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "item = bo.read( ");
        
        //get all keys for args to bo.read()
        ArrayList<Column> cols = table.getKeyColumns();
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemOne.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
        sb.append( TAB + TAB + "} catch (BoException e) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "assertNotNull( item );\n\n" );
        sb.append( TAB + TAB + "testEquals( itemOne, item );\n\n" );
        
        
        for ( int i = 0; i < allCols.size(); i++) {
        	if ( !allCols.get(i).isKey() ) {
        		
        		String str = getLast(allCols.get(i));
        		sb.append( TAB + TAB + "itemTwo.set" + capitalize( allCols.get(i).getFldName()) + 
        				"( " + str + " );\n" );
        	}
        }
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "int result = bo.update( itemTwo );\n" );
        sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "item = null;\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "item = bo.read( ");
        
        //get all keys for args to bo.read()
        cols = table.getKeyColumns();
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemTwo.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
        sb.append( TAB + TAB + "} catch (BoException e) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "assertNotNull( item );\n\n" );
        sb.append( TAB + TAB + "testEquals( itemTwo, item );\n\n" );
        
        
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }
    
    private String getDeleteAndVerify() {
        StringBuilder sb = new StringBuilder();

        sb.append( TAB + "public void deleteAndVerify() {\n" );
       
        sb.append( TAB + TAB + table.getDomName() + "Bo bo = " + table.getDomName() + "Bo.getInstance();\n\n" );
        
        
        
        
        sb.append( TAB + TAB + "try {\n" );
        
        sb.append( TAB + TAB + TAB + "bo.delete( ");
        
        //get all keys for args to bo.read()
        ArrayList<Column> cols = table.getKeyColumns();
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemOne.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
      	
        sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        
        
        
        
        sb.append( TAB + TAB + "try {\n" );
        
        sb.append( TAB + TAB + TAB + "bo.delete( ");
        
        //get all keys for args to bo.read()
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemTwo.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
      	
        sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        
        
        sb.append( TAB + TAB + "//verify that items were deleted" );
        
        
        sb.append( TAB + TAB + table.getDomName() + " item = null;\n\n" );
        
        sb.append( TAB + TAB + table.getDomName() + " item = null;\n\n" );
        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "item = bo.read( ");
        
        //get all keys for args to bo.read()
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemOne.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
        sb.append( TAB + TAB + "} catch (BoException e) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "assertNull( item );\n\n" );
        
        
        


        
        sb.append( TAB + TAB + "try {\n" );
        sb.append( TAB + TAB + TAB + "item = bo.read( ");
        
        //get all keys for args to bo.read()
    	for ( int i = 0; i < cols.size(); i++) {
    		sb.append("itemTwo.get" + capitalize( cols.get(i).getFldName() ) + "()");
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
      	sb.append(" );\n" );
        sb.append( TAB + TAB + "} catch (BoException e) {\n" );
        sb.append( TAB + TAB + TAB + "fail (e.getMessage());\n" );
        sb.append( TAB + TAB + "}\n\n" );
        
        sb.append( TAB + TAB + "assertNull( item );\n\n" );
        
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }
    
    private String getJoinAndVerify() {
    	StringBuilder sb = new StringBuilder();
    	// only generate the TestBo for many-to-many tables
    	if (table.isManyToMany()) {
    		Table one = table.getTableOne();
            Table two = table.getTableTwo();
    		sb.append( TAB + "public void joinAndVerify() {\n" );
    		sb.append( TAB + TAB + table.getDomName() + "Bo bo = " + table.getDomName() + "Bo.getInstance();\n\n" );
    	        
    		sb.append( TAB + TAB + "try {\n" );
    	    
    		// First join method test
    		sb.append( TAB + TAB + TAB + "List<" + two.getDomName() + ">" + two.getDomName() + "List");
    	    sb.append(" = bo.get" + two.getDomName() + "ListBy" + one.getDomName() + "Key(" );
    	    		
    	    //get all keys for args to bo.read()
    	    String params = "";
    	    for ( Column col : one.getColumns() ) {
                if ( col.isKey() ) {
                	params += "itemOne.get" + one.getDomName() + capitalize(  col.getFldName() ) + "(), ";
                }
        	}
            params = params.substring( 0, params.length() - 2 );
    	    sb.append(params);
    	    sb.append(")" + ";\n" );
    	    
    	    sb.append( TAB + TAB + TAB + "assertEquals(1, " + two.getDomName() + "List.size());\n");
    	    
    	    for ( Column col : two.getColumns() ) {
                if ( col.isKey() ) {
		    	    sb.append( TAB + TAB + TAB + "assertTrue(" + two.getDomName() + "List.get(0).get");
		    	    sb.append( capitalize(col.getFldName()) + "().equals(" );
		    	    sb.append( getFirst(col) );
		    		sb.append("));\n");
                }
    	    }
     	   
    	  
    	 // Second join method test
    		sb.append( TAB + TAB + TAB + "List<" + one.getDomName() + ">" + one.getDomName() + "List");
    	    sb.append(" = bo.get" + one.getDomName() + "ListBy" + two.getDomName() + "Key(" );
    	    		
    	    //get all keys for args to bo.read()
    	    params = "";
    	    for ( Column col : two.getColumns() ) {
                if ( col.isKey() ) {
                	params += "itemOne.get" + two.getDomName() + capitalize(  col.getFldName() ) + "(), ";
                }
        	}
            params = params.substring( 0, params.length() - 2 );
    	    sb.append(params);
    	    sb.append(")" + ";\n" );
    	    
    	    sb.append( TAB + TAB + TAB + "assertEquals(1, " + one.getDomName() + "List.size());\n");
    	    
    	    for ( Column col : one.getColumns() ) {
                if ( col.isKey() ) {
		    	    sb.append( TAB + TAB + TAB + "assertTrue(" + one.getDomName() + "List.get(0).get");
		    	    sb.append( capitalize(col.getFldName()) + "().equals(" );
		    	    sb.append( getFirst(col) );
		    		sb.append("));\n");
                }
    	    }
     	   
    	    
    	    sb.append( TAB + TAB + "} catch (BoException e1) {\n" );
    	    sb.append( TAB + TAB + TAB + "fail (e1.getMessage());\n" );
    	    sb.append( TAB + TAB + "}\n\n" );
    		 
    		 
    		sb.append( TAB + "}\n\n" );
    	}
    	
    	return sb.toString();
    }
    
    private String getTestEquals() {
    	StringBuilder sb = new StringBuilder();

        sb.append( TAB + "public void testEquals( " + table.getDomName() + " one, "
        		+ table.getDomName() + " two) {\n" );
        
        List<Column> cols = table.getColumns();
        for ( int i = 0; i < cols.size(); i++) {
     		if (cols.get(i).getFldType().equals("Date")) {
     			sb.append( TAB + TAB + "GenUtil.assertDateEquals( one.get" + capitalize(cols.get(i).getFldName()) + 
         				"(), two.get" + capitalize(cols.get(i).getFldName()) + "() );\n" );
     		}
     		else { 
     			sb.append( TAB + TAB + "assertEquals( one.get" + capitalize(cols.get(i).getFldName()) + 
         				"(), two.get" + capitalize(cols.get(i).getFldName()) + "() );\n" );
     		}
        	
        }
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }
    
    private String getMakeItem() {
    	StringBuilder sb = new StringBuilder();

        sb.append( TAB + "public " + table.getDomName() + " makeItem( " );
         
        ArrayList<Column> keys = table.getKeyColumns();
     	for ( int i = 0; i < keys.size(); i++) {
     		sb.append( keys.get(i).getFldType() + " " + keys.get(i).getFldName() );
     		if ( i < keys.size() - 1) {
     			sb.append(", ");
     		}
     	}      		
        sb.append(") {\n" );
        
        sb.append( TAB + TAB + table.getDomName() + " item = new " + table.getDomName() + "();\n\n" );
        
        //set all values for keys
        for ( int i = 0; i < keys.size(); i++) {
     		sb.append( TAB + TAB + "item.set" + capitalize( keys.get(i).getFldName()) + 
     				"( " + keys.get(i).getFldName() + " );\n" );
        }
        
        //set random values for all non-primary key values
        List<Column> cols = table.getColumns();

        
        for ( int i = 0; i < cols.size(); i++) {
        	if ( !cols.get(i).isKey() ) {
        		
        		String str = getFirst(cols.get(i));
        		sb.append( TAB + TAB + "item.set" + capitalize( cols.get(i).getFldName()) + 
        				"( " + str + " );\n" );
        	}
        }
        
        sb.append( TAB + TAB + "return item;\n" );
        
        sb.append( TAB + "}\n\n" );

        return sb.toString();
    }
    
    private String getFirst ( Column col ) {
    	String str = "";
    	switch ( col.getFldType() ) {
		case "Integer":
			str += 1;
			break;
		case "Long":
			str += "1L";
			break;
		case "String":
			str += "\"A\"";
			break;
		case "Boolean":
			str += true;
			break;
		case "Date":
			str += "new Date(0)";
			break;
		case "byte[]":
			str += "new byte[]{(byte)1}";
			break;
		case "Double":
			str += "1.0";
			break;
		default:
			str = "Type not found : (   See TestBoGenerator.getFirst()";
		}
    	return str;
    }
    
    private String getLast ( Column col ) {
    	String str = "";
    	switch ( col.getFldType() ) {
		case "Integer":
			str += 2;
			break;
		case "Long":
			str += "2L";
			break;
		case "String":
			str += "\"B\"";
			break;
		case "Boolean":
			str += false;
			break;
		case "Date":
			str += "new Date(Long.MAX_VALUE)";
			break;
		case "byte[]":
			str += "new byte[]{(byte)2}";
			break;
		case "Double":
			str += "2.0";
			break;
		default:
			str = "Type not found : (   See TestBoGenerator.getLast()";
		}
    	return str;
    }
    
    private String getMakeItemCall( int itemNum ) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("makeItem( ");
    	
    	ArrayList<Column> cols = table.getKeyColumns();
    	for ( int i = 0; i < cols.size(); i++) {
    		switch ( cols.get(i).getFldType() ) {
    		case "Integer":
    			sb.append(itemNum);
    			break;
    		case "Long":
    			sb.append(itemNum + "L");
    			break;
    		case "String":
    			sb.append( itemNum == 1 ? "\"A\"" : "\"B\"" );
    			break;
    		case "Boolean":
    			sb.append( itemNum == 1 ? true : false );
    			break;
    		case "Date":
    			sb.append( itemNum == 1 ? "new Date(0)" : "new Date(Long.MAX_VALUE)");
    			break;
    		case "byte[]":
    			sb.append( itemNum == 1 ? "new byte[]{(byte)1}" : "new byte[]{(byte)2}");
    			break;
    		case "Double":
    			sb.append( itemNum == 1 ? "1.0" : "2.0");
    			break;
    		default:
    			sb.append("Type not found : (   See TestBoGenerator.getMakeItemCall()");
    		}
    		
    		if ( i < cols.size() - 1) {
    			sb.append(", ");
    		}
    	}
    	
    	sb.append( ")" );
    	return sb.toString();
    }
    
    private String getListBy( String variable ) {
        StringBuilder sb = new StringBuilder();
        if ( hasSearch ) {
            for ( int i = 1; i < searchList.size() + 1; i++ ) {

                sb.append( "\n" + TAB + TAB + "List<" + variable + "> list" + i
                        + "= " );
                sb.append( toJavaCase( variable ) + "Bo.getListBy"
                        + toTitleCase( searchList.get( i - 1 ) ) );
                sb.append( "( " + toJavaCase( variable ) + ".get"
                        + toTitleCase( searchList.get( i - 1 ) ) );
                sb.append( "() ) ; \n" );

                sb.append( TAB + TAB + "assertEquals( 1 , list" + i
                        + ".size() );\n" );

            }
        }
        return sb.toString();
    }

   

    private String toJavaCase( String value ) {
        String line = value;

        line = line.substring( 0, 1 ).toLowerCase() + line.substring( 1 );
        return line;
    }
    

}
