package com.atlassian.sourcemap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Helpers for converting source maps.
 */
public class Util {
    public static Set<String> JS_TYPES_AND_CONTENT_TYPES = new HashSet<>(asList(
        "js", "text/javascript", "application/javascript", "application/x-javascript"
    ));

    public static Set<String> CSS_TYPES_AND_CONTENT_TYPES = new HashSet<>(asList(
        "css", "text/css"
    ));

    /**
     * @param typeOrContentType
     * @return if source map is supported for this content type.
     */
    public static boolean isSourceMapSupportedBy(String typeOrContentType) {
        return JS_TYPES_AND_CONTENT_TYPES.contains(typeOrContentType) || CSS_TYPES_AND_CONTENT_TYPES.contains(typeOrContentType);
    }

    /**
     * Generate source map comment for JS or CSS.
     * @param typeOrContentType "css" or "js" string.
     */
    public static String generateSourceMapComment(String sourceMapUrl, String typeOrContentType) {
        if (JS_TYPES_AND_CONTENT_TYPES.contains(typeOrContentType)) return "//# sourceMappingURL=" + sourceMapUrl;
        else if (CSS_TYPES_AND_CONTENT_TYPES.contains(typeOrContentType)) return "/*# sourceMappingURL=" + sourceMapUrl + " */";
        else throw new RuntimeException("source map not supported for " + typeOrContentType);
    }


    /**
     * Generates 1 to 1 mapping, it's needed in order to create source map for batch. When source maps of individual
     * resources joined into the batch source map - if some of resources doesn't have source map then the 1 to 1 source
     * map would be generated for it.
     * @param source source content.
     * @param sourceUrl source url.
     * @return 1 to 1 source map.
     */
    public static WritableSourceMap create1to1SourceMap(CharSequence source, String sourceUrl) {
        return create1to1SourceMap(countLines(source), sourceUrl);
    }

    /**
     * Generates 1 to 1 mapping, it's needed in order to create source map for batch. When source maps of individual
     * resources joined into the batch source map - if some of resources doesn't have source map then the 1 to 1 source
     * map would be generated for it.
     * @param linesCount count of lines in source file.
     * @param sourceUrl source url.
     * @return 1 to 1 source map.
     */
    public static WritableSourceMap create1to1SourceMap(int linesCount, String sourceUrl) {
        WritableSourceMap map = new WritableSourceMapImpl.Builder()
                .withSources(singletonList(sourceUrl))
                .build();
        for (int i = 0; i < linesCount; i++ ) map.addMapping(i, 0, i, 0, sourceUrl);
        return map;
    }

    /**
     * Helper to count newlines in content.
     */
    public static int countLines(InputStream stream) {
        try {
            int c = stream.read();
            int counter = 0;
            while (c != -1) {
                if (c == '\n') counter += 1;
                c = stream.read();
            }
            return counter + 1;
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Helper to count newlines in content.
     */
    public static int countLines(CharSequence stream) {
        int counter = 0;
        for (int i = 0; i < stream.length(); i++) {
            if (counter == 0) counter += 1;
            if (stream.charAt(i) == '\n') counter += 1;
        }
        return counter;
    }

    /**
     * If multiple transformations applied to the source each of it could generate its own source map. Rebase allows to
     * unite all this maps and generate the final map. It's done by rebasing each map on the map of the previous
     * transformation.
     *
     * @param sourceMap current source map.
     * @param previousSourceMap map from previous transformation.
     */
    public static WritableSourceMap rebase(ReadableSourceMap sourceMap, final ReadableSourceMap previousSourceMap) {
        final WritableSourceMap rebasedMap = new WritableSourceMapImpl.Builder()
                .withSourcesAndSourcesContent(sourceMap.getSources(), sourceMap.getSourcesContent())
                .build();
        sourceMap.eachMapping(mapping -> {
            Mapping rebasedMapping = previousSourceMap.getMapping(mapping.getSourceLine(), mapping.getSourceColumn());
            if (rebasedMapping != null)  {
                rebasedMap.addMapping(
                    mapping.getGeneratedLine(),
                    mapping.getGeneratedColumn(),
                    rebasedMapping.getSourceLine(),
                    rebasedMapping.getSourceColumn(),
                    rebasedMapping.getSourceFileName(),
                    rebasedMapping.getSourceSymbolName()
                );
            }
        });
        return rebasedMap;
    }

    /**
     * Join multiple source map.
     * @return helper to join mutliple source map.
     */
    public static SourceMapJoiner joiner() { return new SourceMapJoiner(); }
}
