package org.batgen;

import java.util.List;

public class IndexNode {
    private String       indexName;
    private List<String> varList;
    private List<String> fieldList;

    public IndexNode( String indexName, List<String> varList, List<String> fieldList ) {
        this.setIndexName( indexName );
        this.setVarList( varList );
        this.setFieldList( fieldList );
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName( String indexName ) {
        this.indexName = indexName;
    }

    public List<String> getVarList() {
        return varList;
    }

    public void setVarList( List<String> varList ) {
        this.varList = varList;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public void setFieldList( List<String> fieldList ) {
        this.fieldList = fieldList;
    }

    public String toString() {
        return indexName + " contains fields " + fieldList.toString();
    }
}
