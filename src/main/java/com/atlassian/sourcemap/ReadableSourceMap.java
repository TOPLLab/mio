package com.atlassian.sourcemap;

import java.util.List;
import java.util.function.Consumer;

/**
 * Reads, writes and combines source maps.
 */
public interface ReadableSourceMap {
    /**
     * Get mapping for line and column in generated file.
     */
    Mapping getMapping(int lineNumber, int column);

    /**
     * Get list of source file names.
     */
    List<String> getSources();

    /**
     * Get list of named identifiers.
     */
    List<String> getNames();

    /**
     * Get list of source contents.
     */
    List<String> getSourcesContent();

    /**
     * Get Offset of sourcemap
     */
    int getOffset();

    /**
     * Adds to the existing offset
     */
    void addOffset(int offset);

    /**
     * Iterate over mappings.
     */
    void eachMapping(Consumer<Mapping> cb);

    /**
     * Turn the given readable sourcemap into a writeable source map
     * @param readableSourceMap the source map
     * @return readable source map
     */
    static WritableSourceMap toWritableSourceMap(final ReadableSourceMap readableSourceMap) {
        if (readableSourceMap == null) {
            return null;
        }
        WritableSourceMap writableSourceMap = new WritableSourceMapImpl.Builder()
                .withSourcesAndSourcesContent(readableSourceMap.getSources(), readableSourceMap.getSourcesContent())
                .build();
        readableSourceMap.eachMapping(writableSourceMap::addMapping);

        return writableSourceMap;
    }
}
