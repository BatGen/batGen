package org.batgen;

public class ForeignNode {
    private String fromTable;
    private String fromField;
    private String toTable;
    private String toField;

    public ForeignNode(String fromTable, String fromField, String toTable, String toField){
        this.setFromTable( fromTable );
        this.setFromField( fromField );
        this.setToTable( toTable );
        this.setToField( toField );
    }

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable( String fromTable ) {
        this.fromTable = fromTable;
    }

    public String getFromField() {
        return fromField;
    }

    public void setFromField( String fromField ) {
        this.fromField = fromField;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable( String toTable ) {
        this.toTable = toTable;
    }

    public String getToField() {
        return toField;
    }

    public void setToField( String toField ) {
        this.toField = toField;
    }
}