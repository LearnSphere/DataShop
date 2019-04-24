package edu.cmu.pslc.datashop.tools;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.cmu.pslc.datashop.tools.SqlDependencyMatrix.DbObjectEntry;
import edu.cmu.pslc.datashop.tools.SqlDependencyMatrix.DependencyMapEntry;

public class SqlDependencyReporterHtml {

    public static final String NEWLINE = System.getProperty("line.separator");
    // public static final int MAX_RECURSE_DEPTH = 7;

    public static final String DEPENDS_ON = "depends on";
    public static final String DEPENDENTS = "dependents";
    public static final int OPT_DEPENDS_ON = 1;
    public static final int OPT_DEPENDENTS = 2;

    private SqlDependencyMatrix matrix = null;

    private HashMap<String, Integer> objectTypeStat = new LinkedHashMap<String, Integer>();

    private int maxRecurseDepth = 3;
    private String indent = "\t";

    private String inFilename = null;
    private Set<String> onlyTypes = null;
    private Set<String> onlyNames = null;
    private int displayOption = 3; // bit-wise option (both: depends on &
                                   // dependents)

    /** To avoid repeated traversal. */
    private HashMap<String, DbObjectEntry> traversedObjects = new HashMap<String, DbObjectEntry>(); 

    public SqlDependencyReporterHtml(SqlDependencyMatrix matrix) {
        this.matrix = matrix;
    }

    public HashMap<String, Integer> getObjectTypeStat() {
        return objectTypeStat;
    }

    public void setObjectTypeStat(HashMap<String, Integer> objectTypeStat) {
        this.objectTypeStat = objectTypeStat;
    }

    public int getMaxRecurseDepth() {
        return maxRecurseDepth;
    }

    public void setMaxRecurseDepth(int maxRecurseDepth) {
        this.maxRecurseDepth = maxRecurseDepth;
    }

    public String getIndent() {
        return indent;
    }

