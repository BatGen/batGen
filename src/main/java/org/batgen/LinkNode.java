package org.batgen;

public class LinkNode {
    
    private String linkInTable;
    private String tableOne;
    private String tableTwo;
    
    public LinkNode(String linkInTable, String tableOne, String tableTwo) {
        super();
        this.linkInTable = linkInTable;
        this.tableOne = tableOne;
        this.tableTwo = tableTwo;
    }
    
    public String getLinkInTable() {
        return linkInTable;
    }
    public void setLinkInTable(String linkInTable) {
        this.linkInTable = linkInTable;
    }
    public String getTableOne() {
        return tableOne;
    }
    public void setTableOne(String tableOne) {
        this.tableOne = tableOne;
    }
    public String getTableTwo() {
        return tableTwo;
    }
    public void setTableTwo(String tableTwo) {
        this.tableTwo = tableTwo;
    }

    
}