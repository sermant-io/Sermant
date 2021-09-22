package com.lubanops.apm.bootstrap.transaction;

import java.util.HashMap;
import java.util.Map;

public class Transaction {

    private int id;

    private Map<String, String> primaryKeyMap = new HashMap<String, String>();

    public Map<String, String> getPrimaryKeyMap() {
        return primaryKeyMap;
    }

    public void setPrimaryKeyMap(Map<String, String> primaryKeyMap) {
        this.primaryKeyMap = primaryKeyMap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
