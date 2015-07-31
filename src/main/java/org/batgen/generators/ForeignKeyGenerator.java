package org.batgen.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.batgen.Column;
import org.batgen.ForeignNode;
import org.batgen.Table;

public class ForeignKeyGenerator {
    private ArrayList<ForeignNode> foreignNodeList;
    private HashMap<String, Table> tableMap;

    public ForeignKeyGenerator( ArrayList<ForeignNode> foreignNodeList,
            HashMap<String, Table> tableMap ) {
        this.foreignNodeList = foreignNodeList;
        this.tableMap = tableMap;
    }

    public void createForeignKeys() {
        StringBuilder sb = new StringBuilder();
        sb.append( writeAddForeignKeys() );
        GenUtil.writeToFile( "sql/_AlterTables.sql", sb.toString() );
        sb = new StringBuilder();
        sb.append( writeDropForeignKeys() );
        GenUtil.appendToFile( "sql/_DropTables.sql", sb.toString() );
    }

    /**
     * Write the add foreign key constraints
     */
    private String writeAddForeignKeys() {
        StringBuilder sb = new StringBuilder();
        Table fromTable;
        Table toTable;
        ArrayList<String> fromFields = new ArrayList<String>();
        ArrayList<String> toFields = new ArrayList<String>();

        for ( ForeignNode node : foreignNodeList ) {
            fromTable = tableMap.get( node.getFromTable() );
            toTable = tableMap.get( node.getToTable() );

            if ( fromTable != null && toTable != null ) {
            	// populate the form fields using the from table
                for ( Column col : fromTable.getColumns() ) {
                    for ( String fromFieldValue : node.getFromFields() ) {
                        if ( col.getFldName().equals( fromFieldValue )) {
                            fromFields.add( col.getColName() );
                            break;
                        }
                    }
                }
             // populate the to fields using the to table
                for ( Column col : toTable.getColumns() ) {
                    for ( String toFieldValue : node.getToFields() ) {
                        if ( col.getFldName().equals( toFieldValue ) ) {
                            toFields.add( col.getColName() );
                            break;
                        }
                    }
                }

                if ( !( fromFields.isEmpty() || toFields.isEmpty() )
                        && fromFields.size() == toFields.size() ) {
                    String fromParam = "";
                    String fkName = "";
                    // List the foreign key(s) of the current table
                    for ( String field : fromFields ) {
                        fromParam += field + ", ";
                        fkName += field.toUpperCase() + "_";
                    }
                    fromParam = fromParam.substring( 0, fromParam.length() - 2 );

                    // List the foreign key(s) of the table being referenced
                    String toParam = "";
                    for ( String field : toFields ) {
                        toParam += field + ", ";
                    }
                    toParam = toParam.substring( 0, toParam.length() - 2 );

                    sb.append( "ALTER TABLE " + fromTable.getTableName() );
                    sb.append( " ADD CONSTRAINT FK_" + fromTable.getTableName() + "_" + fkName
                            + "TO_" + toTable.getTableName() );
                    sb.append( " FOREIGN KEY (" + fromParam + ") " );
                    sb.append( "REFERENCES " + toTable.getTableName() + "(" + toParam + ");\n" );

                    fromFields.clear();
                    toFields.clear();
                } else
                    throw new IllegalArgumentException( "In Table " + node.getFromTable()
                            + " and/or " + node.getToTable()
                            + ", either the field names are wrong or don't exist for foreign keys." );
            } else
                throw new IllegalArgumentException( "Either the tables names ( "
                        + node.getFromTable() + " and/or " + node.getToTable()
                        + " ) are wrong or don't exist for foreign keys." );
        }

        sb.append( "\n-- PROTECTED CODE -->" );
        List<String> lines = GenUtil.getProtectedLines( "sql/_AlterTables.sql" );
        if ( lines.isEmpty() ) {
            sb.append( "\n" );
        }
        for ( String line : lines ) {
            sb.append( line );
        }
        return sb.toString();
    }

    /**
     * Write the drop foreign key constraints
     */
    private String writeDropForeignKeys() {
        StringBuilder sb = new StringBuilder();
        Table fromTable;
        Table toTable;
        ArrayList<String> fromFields = new ArrayList<String>();
        ArrayList<String> toFields = new ArrayList<String>();

        for ( ForeignNode node : foreignNodeList ) {
            fromTable = tableMap.get( node.getFromTable() );
            toTable = tableMap.get( node.getToTable() );

            if ( fromTable != null && toTable != null ) {
                for ( Column col : fromTable.getColumns() ) {
                    for ( String fromFieldValue : node.getFromFields() ) {
                        if ( col.getFldName().equals( fromFieldValue ) ) {
                            fromFields.add( col.getColName() );
                            break;
                        }
                    }
                }
                for ( Column col : toTable.getColumns() ) {
                    for ( String toFieldValue : node.getToFields() ) {
                        if ( col.getFldName().equals( toFieldValue )) {
                            toFields.add( col.getColName() );
                            break;
                        }
                    }
                }

                if ( !( fromFields.isEmpty() || toFields.isEmpty() )
                        && fromFields.size() == toFields.size() ) {

                    String fkName = "";
                    for ( String field : fromFields ) {
                        fkName += field.toUpperCase() + "_";
                    }

                    sb.append( "ALTER TABLE " + fromTable.getTableName() );
                    sb.append( " DROP CONSTRAINT FK_" + fromTable.getTableName() + "_" + fkName
                            + "TO_" + toTable.getTableName() + ";\n" );
                }

            }
            fromFields.clear();
            toFields.clear();
        }

        sb.append( "\n" );

        return sb.toString();
    }
}
