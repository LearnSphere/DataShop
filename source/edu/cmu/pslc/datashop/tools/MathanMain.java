package edu.cmu.pslc.datashop.tools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */

/**
 * @author mkomisin
 *
 */
public class MathanMain {

    private static final int MAX_COLS = 28;




    /**
     * @param args
     */
    public static void main(String[] args) {
        MathanMain main = new MathanMain();

        main.run();
    }


    /**
     *
     */
    public void run() {
     // mck Read the original table from file.
        List<List<String>> originalRows = readFromFile("C:/temp/LoggerDataCombined.csv");
        if (originalRows != null) {


            List<List<String>> table = getExampleTable(originalRows);
            writeTableTo("C:/temp/MathanConversion.txt", table);

        }
    }

    /**
     *
     * @param filePath
     * @param table
     */
    private void writeTableTo(String filePath, List<List<String>> table) {
        File file = new File(filePath);
        FileWriter fWriter = null;
        BufferedWriter bWriter = null;
        try {
            fWriter = new FileWriter(filePath);
            bWriter = new BufferedWriter(fWriter);

        // Write header.
            String[] newArray = new String[MAX_COLS];
            newArray[0] = "Row";
            newArray[1] = "KC (Skills2)";
            newArray[2] = "KC Category (Skills2)";
            newArray[3] = "KC (Skills3)";
            newArray[4] = "KC Category (Skills3)";
            newArray[5] = "KC (Skills4)";
            newArray[6] = "KC Category (Skills4)";
            newArray[7] = "KC (Skills5)";
            newArray[8] = "KC Category (Skills5)";
            newArray[9] = "KC (Skills6)";
            newArray[10] = "KC Category (Skills6)";
            newArray[11] = "Action";
            newArray[12] = "Anon Student Id";
            newArray[13] = "Attempt at Step";
            newArray[14] = "Condition Name";
            newArray[15] = "Condition Type";
            newArray[16] = "Input";
            newArray[17] = "Level(Section)";
            newArray[18] = "Outcome";
            newArray[19] = "Problem Name";
            newArray[20] = "Time Zone";
            newArray[21] = "Selection";
            newArray[22] = "Session Id";
            newArray[23] = "Step Name";
            newArray[24] = "Student Response Type";
            newArray[25] = "Time";
            newArray[26] = "Total Num Hints";
            newArray[27] = "Tutor Response Type";

            int headerCount = 0;
            for (String val : newArray) {

                bWriter.append(val);
                if (headerCount < newArray.length - 1) {
                    bWriter.append("\t");
                }
                headerCount++;
            }
            bWriter.append("\r\n");


        // Write data.
            int rowCount = 0;
            for (List<String> row : table) {


                int count = 0;
                for (String val : row) {

                    bWriter.append(val);
                    if (count < row.size() - 1) {
                        bWriter.append("\t");
                    }
                }
                bWriter.append("\r\n");
                rowCount++;
            }

            System.out.println(rowCount + " rows written to file.");

            if (bWriter != null) { bWriter.close(); }
            if (fWriter != null) { fWriter.close(); }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }


    }


