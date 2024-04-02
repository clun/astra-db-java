package com.datastax.astra.tool.csv;

import com.datastax.astra.client.model.Document;

/**
 * Settings for the CSV Loader
 */
public interface CsvRowMapper {

    /**
     * Process the document
     *
     * @param doc
     *      document to process
     */
    Document map(Document doc) ;

}
