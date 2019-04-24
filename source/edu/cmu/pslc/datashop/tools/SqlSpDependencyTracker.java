package edu.cmu.pslc.datashop.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.mutable.MutableInt;

import edu.cmu.pslc.datashop.tools.SqlDependencyMatrix.DbObjectEntry;
import edu.cmu.pslc.datashop.tools.SqlDependencyMatrix.DependencyMapEntry;

/**
 * Sql SP dependency tracker.
 * @author ysahn
 */
public class SqlSpDependencyTracker {

    public static final String DELIMITER = "$$";

    public static final String TYPE_COMMENT = "COMMENT";

    public static final String WORD_CALL = "CALL";
    public static final String WORD_SELECT = "SELECT";
    public static final String WORD_FROM = "FROM";
    public static final String WORD_JOIN = "JOIN";
    public static final String WORD_UPDATE = "UPDATE";
    public static final String WORD_SET = "SET";
    public static final String WORD_INSERT = "INSERT";
    public static final String WORD_DELETE = "DELETE";

    public static final String SINGLE_TOKENS = "()+-=';,";

    private SqlDependencyMatrix dependencyMatrix = new SqlDependencyMatrix();

    private Stack<DbObjectEntry> procedureStack = new Stack<DbObjectEntry>();
    private Stack<DbObjectEntry> tableStack = new Stack<DbObjectEntry>();

    private Set<String> callsToIgnore = new HashSet<String>();

    int lineNum = 0;

    int _LOG_LEVEL = 0; // 1=info, 2=debug

    public Set<String> getCallsToIgnore() {
        return callsToIgnore;
    }

    public void setIgnoreCallsCvs(String callsCsv) {
        callsToIgnore.addAll(Arrays.asList(callsCsv.split(",")));
    }

    public SqlDependencyMatrix getDependencyTable() {
        return dependencyMatrix;
    }

