// src/main/java/com/vinayak/RestGen/Dto/ApiRequest.java
package com.vinayak.RestGen.Dto;

import java.util.List;

public class ApiRequest {

    private String projectName;
    private String tableName;
    private List<Field> fields;

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public List<Field> getFields() { return fields; }
    public void setFields(List<Field> fields) { this.fields = fields; }

    public static class Field {
        private String name;
        private String type;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
