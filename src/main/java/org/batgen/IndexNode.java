package org.batgen;

import java.util.List;

public class IndexNode {
    private String       indexName;
    private List<Column> columnList;

    public IndexNode( String indexName, List<Column> columnList ) {
        this.setIndexName( indexName );
        this.setColumnList( columnList );
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName( String indexName ) {
        this.indexName = indexName;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList( List<Column> columnList ) {
        this.columnList = columnList;
    }
}
