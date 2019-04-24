package edu.cmu.pslc.datashop.workflows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThreadedStreamReader implements Runnable {

    private List<String> streamAsList = null;
    private InputStreamReader inStreamReader = null;
    private BufferedReader bufferedReader = null;

    public ThreadedStreamReader(InputStream inStream) {
        streamAsList = new ArrayList<String>();
        inStreamReader = new InputStreamReader(inStream);
        bufferedReader = new BufferedReader(inStreamReader);
    }

    public void run() {
        String line = null;

        try {

            // Read the standard output stream
            while ((line = bufferedReader.readLine()) != null) {
                streamAsList.add(line);
            }

            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e) {

        } finally {

        }
    }

    public List<String> getStringBuffer() {
        return streamAsList;
    }


}