    public String getIndent(int depth) {
        StringBuffer retval = new StringBuffer(indent);
        for (int i = 1; i < depth; i++) {
            retval.append(indent);
        }
        return retval.toString();
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public Set<String> getOnlyTypes() {
        return onlyTypes;
    }

    public void setOnlyTypes(Set<String> onlyTypes) {
        this.onlyTypes = onlyTypes;
    }

    public Set<String> getOnlyNames() {
        return onlyNames;
    }

    public void setOnlyNames(Set<String> onlyNames) {
        this.onlyNames = onlyNames;
    }

    boolean hasDisplayOption(int opt) {
        return (this.displayOption & opt) > 0;
    }

    /**
     * [filename] <-ignore:[comma separated function names]> <-types:[comma
     * separated type names]> Where -ignore : ignores the object to parse -types
     * : displays only this type of object -depth : The Recursion depth -only :
     * shows only object of this name(s).
     *
     * @param args
     */
    public static void main(String[] args) {
        Properties commandArgs = SqlDependencyReporterHtml
                .processCommandArgs(args);

        String filename = commandArgs.getProperty("filename", null);
        String outFilename = null;
        int recurseDepth = 3;
        if (filename != null) {

            Set<String> types = new HashSet<String>();
            Set<String> names = new HashSet<String>();
            int options = 0;
            SqlSpDependencyTracker depTracker = new SqlSpDependencyTracker();
            if (!commandArgs.containsKey("filename")
                    || commandArgs.containsKey("?")) {
                System.out
                        .println("SqlDependencyReport <options> <source file>");
                System.out.println("Where possible options include:");
                System.out.println("\t-ignore:{CSV list of calls to ignore}");
                System.out.println("\t-types:{CSV list of types to show}");
                System.out
                        .println("\t-names:{CSV list of object names to show}");
                System.out
                        .println("\t-depth:{number}  Recursion depth to display");
                System.out
                        .println("\t-dependson   Show only objects that depends on");
                System.out
                        .println("\t-dependants  Show only dependents objects");
                System.exit(0);
            }
            if (commandArgs.containsKey("ignore")) {
                depTracker.setIgnoreCallsCvs(commandArgs.getProperty("ignore"));
            }
            if (commandArgs.containsKey("types")) {
                types.addAll(Arrays.asList(commandArgs.getProperty("types")
                        .split(",")));
            }
            if (commandArgs.containsKey("names")) {
                names.addAll(Arrays.asList(commandArgs.getProperty("names")
                        .split(",")));
            }
            if (commandArgs.containsKey("depth")) {
                recurseDepth = Integer.parseInt(commandArgs.getProperty(
                        "depth", String.valueOf(recurseDepth)));
            }
            if (commandArgs.containsKey("dependents")) {
                options = options | OPT_DEPENDENTS;
            }
            if (commandArgs.containsKey("dependson")) {
                options = options | OPT_DEPENDS_ON;
            }
            if (commandArgs.containsKey("out")) {
                outFilename = commandArgs.getProperty("out");
            }

            try {
                depTracker.analyze(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            SqlDependencyReporterHtml reporter = new SqlDependencyReporterHtml(
                    depTracker.getDependencyTable());

            reporter.setMaxRecurseDepth(recurseDepth);
            reporter.setOnlyTypes(types);
            reporter.setOnlyNames(names);

            Writer writer = null;
            if (outFilename != null) {
                try {
                    writer = new BufferedWriter(new FileWriter(outFilename));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                writer = new OutputStreamWriter(System.out);
            }

            try {
                reporter.outputHeader(writer, filename);
                reporter.outputSummary(writer, filename, depTracker);
                reporter.outputFooter(writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Done.");
            if (outFilename != null) {
                System.out.println("Output: " + outFilename);
            }
        }

    }

    public static Properties processCommandArgs(String[] args) {
        Properties retval = new Properties();

        /*
         * Last argument must be filename
         */
        if (args.length > 0) {
            retval.setProperty("filename", args[args.length - 1]);
        }

        if (args.length > 1) {
            for (int i = 0; i < args.length - 1; i++) {
                System.out.println(args[i]);
                if (args[i].startsWith("-")) {
                    String[] tokens = args[i].split("=");
                    String argName = tokens[0].substring(1);
                    String argValue = "";
                    if (tokens.length > 1) {
                        argValue = tokens[1];
                    }
                    retval.setProperty(argName, argValue);
                }
            }
        }

        return retval;
    }

    public void outputDependencies(Writer writer) throws IOException {
        for (DbObjectEntry dbObject : matrix.getObjectMap().values()) {

            // Count number of objects per type
            if (objectTypeStat.containsKey(dbObject.getType())) {
                Integer count = objectTypeStat.get(dbObject.getType());
                objectTypeStat.put(dbObject.getType(), new Integer(count + 1));
            } else {
                objectTypeStat.put(dbObject.getType(), new Integer(1));
            }

            // Filter by type. If null, then retrieve all, otherwise just the
            // object type in the set
            if (shouldFilterIn(onlyTypes, dbObject.getType())
                    && shouldFilterIn(onlyNames, dbObject.getName())) {

                writer.write(dbObject.getDeclLineNum() + ": ["
                        + dbObject.getType() + "] <a name='"
                        + dbObject.getName() + "'>" + dbObject.getName()
                        + "</a>");
                if (StringUtils.isNotEmpty(dbObject.getComment())) {
                    writer.write("<div class=\"comment\">");
                    writer.write(dbObject.getComment());
                    writer.write("</div>");
                }

                traversedObjects.put(dbObject.getName(), dbObject);

                if (hasDisplayOption(OPT_DEPENDS_ON)
                        && dbObject.getDependsOn().size() > 0) {
                    // outputDependencies(dbObject.getDependsOn(), DEPENDS_ON,
                    // writer);
                    outputDependenciesRecursive(dbObject,
                            dbObject.getDependsOn(), DEPENDS_ON, writer, 1);
                }

                if (hasDisplayOption(OPT_DEPENDENTS)
                        && dbObject.getDependants().size() > 0) {
                    // outputDependencies(dbObject.getDependants(), DEPENDENTS,
                    // writer);
                    outputDependenciesRecursive(dbObject,
                            dbObject.getDependants(), DEPENDENTS, writer, 1);
                }
            }
        }
    }

    /**
     * Outputs the dependencies using depth first recursion.
     *
     * @param dbDependencyEntries
     * @param type
     * @param writer
     * @param depth
     * @throws IOException
     */
    private void outputDependenciesRecursive(DbObjectEntry parentDbObj,
            List<DependencyMapEntry> dbDependencyEntries, String type,
            Writer writer, int depth) throws IOException {
        if (depth > maxRecurseDepth) {
            return;
        }

        if (dbDependencyEntries.size() == 0) {
            return;
        }
        writer.write(NEWLINE);
        writer.write("<ul>");
        // writer.write(type+" {{");writer.write(NEWLINE);
        for (DependencyMapEntry dbDependencyEntry : dbDependencyEntries) {

            String objType = (dbDependencyEntry.getObject() != null) ? dbDependencyEntry
                    .getObject().getType() : "";

            if (!shouldFilterIn(onlyTypes, objType)
                    || !shouldFilterIn(onlyNames, objType)) {
                continue;
            }

            writer.write("<li>");
            String descriptionLine = dbDependencyEntry.getLineNum() + ":["
                    + dbDependencyEntry.getDependencyType() + "] "
                    + dbDependencyEntry.getObjectName();
            if (dbDependencyEntry.getDependencyType().equals("create")) {
                writer.write("<span class='access_create'>");
                writer.write(descriptionLine);
                writer.write("</span>");
            } else if (dbDependencyEntry.getDependencyType().equals("update")) {
                writer.write("<span class='access_update'>");
                writer.write(descriptionLine);
                writer.write("</span>");
            } else if (dbDependencyEntry.getDependencyType().equals("insert")) {
                writer.write("<span class='access_insert'>");
                writer.write(descriptionLine);
                writer.write("</span>");
            } else {
                writer.write(descriptionLine);
            }

            // CHeck whether the object has already been printed:
            String traversedKey = dbDependencyEntry.getObjectName();
            if (traversedObjects.containsKey(traversedKey)) {
                DbObjectEntry dbObjectEntry = traversedObjects
                        .get(traversedKey);
                writer.write(" #{see <a href='#" + dbObjectEntry.getName()
                        + "'>decl at line " + dbObjectEntry.getDeclLineNum()
                        + "</a>" + "}");
            } else {
                DbObjectEntry dbObjectEntry = this.matrix
                        .getObject(dbDependencyEntry.getObjectName());
                if (dbObjectEntry != null) {
                    writer.write(" #{see <a href='#" + dbObjectEntry.getName()
                            + "'>decl at line "
                            + dbObjectEntry.getDeclLineNum() + "</a>" + "}");
                }

                if (dbObjectEntry != null) {
                    traversedObjects
                            .put(dbObjectEntry.getName(), dbObjectEntry);
                    if (type.equals(DEPENDS_ON)) {
                        outputDependenciesRecursive(dbObjectEntry,
                                dbObjectEntry.getDependsOn(), DEPENDS_ON,
                                writer, depth + 1);
                    } else {
                        outputDependenciesRecursive(dbObjectEntry,
                                dbObjectEntry.getDependants(), DEPENDENTS,
                                writer, depth + 1);
                    }
                }
            }
            writer.write("</li>");
            writer.write(NEWLINE);
        }
        writer.write("</ul>");
        // writer.write("}} "+type
        // +" // "+parentDbObj.getName()+"");writer.write(NEWLINE);
    }

    public void outputHeader(Writer writer, String title) throws IOException {
        writer.write("<html><head>");
        writer.write("<title>" + title + "</title>");
        writer.write("<style type='text/css'> <!--");
        writer.write("body {}");
        writer.write("h1 {}");
        writer.write(".comment {font:11px arial,sans-serif;}");
        writer.write(".access_create {font-weight:bold; color:green}");
        writer.write(".access_read {}");
        writer.write(".access_update {font-style:italic;}");
        writer.write(".access_insert {font-style:italic; font-weight:bold;}");
        writer.write(".access_delete {font:italic;}");
        writer.write("</style></head><body>");
    }

    public void outputFooter(Writer writer) throws IOException {
        writer.write("</body></body>");
    }

    public void outputSummary(Writer writer, String inFilename,
            SqlSpDependencyTracker depTracker) throws IOException {
        writer.write("<h2>" + inFilename + "</h2>");
        writer.write(NEWLINE);
        writer.write("PARMS {<ul>");
        writer.write(NEWLINE);
        writer.write("<li>depth:" + getMaxRecurseDepth());
        writer.write("</li>" + NEWLINE);
        if (depTracker.getCallsToIgnore() != null
                && !depTracker.getCallsToIgnore().isEmpty()) {
            writer.write("<li>ignore:"
                    + StringUtils.join(
                            depTracker.getCallsToIgnore().iterator(), ","));
            writer.write("</li>" + NEWLINE);
        }
        if (this.getOnlyNames() != null && !this.getOnlyNames().isEmpty()) {
            writer.write("<li>names:"
                    + StringUtils.join(this.getOnlyNames().iterator(), ","));
            writer.write("</li>" + NEWLINE);
        }
        if (this.getOnlyTypes() != null && !this.getOnlyTypes().isEmpty()) {
            writer.write("<li>types:"
                    + StringUtils.join(this.getOnlyTypes().iterator(), ","));
            writer.write("</li>" + NEWLINE);
        }

        writer.write("}</ul>");
        writer.write(NEWLINE);
        writer.write("<h2> Result </h2>");
        writer.write(NEWLINE);
        try {
            writer.write(this.reportTextToString());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        writer.write("<h2> Stat </h2>");
        writer.write(NEWLINE);
        writer.write("<ul><li>Lines: " + depTracker.getLineNum());
        writer.write("</li>" + NEWLINE);
        for (Map.Entry<String, Integer> statEntry : this.getObjectTypeStat()
                .entrySet()) {
            writer.write("<li>" + statEntry.getKey() + "s: "
                    + statEntry.getValue().toString());
            writer.write("</li>" + NEWLINE);
        }
        writer.write("</ul>");

    }

    public String reportTextToString() throws IOException {
        StringWriter stringWriter = new StringWriter();

        outputDependencies(stringWriter);

        return stringWriter.toString();
    }

    private boolean shouldFilterIn(Set<String> container, String element) {
        return (container == null || container.isEmpty() || onlyTypes
                .contains(element));
    }

}
