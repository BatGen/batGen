package org.batgen;

import java.util.ArrayList;

public class ForeignNode {
    private String fromTable;
    private ArrayList<String> fromFields;
    private String toTable;
    private ArrayList<String> toFields;

    public ForeignNode(String fromTable, ArrayList<String> fromFields, String toTable, ArrayList<String> toFields){
        this.setFromTable( fromTable );
        this.setFromFields( fromFields );
        this.setToTable( toTable );
        this.setToFields( toFields );
    }

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable( String fromTable ) {
        this.fromTable = fromTable;
    }

    public ArrayList<String> getFromFields() {
        return fromFields;
    }

    public void setFromFields( ArrayList<String> fromFields ) {
        this.fromFields = fromFields;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable( String toTable ) {
        this.toTable = toTable;
    }

    public ArrayList<String> getToFields() {
        return toFields;
    }

    public void setToFields( ArrayList<String> toFields ) {
        this.toFields = toFields;
    }

}