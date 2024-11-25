package com.atlassian.sourcemap;

import java.util.List;

import static com.atlassian.sourcemap.ReadableSourceMapImpl.fromSource;

/**
 * Reads, writes and combines source maps.
 */
public interface WritableSourceMap {
    /**
     * Add sourceRoot.
     */
    void addSourcesAndContents(List<String> sources, List<String> sourcesContent);

    /**
     * Add mapping.
     */
    void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName);

    /**
     * Add mapping.
     */
    void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName);

    /**
     * Add mapping.
     */
    void addMapping(Mapping mapping);

    /**
     * Generate source map JSON.
     */
    String generate();

    /**
     * Generate source map in format easily read by humans, for debug purposes.
     */
    String generateForHumans();

    /**
     * Turn this writable sourcemap into a readable source map
     * @param writableSourceMap the source map
     * @return readable source map
     */
    static ReadableSourceMap toReadableSourceMap(WritableSourceMap writableSourceMap) {
        if (writableSourceMap == null) {
            return null;
        }
        return fromSource(writableSourceMap.generate());
    };
}
