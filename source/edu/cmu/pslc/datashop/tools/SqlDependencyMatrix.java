package edu.cmu.pslc.datashop.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SqlDependencyMatrix {

    public static final String TYPE_PROCEDURE = "procedure";
    public static final String TYPE_FUNCTION = "function";
    public static final String TYPE_TABLE = "table";

    public SqlDependencyMatrix() {
    }

    private Map<String, DbObjectEntry> objectMap = new LinkedHashMap<String, DbObjectEntry>();

    private DbObjectEntry lastCreatedObject = null;

    public DbObjectEntry getLastCreatedObject() {
        return lastCreatedObject;
    }

    public DbObjectEntry getObject(String name) {
        return objectMap.get(name);
    }

    public DbObjectEntry putObject(String name, String type, int declLineNum) {
        DbObjectEntry objectEntry = null;
        if (objectMap.containsKey(name)) {
            objectEntry = objectMap.get(name);
        } else {
            objectEntry = new DbObjectEntry(name, type, declLineNum);
            objectMap.put(name, objectEntry);
            this.lastCreatedObject = objectEntry;
        }
        return objectEntry;
    }

    /**
     * This was added for comment (I need to know if the previous object added
     * was a comment).
     *
     * @param name
     * @param type
     * @param declLineNum
     * @return
     */
    public DbObjectEntry createObject(String name, String type, int declLineNum) {
        this.lastCreatedObject = new DbObjectEntry(name, type, declLineNum);
        return this.lastCreatedObject;
    }

    public Map<String, DbObjectEntry> getObjectMap() {
        return objectMap;
    }

    public void setObjectMap(Map<String, DbObjectEntry> objectMap) {
        this.objectMap = objectMap;
    }

    public class DbObjectEntry {
        public String name;
        public String type; // procedure, function, table
        public int declLineNum;

        public String comment = null;

        // Those that depends on this object (eg. caller)
        List<DependencyMapEntry> dependsOn = new ArrayList<DependencyMapEntry>();

        // Those that this objects depends on (eg. callee)
        List<DependencyMapEntry> dependants = new ArrayList<DependencyMapEntry>();

        public void addDependsOn(DependencyMapEntry dependsOn) {
            this.dependsOn.add(dependsOn);
        }

        public void addDependsOn(String type, String method,
                String dependsOnObjName, int lineNum) {
            DependencyMapEntry dependsOn = new DependencyMapEntry(type);
            dependsOn.setDependencyMethod(method);
            dependsOn.setObjectName(dependsOnObjName);
            dependsOn.setLineNum(lineNum);
            this.dependsOn.add(dependsOn);
        }

        public void addDependsOn(String type, String method,
                DbObjectEntry dbObj, int lineNum) {
            DependencyMapEntry dependsOn = new DependencyMapEntry(type);
            dependsOn.setDependencyMethod(method);
            dependsOn.setObject(dbObj);
            dependsOn.setObjectName(dbObj.getName());
            dependsOn.setLineNum(lineNum);
            this.dependsOn.add(dependsOn);
        }

        public void addDependent(DependencyMapEntry dependent) {
            this.dependants.add(dependent);
        }

        public void addDependent(String type, String method,
                DbObjectEntry dbObj, int lineNum) {
            DependencyMapEntry dependent = new DependencyMapEntry(type);
            dependent.setDependencyMethod(method);
            dependent.setObject(dbObj);
            dependent.setObjectName(dbObj.getName());
            dependent.setLineNum(lineNum);
            this.dependants.add(dependent);
        }

        public DbObjectEntry(String name, String type, int declLineNum) {
            this.name = name;
            this.type = type;
            this.declLineNum = declLineNum;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getDeclLineNum() {
            return declLineNum;
        }

        public void setDeclLineNum(int declLineNum) {
            this.declLineNum = declLineNum;
        } // line number that this object was declared

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public List<DependencyMapEntry> getDependsOn() {
            return dependsOn;
        }

        public void setDependsOn(List<DependencyMapEntry> dependsOn) {
            this.dependsOn = dependsOn;
        }

        public List<DependencyMapEntry> getDependants() {
            return dependants;
        }

        public void setDependants(List<DependencyMapEntry> dependants) {
            this.dependants = dependants;
        }

    }

    public class DependencyMapEntry {
        public String dependencyType; // create, read, update, insert, delete,
                                      // call
        public String dependencyMethod; // create, select, join, update, call,
                                        // etc.
        public String objectName; // this is the name of the object until the
                                  // reference is obtained in the 2nd phase
        public DbObjectEntry object;
        public int lineNum;

        public DependencyMapEntry(String depType) {
            dependencyType = depType;
        }

        public String getDependencyType() {
            return dependencyType;
        }

        public void setDependencyType(String dependencyType) {
            this.dependencyType = dependencyType;
        }

        public String getDependencyMethod() {
            return dependencyMethod;
        }

        public void setDependencyMethod(String dependencyMethod) {
            this.dependencyMethod = dependencyMethod;
        }

        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        public DbObjectEntry getObject() {
            return object;
        }

        public void setObject(DbObjectEntry object) {
            this.object = object;
        }

        public int getLineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }

    }
}
