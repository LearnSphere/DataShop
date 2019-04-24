/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.export;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;

/**
 * Read tab-delimited data from a zipped, cached export file.
 * @author Kyle A Cunningham
 * @version $Revision: 10140 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-10-12 14:51:32 -0400 (Sat, 12 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CachedExportFileReader {
    /** file location. */
    private String cachedFilePath;
    /** reader for the contents of the zipped cache file. */
    private BufferedReader reader;
    /** contents of the first (header) line of the file. */
    private List<String> headers;
    /** Provides the option to display data for web services
     * which may differ from the display in the UI. */
    private Boolean isWebServices;

    /**
     * Read a line and split on tabs.
     * @return the next line split on tabs
     * @throws IOException if something goes wrong with the read
     */
    public List<String> nextRow() throws IOException {
        String line = reader.readLine();
        if (isWebServices) {
            return line == null ? emptyList() : new ArrayList(asList(line.split("\t", -1)));
        }

        List<String> headerRow = new ArrayList<String>();
        List<String> cachedHeaders = line == null
            ? emptyList() : new ArrayList(asList(line.split("\t", -1)));
        for (String header : cachedHeaders) {
            if (header.trim().matches("CF[\\s]+\\(.*\\)")) {
                Integer openingParen = header.indexOf('(');
                Integer closingParen = header.lastIndexOf(')');
                String cfDomId = "cf_hash_" + header.substring(openingParen + 1, closingParen)
                    .hashCode();
                String cfName = header.substring(openingParen + 1, closingParen);
                headerRow.add("<span class=\"cf_header\" id=\"" + cfDomId
                    + "\">CF (" + cfName + ")");
            } else {
                headerRow.add(header);
            }
        }

        return headerRow;
    }

    /**
     * Close reader if already open, then open and read the header line.
     * @throws IOException if something goes wrong with the read
     */
    private void init() throws IOException {
        if (reader != null) {
            reader.close();
        }
        ZipFile zip = new ZipFile(cachedFilePath);
        ZipEntry entry = zip.entries().nextElement();
        reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry), "UTF-8"));
        headers = nextRow();
    }

    /**
     * Create a CachedExportFileReader.
     * @param cachedFilePath location of a zipped cache file
     */
    public CachedExportFileReader(String cachedFilePath) {
        this.cachedFilePath = cachedFilePath;
        this.isWebServices = false;
    }

    /**
     * Create a CachedExportFileReader for web services.
     * @param cachedFilePath location of a zipped cache file
     * @param isWebServices true if reading cached files for web services
     */
    public CachedExportFileReader(String cachedFilePath, Boolean isWebServices) {
        this.cachedFilePath = cachedFilePath;
        this.isWebServices = isWebServices;
    }

    /**
     * Contents of the header row.
     * @return contents of the header row
     */
    public List<String> headers() {
        if (headers == null) {
            try {
                init();
            } catch (IOException ioe) {
                ioe.printStackTrace(); //FIXME why this instead of log4j call?
                headers = emptyList();
            }
        }
        return headers;
    }

    /**
     * Skip the first offset rows (excluding the header row) then read at most limit rows.
     * @param limit maximum number of rows to read
     * @param offset index of the first row to read
     * @return each row is a List of the String values separated by tabs
     */
    public List<List<String>> rows(int limit, int offset) {
        List<List<String>> rows = new ArrayList<List<String>>();

        try {
            advance(offset);
            for (int i = 0; i < limit; i++) {
                List<String> row = nextRow();
                // row is returned as an empty list
                if (row.size() == 0) { break; }
                rows.add(row);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(); //FIXME why this instead of log4j call?
        }

        return rows;
    }

    /**
     * Open the file reader and advance to the first row we want to read.
     * @param offset index of the first row to read
     * @throws IOException if something goes wrong
     */
    private void advance(int offset) throws IOException {
        init();
        for (int i = 0; i < offset; i++) {
            if (reader.readLine() == null) {
                throw new IndexOutOfBoundsException("Offset " + offset
                        + " is past the end of the file.");
            }
        }
    }

    /**
     * Iterate over at most limit rows starting at offset.  Allows us to read rows one at a time
     * instead of allocating memory all at once.
     * @param limit maximum number of rows to read
     * @param offset index of the first row to read
     * @return an Iterable is suitable for the abbreviated for loop syntax
     * @throws IOException need to handle at a higher level if something goes wrong
     */
    public Iterable<List<String>> rowsIter(final int limit, final int offset) throws IOException {
        advance(offset);
        final Iterator<List<String>> iter = new Iterator<List<String>>() {
            /** the current row */
            private List<String> row = headers();
            /** which row we're on */
            private int rowNo = 0;

            /**
             * Whether we are past the last row.
             * @return whether we are past the last row
             */
            private boolean done() { return row.isEmpty() || rowNo == limit + 1; }

            /**
             * Get the next row if available, close the reader and input stream if we have
             * reached the end.
             */
            private void nextRow() {
                try {
                    row = CachedExportFileReader.this.nextRow();
                    if (rowNo == 0 && row == null) {
                        throw new IndexOutOfBoundsException("Offset " + offset
                                + " is past the end of the file.");
                    }
                    rowNo++;
                } catch (IOException ioe) {
                    row = null;
                    ioe.printStackTrace(); //FIXME why this instead of log4j call?
                } finally {
                    if (done() && reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace(); //FIXME why this instead of log4j call?
                        }
                    }
                }
            }

            /**
             * Whether we still have more rows.
             * @return whether we still have more rows
             */
            public boolean hasNext() { return !done(); }

            /** The next row. @return the next row */
            public List<String> next() {
                List<String> currentRow = row;
                nextRow();
                return currentRow;
            }

            /** Can't remove. */
            public void remove() { throw new UnsupportedOperationException(); }
        };

        return new Iterable<List<String>>() {
            public Iterator<List<String>> iterator() { return iter; }
        };
    }
}