    public void setDependencyTable(SqlDependencyMatrix dependencyTable) {
        this.dependencyMatrix = dependencyTable;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public SqlSpDependencyTracker() {
    }

    public void analyze(String filename) throws FileNotFoundException {
        InputStream is = new FileInputStream(filename);
        analyze(is);
    }

    public void analyze(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line = null;
        StringBuffer sentence = new StringBuffer();
        do {
            this.lineNum++;
            try {
                line = reader.readLine();
                if (line == null)
                    continue;

                line = line.trim();
                if (line.length() > 0) {
                    sentence.append(' ').append(line);
                    if (isEndOfSentence(line)) {
                        logDebug(sentence.toString());
                        // sentence contains a complete sentence
                        processStatement(sentence.toString().trim());
                        sentence = new StringBuffer();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } while (line != null);

        buildDependencies(); // second phase is needed as the procedures can be
                             // referencing undeclared procedures/
    }

    boolean isEndOfSentence(String line) {

        if (line.endsWith(DELIMITER) // delimiter
                || line.endsWith("BEGIN") // procedure
                || line.endsWith("END") // procedure
                || line.endsWith(";") // statement
                || line.endsWith("*/") // comment
                || line.endsWith("DO") // WHILE statement
                || line.endsWith("THEN") // IF statement
                || line.endsWith("ELSE") // IF statement
        )
            return true;
        return false;

    }

    public void processStatement(String sentence) {
        try {
            if (sentence.startsWith("CREATE PROCEDURE")) {
                processCreateProcedure(sentence);
            } else if (sentence.startsWith("CREATE FUNCTION")) {
                processCreateFunction(sentence);
            } else if (sentence.startsWith("CREATE TABLE")) {
                processCreateTable(sentence);
            } else if (sentence.startsWith("DROP TABLE")) {
                processDropTable(sentence);
            } else if (sentence.startsWith("SELECT")) {
                processSelect(sentence, 0);
            } else if (sentence.startsWith("UPDATE")) {
                processUpdate(sentence);
            } else if (sentence.startsWith("INSERT")) {
                processInsert(sentence);
            } else if (sentence.startsWith("DELETE")) {
                processDelete(sentence);
            } else if (sentence.startsWith(WORD_CALL)) {
                processCall(sentence);
            } else if (sentence.startsWith("END")) {
                processEnd(sentence);
            } else if (sentence.startsWith("/*")) {
                processComment(sentence);
            }
        } catch (Exception e) {
            System.err.println("Error near line [" + this.lineNum + "]");
            e.printStackTrace();
        }
    }

    public void processComment(String sentence) {
        sentence = sentence.trim();

        // find first non space/
        int pos = 3;
        char ch = sentence.charAt(pos);
        while (" -=".indexOf(ch) >= 0) {
            ch = sentence.charAt(pos++);
        }
        int startPos = pos - 1;

        pos = sentence.length() - 3;
        ch = sentence.charAt(pos);
        while (" -=".indexOf(ch) >= 0) {
            ch = sentence.charAt(pos--);
        }
        int endPos = pos + 2;

        sentence = sentence.substring(startPos, endPos);
        this.getDependencyTable().createObject(sentence, TYPE_COMMENT,
                this.lineNum);
    }

    public void processCrateProcedureFuncProc(String sentence, String type) {
        String procedureName = extractFirstObjectName(sentence, type, 0, null);

        DbObjectEntry dbObject = this.dependencyMatrix.putObject(procedureName,
                type.toLowerCase(), this.lineNum);
        dbObject.setDeclLineNum(this.lineNum); // Update the line number if was
                                               // previously called

        if (this.getDependencyTable().getLastCreatedObject() != null
                && this.getDependencyTable().getLastCreatedObject().getType()
                        .equals(TYPE_COMMENT)) {
            dbObject.setComment(this.getDependencyTable()
                    .getLastCreatedObject().getName());
        }

        logInfo("PUSHING " + dbObject.getName());
        this.procedureStack.push(dbObject);
        // Ignore the rest
    }

    public void processCreateProcedure(String sentence) {
        processCrateProcedureFuncProc(sentence, "PROCEDURE");

    }

    public void processCreateFunction(String sentence) {
        processCrateProcedureFuncProc(sentence, "FUNCTION");

    }

    public void processCreateTable(String sentence) {
        final String TABLE = "TABLE";
        final String NOT_EXISTS = "NOT EXISTS";

        String objectName = extractFirstObjectName(sentence, NOT_EXISTS, 0,
                null);
        if (objectName == null) {
            objectName = extractFirstObjectName(sentence, TABLE, 0, null);
        }

        // Adds the table to the dependency matrix
        DbObjectEntry dbObject = this.dependencyMatrix.putObject(objectName,
                SqlDependencyMatrix.TYPE_TABLE, this.lineNum);
        dbObject.setDeclLineNum(this.lineNum);

        // Adds the enclosing procedure as object that this table depends on
        if (!this.procedureStack.empty()) {
            DbObjectEntry caller = this.procedureStack.peek();
            dbObject.addDependent("create", "creator", caller, this.lineNum);
            caller.addDependsOn("create", "created", dbObject.getName(),
                    this.lineNum);

            logDebug(dbObject.getName());
        }

        // In case there is a SELECT sentence that populates the table
        int selectPos = this.indexOfFirstMatch(sentence, WORD_SELECT, 0);
        if (selectPos > 0) {
            String selectSentence = sentence.substring(selectPos);
            this.tableStack.push(dbObject);
            processSelect(selectSentence, 0);
            this.tableStack.pop();
        }

    }

    public void processDropTable(String sentence) {

    }

    public void processSelect(String sentence, int depth) {
        processSelect(sentence, 0, 0);
    }

    /**
     * Process the select statement Condition: the sentences should have a FROM
     * clause, or be a nested select, ie. with parenthesis.
     *
     * @param sentence
     * @param depth
     * @param startPos
     */
    public void processSelect(String sentence, int depth, int startPos) {
        // form of FROM ( <another select sentence) )
        // TODO: use regexp instead of hard coded string indexOf
        int parenthesisOpenPos = this.indexOfFirstMatch(sentence, "FROM (",
                startPos);
        MutableInt lastPos = new MutableInt(0);
        if (parenthesisOpenPos > 0) {
            String subSentence = extractParenthesized(sentence,
                    parenthesisOpenPos, lastPos);
            processSelect(subSentence, depth + 1);
        } else {
            // normal path: find the table name
            String tableName = extractFirstObjectName(sentence, WORD_FROM,
                    startPos, null);

            // Adding dependency to the caller procedure
            if (!this.procedureStack.empty()) {
                this.logDebug("Adding table as select '" + tableName
                        + "' from '" + sentence + "'");
                DbObjectEntry caller = this.procedureStack.peek();
                caller.addDependsOn("read", "select", tableName, this.lineNum);
            }

            // If it's a nested select, add dependency to the table
            if (!this.tableStack.empty()) {
                DbObjectEntry caller = this.tableStack.peek();
                caller.addDependsOn("read", "select", tableName, this.lineNum);
            }
        }

        // Process JOIN's
        processJoin(sentence, depth, lastPos);

        // Process WHERE .. IN ()
        processWhere(sentence, depth, lastPos);

    }

    /**
     * Process JOIN clauses.
     *
     * @param clause
     *            - the clause that starts with 'JOIN'
     * @param depth
     * @param lastPos
     */
    public void processJoin(String clause, int depth, MutableInt lastPos) {
        int joinCtr = 0;
        int joinPos = this.indexOfFirstMatch(clause, WORD_JOIN,
                lastPos.intValue());

        if (joinPos > 0) {
            this.logDebug("Start processing JOIN[joinPos=" + joinPos
                    + ",lastPos=" + lastPos + "]: " + clause.substring(joinPos));
        }

        while (joinPos > 0) {
            this.logDebug("\n Processing JOIN [joinCtr=" + (++joinCtr)
                    + ", depth=" + depth + "]{joinPos=" + joinPos + ",lastPos="
                    + lastPos + "}: " + clause.substring(joinPos));

            // there is a inner clause
            String nextToken = SqlSpDependencyTracker.getNextToken(clause,
                    joinPos + WORD_JOIN.length(), lastPos);
            if (nextToken.startsWith("(")) {
                // nested select in JOIN
                int parenthesisOpenPos = lastPos.intValue() - 1; // instead of
                                                                 // lastPos
                String subSentence = extractParenthesized(clause,
                        parenthesisOpenPos, lastPos);
                processSelect(subSentence, depth + 1);
            } else {
                String objectName = extractFirstObjectName(clause, WORD_JOIN,
                        joinPos, lastPos);

                // add to the dependsOn of the procedure
                if (!this.procedureStack.empty()) {
                    this.logDebug("Adding table read by join '" + objectName
                            + "' from '" + clause + "'");
                    DbObjectEntry caller = this.procedureStack.peek();
                    caller.addDependsOn("read", "join", objectName,
                            this.lineNum);
                }
                // If it's a nested select, add dependency to the table
                if (!this.tableStack.empty()) {
                    DbObjectEntry caller = this.tableStack.peek();
                    caller.addDependsOn("read", "join", objectName,
                            this.lineNum);
                }

            }
            joinPos = this.indexOfFirstMatch(clause, WORD_JOIN,
                    lastPos.intValue());
            if (joinPos > 0) {
                this.logDebug("\n  JOIN indexOf{joinPos=" + joinPos
                        + ",lastPos=" + lastPos + "}: ");
            }
        }
    }

    public void processWhere(String clause, int depth, MutableInt lastPos) {
        int wherePos = this.indexOfFirstMatch(clause, "WHERE",
                lastPos.intValue());

        if (wherePos >= 0) {
            // we only care about (SELECT
            int selectPos = this.indexOfFirstMatch(clause, "(SELECT", wherePos);
            if (selectPos >= 0) {
                String subSentence = extractParenthesized(clause, selectPos,
                        lastPos);
                processSelect(subSentence, depth + 1);
            }
        }
    }

    public void processUpdate(String sentence) {
        MutableInt lastPos = new MutableInt(0);
        String objectName = extractFirstObjectName(sentence, WORD_UPDATE, 0,
                lastPos);

        DbObjectEntry dbObject = this.dependencyMatrix.putObject(objectName,
                SqlDependencyMatrix.TYPE_TABLE, this.lineNum);

        // Adds the enclosing procedure as object that this table depends on
        if (!this.procedureStack.empty()) {
            DbObjectEntry caller = this.procedureStack.peek();
            caller.addDependsOn("update", "update", dbObject.getName(),
                    this.lineNum);
            dbObject.addDependent("update", "update", caller, this.lineNum);
            logDebug(dbObject.getName());
        }

        // UPDATE <table> SET <col> = <val>: NOTHING TO DO, THERE IS NO
        // DEPENDENCY

        // UPDATE <table> JOIN <tables> SET ...
        int joinPos = this.indexOfFirstMatch(sentence, WORD_JOIN,
                lastPos.intValue());
        if (joinPos >= 0) {
            processJoin(sentence, joinPos, lastPos);
        }

        // UPDATE <table> SET <col> = (SELEC <clause>)
        int setPos = this.indexOfFirstMatch(sentence, WORD_SET,
                lastPos.intValue());
        if (setPos < 0) {
            throw new IllegalArgumentException("UPDATE without SET near line "
                    + this.lineNum);
        }

        int assignPos = this.indexOfFirstMatch(sentence, "=",
                lastPos.intValue());

        int wherePos = this.indexOfFirstMatch(sentence, "WHERE ",
                lastPos.intValue());
        if (wherePos < 0) {
            wherePos = Integer.MAX_VALUE;
        }
        while (assignPos > 0 && assignPos < wherePos) {
            String nextToken = SqlSpDependencyTracker.getNextToken(sentence,
                    assignPos + 1, lastPos);
            if (nextToken.equals("(")) {
                nextToken = SqlSpDependencyTracker.getNextToken(sentence,
                        lastPos.intValue(), lastPos);
                if (WORD_SELECT.equalsIgnoreCase(nextToken)) {
                    int parenthesisOpenPos = this.indexOfFirstMatch(sentence,
                            "(", lastPos.intValue()); // instead of lastPos
                    String subSentence = extractParenthesized(sentence,
                            parenthesisOpenPos, lastPos);
                    processSelect(subSentence, 0);
                }
            }
            assignPos = this.indexOfFirstMatch(sentence, "=",
                    lastPos.intValue());
        }

        // Process WHERE
        processWhere(sentence, 0, lastPos);

    }

    /**
     * Process the insert statement.
     *
     * @param sentence
     */
    public void processInsert(String sentence) {
        // INSERT INTO curriculum (cols) VALUES (vals);
        MutableInt lastPos = new MutableInt(0);
        String objectName = extractFirstObjectName(sentence, " INTO", 0,
                lastPos);

        DbObjectEntry dbObject = this.dependencyMatrix.putObject(objectName,
                SqlDependencyMatrix.TYPE_TABLE, this.lineNum);

        // Adds the enclosing procedure as object that this table depends on
        if (!this.procedureStack.empty()) {
            DbObjectEntry caller = this.procedureStack.peek();
            caller.addDependsOn("insert", "insert", dbObject.getName(),
                    this.lineNum);
            dbObject.addDependent("insert", "insert", caller, this.lineNum);
            logDebug(dbObject.getName());
        }

        // Find the end of the first parenthesis
        String nextToken = SqlSpDependencyTracker.getNextToken(sentence,
                lastPos.intValue(), lastPos);
        if (nextToken.equals("(")) {
            String dummy = this.extractParenthesized(sentence,
                    lastPos.intValue() - 1, lastPos);
        }

        // just ignore VALUES clause

        // Handle optional SELECT clause (it will take care of the rest, e.g.
        // JOIN)
        nextToken = SqlSpDependencyTracker.getNextToken(sentence,
                lastPos.intValue(), lastPos);
        if (nextToken.equalsIgnoreCase("SELECT")) {
            this.processSelect(sentence, lastPos.intValue(), lastPos.intValue());
        }

    }

    /**
     * Process the DELETE sentence.
     *
     * @param sentence
     */
    public void processDelete(String sentence) {
        // DELETE table name;
        MutableInt lastPos = new MutableInt(0);
        String objectName = extractFirstObjectName(sentence, WORD_FROM, 0,
                lastPos);

        DbObjectEntry dbObject = this.dependencyMatrix.putObject(objectName,
                SqlDependencyMatrix.TYPE_TABLE, this.lineNum);

        // Adds the enclosing procedure as object that this table depends on
        if (!this.procedureStack.empty()) {
            DbObjectEntry caller = this.procedureStack.peek();
            caller.addDependsOn("delete", "delete", dbObject.getName(),
                    this.lineNum);
            dbObject.addDependent("delete", "delete", caller, this.lineNum);
            logDebug(dbObject.getName());
        }

        // Process JOIN's (if any)
        processJoin(sentence, 0, lastPos);

        // Process WHERE .. IN ()
        processWhere(sentence, 0, lastPos);
    }

    /**
     * Process the CALL statement.
     *
     * @param sentence
     */
    public void processCall(String sentence) {

        MutableInt lastPos = new MutableInt(0);
        String objectName = extractFirstObjectName(sentence, WORD_CALL, 0,
                lastPos);

        DbObjectEntry dbObject = this.dependencyMatrix.putObject(objectName,
                SqlDependencyMatrix.TYPE_PROCEDURE, this.lineNum);

        if (this.callsToIgnore.contains(objectName)) {
            return;
        }

        // Adds the enclosing procedure as object that this table depends on
        if (!this.procedureStack.empty()) {
            DbObjectEntry caller = this.procedureStack.peek();
            caller.addDependsOn("callee", "call", dbObject.getName(),
                    this.lineNum);
            dbObject.addDependent("caller", "call", caller, this.lineNum);
            logDebug(dbObject.getName());
        }

    }

    public void processEnd(String sentence) {
        // If it ends with the delimiter, is end of function or procedure
        if (sentence.endsWith(DELIMITER)) {
            if (!this.procedureStack.empty()) {
                DbObjectEntry dbObject = this.procedureStack.peek();
                logInfo("POPPING " + dbObject.getName());
            } else {
                logInfo("POPPING empty stack!!!");
            }

            this.procedureStack.pop();
        }
    }

    void buildDependencies() {
        for (DbObjectEntry dbObject : this.getDependencyTable().getObjectMap()
                .values()) {

            for (DependencyMapEntry dbDependsOnEntry : dbObject.getDependsOn()) {
                DbObjectEntry referencedObj = this.getDependencyTable()
                        .getObject(dbDependsOnEntry.getObjectName());
                dbDependsOnEntry.setObject(referencedObj);
            }
            /*
             * NOT NEEDED: this association is done in the first phase for
             * (DependencyMapEntry dbDependant: dbObject.getDependants()) {
             * DbObjectEntry referencedObj =
             * this.getDependencyTable().getObject(dbDependant.getObjectName());
             * dbDependant.setObject(referencedObj); }
             */
        }
    }

    /**
     *
     * @param text
     *            the text where to look for the pattern
     * @param pattern
     * @param startPos
     *            the starting position
     * @return
     */
    public final int indexOfFirstMatch(String text, String pattern, int startPos) {
        // TODO: change this to regex pattern matching
        return text.indexOf(pattern, startPos);
    }

    /**
     * Returns the next token (skips the white spaces, and returns the next
     * substring until space again).
     *
     * @param text
     * @param startPos
     * @param lastPos
     * @return
     */
    public static final String getNextToken(String text, int startPos,
            MutableInt lastPos) {
        if (startPos > text.length() - 1) {
            return null;
        }
        int pos = startPos;
        int tokenStartPos = startPos;

        boolean isSymbolToken = false;
        while (pos < text.length()) {
            if (SINGLE_TOKENS.indexOf(text.charAt(pos)) >= 0) {
                tokenStartPos = pos;
                pos++;
                isSymbolToken = true;
                break;
            } else if (!isSpace(text.charAt(pos))) {
                tokenStartPos = pos;
                break;
            }
            pos++;
        }

        if (!isSymbolToken) {
            while (pos < text.length()) {
                if (isSpace(text.charAt(pos))) {
                    break;
                }
                pos++;
            }
        }
        lastPos.setValue(pos);

        return text.substring(tokenStartPos, pos);
    }

    /**
     * Returns the first object name found right after the keyword (e.g. after
     * "TABLE" for table name in create sentence).
     *
     * @param sentence
     * @param keyword
     * @param startPos
     *            the starting position to look for the name
     * @param lastPos
     *            (out param) the last position of the name
     * @return
     */
    public String extractFirstObjectName(String sentence, String keyword,
            int startPos, MutableInt lastPos) {
        int fnNameStartPos = this.indexOfFirstMatch(sentence, "`", startPos);
        if (fnNameStartPos < 0) {
            int keywordStartPos = sentence.indexOf(keyword, startPos);
            if (keywordStartPos >= 0) {
                fnNameStartPos = findNextNonSpace(sentence, keywordStartPos
                        + keyword.length() + 1) - 1;
            } else {
                return null; // could not find the type (e.g. "TABLE") there is
                             // no point on going further
            }
        }

        int fnNameEndPos = this.indexOfFirstMatch(sentence, "`",
                fnNameStartPos + 1);
        if (fnNameEndPos < 0) {
            fnNameEndPos = findNextNonAlphaNumUnderscore(sentence,
                    fnNameStartPos + 1) - 1;
        }

        String objectName = sentence
                .substring(fnNameStartPos + 1, fnNameEndPos);
        if (lastPos != null) {
    lastPos.setValue(fnNameEndPos);
        }
        return objectName;
    }

    /**
     * Must start with the parenthesis.
     *
     * @param sentence
     * @param startPos
     * @param lastPos
     * @return
     */
    public String extractParenthesized(String sentence, int startPos,
            MutableInt lastPos) {
        String retval = null;

        int openParenthesisPos = sentence.indexOf("(", startPos);
        int currPos = openParenthesisPos;

        if (currPos > 0) {
            currPos++;
            int openCount = 1;
            while (openCount > 0) {
                char ch = sentence.charAt(currPos);
                if (ch == '(') {
                    openCount++;
                }
                if (ch == ')') {
                    openCount--;
                }
                currPos++;
            }
            retval = sentence.substring(openParenthesisPos, currPos);
            if (lastPos != null) {
                lastPos.setValue(currPos);
            }
        }
        return retval;
    }

    public static final boolean isSpace(char ch) {
        return (ch == ' ' || ch == '\t') ? true : false;
    }

    int findNextNonSpace(String sentence, int startPos) {
        int pos = startPos;
        // DBG
        char ch = sentence.charAt(pos);
        while (isSpace(sentence.charAt(pos))) {
            ch = sentence.charAt(pos);
            pos++;
        }
        return pos;
    }

    public final int findNextSpace(String sentence, int startPos) {
        int pos = startPos;
        while (!isSpace(sentence.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    public final int findNextNonAlphaNumUnderscore(String sentence, int startPos) {
        int pos = startPos;
        char ch = sentence.charAt(pos);
        while (Character.isJavaIdentifierPart(ch) || ch == '.') {
            ch = sentence.charAt(pos++);
        }
        return pos;
    }

    public void logDebug(String message) {
        if (_LOG_LEVEL >= 2) {
            System.out.println("[DBG:" + this.lineNum + "] " + message);
        }
    }

    public void logInfo(String message) {
        if (_LOG_LEVEL >= 1) {
            System.out.println("[DBG:" + this.lineNum + "] " + message);
        }
    }
}