    /**
     * Reads the csv file containing the log data.
     * @param string
     * @return
     */
    private List<List<String>> readFromFile(String filePath) {
        List<ExampleRow> originalRows = new ArrayList<ExampleRow>();
        File inputFile = null;
        FileReader fReader = null;
        BufferedReader bReader = null;
        try {
            inputFile = new File(filePath);
            fReader = new FileReader(inputFile);
            bReader = new BufferedReader(fReader);

            // For each row, split by tabs,
            // and store the values into an ExampleRow object.
            String readBuffer;
            List<List<String>> table = new ArrayList();
            int rowCounter = 0;

            while ((readBuffer = bReader.readLine()) != null) {
                if (rowCounter == 0) {
                    rowCounter++;
                    // Get headers here
                    continue;
                }
                // Add the values to the row.
                List<String> row = new ArrayList<String>();
                String splitArray[] = readBuffer.split("\t");

                for (String rowValue : splitArray) {


                    row.add(rowValue);
                }

                // Add the example row to list.
                table.add(row);
                rowCounter++;
            }

            if (bReader != null) {
                bReader.close();
            }
            if (fReader != null) {
                fReader.close();
            }

            return table;

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }



    /**
     *
     * @param originalRows
     * @return
     */
    private List<List<String>> getExampleTable(List<List<String>> originalRows) {
        // Create a new example table.
        List<List<String>> exampleTable = new ArrayList<List<String>>();
        // Row counter.
        int rowCounter = 0;
    // For each row in the step rollup
        for (List<String> originalRow : originalRows) {

        // Meta data
            int formulaAttempts = Integer.parseInt(originalRow.get(81));
            int hintRequests = Integer.parseInt(originalRow.get(84));
            int success = Integer.parseInt(originalRow.get(85)) == 0 ? 1 : 0;
            int incorrects = Integer.parseInt(originalRow.get(82));


            int formulaCounter = 0;

            int txs = formulaAttempts + hintRequests;

        // Timing
            int secondsElapsed = Integer.parseInt(originalRow.get(7));
            int secondsPerTx = (int) (secondsElapsed / (double) txs);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yy H:mm:ss");
            Calendar calendar = Calendar.getInstance();

            Date startTime = null;
            try {

                startTime = (Date)formatter.parse(originalRow.get(5));
                calendar.setTime(startTime);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        // Generate new rows
            String[] formulasEntered = new String[17];

            // Count formulas entered to make sure none are missing.
            for (int j = 0; j < 17; j++) {
                formulasEntered[j] = originalRow.get(25 + j);
                if (formulasEntered[j] != null && !formulasEntered[j].isEmpty()) {
                    formulaCounter++;
                }
            }





            // Both types of txs affected
            int attemptAtStepCount = 1;

        // Hint requests
            for (int j = 0; j < hintRequests; j++) {
                String[] newArray = new String[MAX_COLS];
                List<String> newRow = new ArrayList<String>();

             // Outcome
                if (success == 0) {
                    // Outcome
                    newArray[18] = "HINT";
                }
             // Time adjustment
                calendar.add(Calendar.SECOND, secondsPerTx);
                // Time
                newArray[25] = formatter.format(calendar.getTime());
             // Formulas Entered only for formula attempts
                // Input
                newArray[16] = "";
             // Attempt at Step
                newArray[13] = "" + attemptAtStepCount;
                // Don't increment attempt at step because it's a hint
                /// attemptAtStepCount++;

             // Student Response Type
                newArray[24] = "HINT_REQUEST";
            // Tutor Response Type
                newArray[27] = "HINT_MSG";
            // Total Num Hints
                newArray[26] = "1";



                // Row
                newArray[0] = "" + (rowCounter + 1);
                // KC (Skills2)
                newArray[1] = originalRow.get(78);
                // KC Category (Skills2)
                newArray[2] = "";
                // KC (Skills3)
                newArray[3] = originalRow.get(67);
                // KC Category (Skills3)
                newArray[4] = "";
                // KC (Skills4)
                newArray[5] = originalRow.get(64);
                // KC Category (Skills4)
                newArray[6] = "";
                // KC (Skills5)
                newArray[7] = originalRow.get(61);
                // KC Category (Skills5)
                newArray[8] = "";
                // KC (Skills6)
                newArray[9] = originalRow.get(57);
                // KC Category (Skills6)
                newArray[10] = "";
                // Action
                newArray[11] = "";
                // Anon Student Id
                newArray[12] = originalRow.get(44);

                // Condition Name
                newArray[14] = originalRow.get(48);
                // Condition Type
                newArray[15] = "";

                // Level(Section)
                newArray[17] = "Cell Referencing";

                // Problem Name
                newArray[19] = originalRow.get(51);
                // Time Zone
                newArray[21] = "US/Eastern";
                // Selection
                newArray[21] = "";
                // Session Id
                newArray[22] = originalRow.get(46);
                // Step Name
                newArray[23] = "EnterFormula";

                for (int k = 0; k < newArray.length; k++) {
                    newRow.add(newArray[k]);
                }
                exampleTable.add(newRow);
            }


        // Attempts

            for (int j = 0; j < formulaAttempts; j++) {
                String[] newArray = new String[MAX_COLS];
                List<String> newRow = new ArrayList<String>();

            // Outcome


                if (success == 1 || j == (formulaAttempts - 1)) {
                    // Outcome
                    newArray[18] = "CORRECT";
                } else {
                    newArray[18] = "INCORRECT";
                }
             // Time adjustment
                calendar.add(Calendar.SECOND, secondsPerTx);
                // Time
                newArray[25] = formatter.format(calendar.getTime());
             // Formulas Entered only for formula attempts
                // Input
                newArray[16] = formulasEntered[j];
             // Attempt at Step
                newArray[13] = "" + attemptAtStepCount;
                // Increment attempt at step
                attemptAtStepCount++;

             // Student Response Type
                newArray[24] = "ATTEMPT";
            // Tutor Response Type
                newArray[27] = "RESULT";
            // Total Num Hints
                newArray[26] = "0";



             // Row
                newArray[0] = "" + (rowCounter + 1);
                // KC (Skills2)
                newArray[1] = originalRow.get(78);
                // KC Category (Skills2)
                newArray[2] = "";
                // KC (Skills3)
                newArray[3] = originalRow.get(67);
                // KC Category (Skills3)
                newArray[4] = "";
                // KC (Skills4)
                newArray[5] = originalRow.get(64);
                // KC Category (Skills4)
                newArray[6] = "";
                // KC (Skills5)
                newArray[7] = originalRow.get(61);
                // KC Category (Skills5)
                newArray[8] = "";
                // KC (Skills6)
                newArray[9] = originalRow.get(57);
                // KC Category (Skills6)
                newArray[10] = "";
                // Action
                newArray[11] = "";
                // Anon Student Id
                newArray[12] = originalRow.get(44);

                // Condition Name
                newArray[14] = originalRow.get(48);
                // Condition Type
                newArray[15] = "";

                // Level(Section)
                newArray[17] = "Cell Referencing";

                // Problem Name
                newArray[19] = originalRow.get(51);
                // Time Zone
                newArray[21] = "US/Eastern";
                // Selection
                newArray[21] = "";
                // Session Id
                newArray[22] = originalRow.get(46);
                // Step Name
                newArray[23] = "EnterFormula";








                for (int k = 0; k < newArray.length; k++) {

                    newRow.add(newArray[k]);
                }
                exampleTable.add(newRow);
            }

            rowCounter++;
        }
        return exampleTable;

    }




    /**
     *
     * @author mkomisin
     *
     */
    private class ExampleRow {




    }

}
