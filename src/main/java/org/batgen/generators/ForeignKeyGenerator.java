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

    public ForeignKeyGenerator( ArrayList<ForeignNode> foreignNodeList, HashMap<String, Table> tableMap ) {
        this.foreignNodeList = foreignNodeList;
        this.tableMap = tableMap;
    }

    public String createForeignKeys() {
        StringBuilder sb = new StringBuilder();
        boolean fromFieldExist = false;
        boolean toFieldExist = false;
        Table fromTable;
        Table toTable;
        String fromField = "";
        String toField = "";

        for ( ForeignNode node : foreignNodeList ) {
            fromTable = tableMap.get( node.getFromTable() );
            toTable = tableMap.get( node.getToTable() );
            fromFieldExist = false;
            toFieldExist = false;

            if ( fromTable != null && toTable != null ) {
                for ( Column col : fromTable.getColumns() ) {
                    if ( col.getFldName().equals( node.getFromField() ) ) {
                        fromFieldExist = true;
                        fromField = col.getColName();
                        break;
                    }
                }
                for ( Column col : toTable.getColumns() ) {
                    if ( col.getFldName().equals( node.getToField() ) ) {
                        toFieldExist = true;
                        toField = col.getColName();
                        break;
                    }
                }

                if ( fromFieldExist && toFieldExist ) {
                    sb.append( "ALTER TABLE " + fromTable.getTableName() );
                    sb.append( " ADD CONSTRAINT FK_" + fromTable.getTableName() + "_" + fromField );
                    sb.append( " FOREIGN KEY (" + fromField + ") " );
                    sb.append( "REFERENCES " + toTable.getTableName() + "(" + toField + ");\n" );
                }
                else
                    throw new IllegalArgumentException( "In Table" + node.getFromTable() + " and/or "
                            + node.getToTable() + ", either the field names are wrong or don't exist for foreign keys." );
            }
            else
                throw new IllegalArgumentException( "Either the tables names ( " + node.getFromTable() + " and/or "
                        + node.getToTable() + " ) are wrong or don't exist for foreign keys." );
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

    public String createDropForeignKeys() {
        StringBuilder sb = new StringBuilder();
        boolean fromFieldExist = false;
        Table fromTable;
        String fromField = "";

        for ( ForeignNode node : foreignNodeList ) {
            fromTable = tableMap.get( node.getFromTable() );
            fromFieldExist = false;
            for ( Column col : fromTable.getColumns() ) {
                if ( col.getFldName().equals( node.getFromField() ) ) {
                    fromFieldExist = true;
                    fromField = col.getColName();
                    break;
                }
            }

            if ( fromFieldExist ) {
                sb.append( "ALTER TABLE " + fromTable.getTableName() );
                sb.append( " DROP CONSTRAINT FK_" + fromTable.getTableName() + "_" + fromField + ";\n" );
            }
        }
        return sb.toString();
    }
}
