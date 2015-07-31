package org.batgen.generators;

import java.util.ArrayList;
import java.util.HashMap;

import org.batgen.Column;
import org.batgen.ForeignNode;
import org.batgen.LinkNode;
import org.batgen.Parser;
import org.batgen.Table;

public class LinkGenerator {

    private ArrayList<LinkNode> links;
    private HashMap<String, Table> tableMap;

    public LinkGenerator( ArrayList<LinkNode> linkList, HashMap<String, Table> tableMap ) {
        this.links = linkList;
        this.tableMap = tableMap;
    }

    public void createLinks() {

        for ( LinkNode link : links ) {

            Table inTable = tableMap.get( link.getLinkInTable() );
            Table tableOne = tableMap.get( link.getTableOne() );
            Table tableTwo = tableMap.get( link.getTableTwo() );

            if ( inTable != null && tableOne != null && tableTwo != null ) {

                for ( Column c : inTable.getColumns() ) {
                    if ( c.isKey() ) {
                        throw new IllegalArgumentException(
                                "\n There can be no primary keys when LINKing. " + "Check CLASS "
                                        + inTable.getTableName() + " and remove all !'s" );
                    }
                }

                ArrayList<String> fromFields = new ArrayList<String>();
                ArrayList<String> toFields = new ArrayList<String>();

                for ( Column c : tableOne.getColumns() ) {
                    if ( c.isKey() ) {
                        Column newColumn = new Column( c );

                        newColumn.setTable( inTable );
                        newColumn.setColName( tableOne.getTableName() + "_" + c.getColName() );
                        newColumn.setFldName( Parser.capsToCamel( tableOne.getTableName() + "_"
                                + c.getColName() ) );
                        newColumn.setLinkGenerated( true );
                        inTable.addColumn( newColumn );

                        toFields.add( c.getFldName() );
                        fromFields.add( newColumn.getFldName() );

                    }
                }

                ForeignNode fn1 = new ForeignNode( inTable.getTableName(), fromFields, tableOne
                        .getTableName(), toFields );
                Parser.getForeignKeyList().add( fn1 );

                inTable.addFN( fn1 );
                
                fromFields = new ArrayList<String>();
                toFields = new ArrayList<String>();

                for ( Column c : tableTwo.getColumns() ) {
                    if ( c.isKey() ) {

                        Column newColumn = new Column( c );
                        newColumn.setTable( inTable );

                        if ( tableOne.getTableName().equals( tableTwo.getTableName() ) ) {
                            newColumn.setColName( "OTHER_" + tableTwo.getTableName() + "_"
                                    + c.getColName() );
                            newColumn.setFldName( Parser.capsToCamel( "OTHER_"
                                    + tableTwo.getTableName() + "_" + c.getColName() ) );
                        } else {
                            newColumn.setColName( tableTwo.getTableName() + "_" + c.getColName() );
                            newColumn.setFldName( Parser.capsToCamel( tableTwo.getTableName() + "_"
                                    + c.getColName() ) );
                        }

                        newColumn.setLinkGenerated( true );
                        inTable.addColumn( newColumn );

                        toFields.add( c.getFldName() );
                        fromFields.add( newColumn.getFldName() );
                    }
                }

                ForeignNode fn2 = new ForeignNode( inTable.getTableName(), fromFields, tableTwo
                        .getTableName(), toFields );
                Parser.getForeignKeyList().add( fn2 );

                inTable.addFN( fn2 );
                
                inTable.setTableOne( tableOne );
                inTable.setTableTwo( tableTwo );
                inTable.setManyToMany( true );

            } else {
                throw new IllegalArgumentException( "\nError linking classes. Check your LINKs" );
            }

        }

    }
}