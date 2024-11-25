package com.atlassian.sourcemap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Generates Source Map version 3.
 * <p>
 * Code based on Google Closure Compiler https://code.google.com/p/closure-compiler
 */
class SourceMapGenerator {

    // A pre-order traversal ordered list of mappings stored in this map.
    private final List<Mapping> mappings = new ArrayList<>();

    private final OrderedSourcesValues orderedSources = new OrderedSourcesValues();
    private final OrderedSourcesValues orderedSourcesContent = new OrderedSourcesValues();
    private final OrderedSourcesValues orderedNames = new OrderedSourcesValues();

    // needs to be reset every time a new source map is added
    private final LinkedHashMap<String, String> sourcesRemapper = new LinkedHashMap<>();

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * This Generator is build with "joinability" in mind - meaning it allows to merge multiple sourcemaps
     * Be aware though that this comes with the expactation that source maps are provided one after the other.
     * It is not possible to add source maps intermingled!
     * E.g. if you have source map "a" and source map "b" you must first provide the sources/sourcesContent and mappings
     * of "a" before can provide the sources/sourcesContent of "b" and its mappings.
     * The Reasoning being that "a" and "b" are independent, and assumptions like "unique" sources-names do not hold
     * across multiple source maps.
     * The generator will hold a list of sources and will ensure that when merging source-maps sources remain unique and
     * that changes to sources will correctly be mapped to the provided mappings. In order for this to work it has to
     * remain internal state of which sourcemap is currently providing mapping. Further the changes to mappings are
     * one-way, and therefore non-reversable. This is the primary reason that sourcemaps must always be provided in full.
     *
     * @param sources        the sources or "filenames" of source files referenced in this sourcemap
     * @param sourcesContent the contents of the filenames/sources referenced in this sourcemap
     */
    public void addSourceAndContents(List<String> sources, List<String> sourcesContent) {
        if (sources.size() != sourcesContent.size()) {
            throw new RuntimeException("The number of sources does not match the number of sourcesContents provided.");
        }

        // reset the remapper
        sourcesRemapper.clear();

        // There are 4 cases we need to be aware of:
        // - The source is unknown  and the sourceContent is unknown    -> see #1 below
        // - The source is unknown  and the sourceContent is known      -> see #2 below
        // - The source is known    and the sourceContent is unknown    -> see #3 below
        // - The source is known    and the sourceContent is known      -> see #4 below
        //
        // #1:  Add both the source and sourceContent to our hash maps and increment the counter.
        // #2:  Overwrite the existing source entry which will now point at the previously added content.
        //      The counter remains untouched.
        // #3:  Add the sources name to the remapper - create a new unique name for it and ensure any mappings
        //       referencing it are redirected to the new unique name, increment the counter
        // #4:  Do nothing

        for (int i = 0; i < sources.size(); i++) {
            final String source = sources.get(i);
            final String sourceContent = sourcesContent.get(i);

            // Case #2 and #4
            // Check if we already know the sourceContent, if so overwrite the associated source filename.
            // In theory we wouldn't need to do this in case #4 but as sources are the same it's an effective noop
            final boolean hasKnownSource = sourceContent != null && orderedSourcesContent.hasValue(sourceContent);
            if (hasKnownSource) {
                final int sourceIndex = orderedSourcesContent.getIndex(sourceContent);
                // overwrite filename for the final source map
                orderedSources.replaceAt(sourceIndex, source);
                continue;
            }

            // Case #3
            // We already know the source but the sourceContent diverge or is null
            final boolean knownSourceNameButContentDoesNotMatch = orderedSources.hasValue(source) &&
                    (sourceContent == null ||
                            !orderedSourcesContent.getValueAtIndex(orderedSources.getIndex(source)).equals(sourceContent));
            if (knownSourceNameButContentDoesNotMatch) {
                remapSource(source, sourceContent);
                continue;
            }

            // Case #1 - The "default" case - we add both source and sourceContents
            addSourceAndSourceContent(source, sourceContent);
        }
    }

    private void remapSource(final String source, final String sourceContent) {
        String remappedSource;
        int counter = 1;

        do {
            remappedSource = source + "-uniquified-" + counter;
            counter += 1;
        } while (orderedSources.hasValue(remappedSource));

        sourcesRemapper.put(source, remappedSource);
        addSourceAndSourceContent(remappedSource, sourceContent);
    }

    private void addSourceAndSourceContent(final String source, final String sourceContent) {
        orderedSources.add(source);
        orderedSourcesContent.add(sourceContent);
    }

    /**
     * Adds a mapping for the given node.  Mappings must be added in order.
     */
    public void addMapping(Mapping mapping) {
        final String sourceFileName = mapping.getSourceFileName();

        // Some generators like Closure Compiler could produce mappings with undefined file names and
        // source locations, there's no point to use such mapping ignoring it.
        if (sourceFileName == null) {
            return;
        }

        // check if we had to rename the source file and remap it
        if (sourcesRemapper.containsKey(sourceFileName)) {
            mapping.setSourceFileName(sourcesRemapper.get(sourceFileName));
        }

        if (!orderedSources.hasValue(mapping.getSourceFileName())) {
            throw new RuntimeException("No source with name '" + mapping.getSourceFileName() + "' exists.");
        }

        String sourceSymbolName = mapping.getSourceSymbolName();
        if (sourceSymbolName != null && !orderedNames.hasValue(sourceSymbolName)) {
            orderedNames.add(mapping.getSourceSymbolName());
        }

        mappings.add(mapping);
    }

    public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName) {
        addMapping(new MappingImpl(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, sourceSymbolName));
    }

    public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName) {
        addMapping(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, null);
    }

    /**
     * Writes out the source map in the following format (line numbers are for
     * reference only and are not part of the format):
     * <p>
     * 1.  {
     * 2.    version: 3,
     * 3.    sources: ["foo.js", "bar.js"],
     * 4.    sourcesContent: ["<some javascript or null>", "<some more javascript or null>"],
     * 5.    names: ["src", "maps", "are", "fun"],
     * 6.    mappings: "a;;abcde,abcd,a;"
     * 7. }
     * <p>
     * Line 1: The entire file is a single JSON object
     * Line 2: File revision (always the first entry in the object)
     * Line 3: A list of sources used by the "mappings" entry relative to the sourceRoot.
     * Line 4: A list of sourcesContent - the source code of the sources specified in the field above.
     * Line 5: A list of symbol names used by the "mapping" entry. This list may be incomplete.
     * Line 6: The mappings field.
     */
    public String generate() {
        try {
            final StringBuilder mappingsBuilder = new StringBuilder();
            new LineMapper(mappingsBuilder).appendLineMappings();
            SourceMapJson sourceMapJson = new SourceMapJson.Builder()
                    .withVersion(3)
                    .withSources(orderedSources.getValues())
                    .withSourcesContent(orderedSourcesContent.getValues())
                    .withNames(orderedNames.getValues())
                    .withMappings(mappingsBuilder.toString())
                    .build();
            return gson.toJson(sourceMapJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getSourceFileNameIndex(String sourceName) {
        Integer index = orderedSources.getIndex(sourceName);
        if (index == null) throw new RuntimeException("source file name " + sourceName + " is unknown!");
        return index;
    }

    private int getSourceSymbolNameIndex(String symbolName) {
        Integer index = orderedNames.getIndex(symbolName);
        if (index == null) throw new RuntimeException("source symbol name " + symbolName + " is unknown!");
        return index;
    }

    private class LineMapper {
        // The destination.
        private final Appendable out;

        private int previousLine = -1;
        private int previousColumn = 0;

        // Previous values used for storing relative ids.
        private int previousSourceFileNameId;
        private int previousSourceLine;
        private int previousSourceColumn;
        private int previousSourceSymbolNameId;

        LineMapper(Appendable out) {
            this.out = out;
        }

        // Append the line mapping entries.
        void appendLineMappings() throws IOException {
            for (Mapping mapping : mappings) {
                int generatedLine = mapping.getGeneratedLine();
                int generatedColumn = mapping.getGeneratedColumn();

                if (generatedLine > 0 && previousLine != generatedLine) {
                    int start = previousLine == -1 ? 0 : previousLine;
                    for (int i = start; i < generatedLine; i++) {
                        finishLine();
                    }
                }

                if (previousLine != generatedLine) {
                    previousColumn = 0;
                } else {
                    finishMapping();
                }

                writeEntry(mapping, generatedColumn);
                previousLine = generatedLine;
                previousColumn = generatedColumn;
            }
        }

        /**
         * End the entry for this mapping.
         */
        private void finishMapping() throws IOException {
            out.append(',');
        }

        /**
         * End the entry for a line.
         */
        private void finishLine() throws IOException {
            out.append(';');
        }

        /**
         * Writes an entry for the given column (of the generated text) and
         * associated mapping.
         * The values are stored as relative to the last seen values for each
         * field and encoded as Base64VLQs.
         */
        void writeEntry(Mapping m, int column) throws IOException {
            // The relative generated column number
            Base64VLQ.encode(out, column - previousColumn);
            previousColumn = column;
            if (m != null) {
                // The relative source file id
                int sourceId = getSourceFileNameIndex(m.getSourceFileName());
                Base64VLQ.encode(out, sourceId - previousSourceFileNameId);
                previousSourceFileNameId = sourceId;

                // The relative source file line and column
                int srcline = m.getSourceLine();
                int srcColumn = m.getSourceColumn();
                Base64VLQ.encode(out, srcline - previousSourceLine);
                previousSourceLine = srcline;

                Base64VLQ.encode(out, srcColumn - previousSourceColumn);
                previousSourceColumn = srcColumn;

                if (m.getSourceSymbolName() != null) {
                    // The relative id for the associated symbol name
                    int nameId = getSourceSymbolNameIndex(m.getSourceSymbolName());
                    Base64VLQ.encode(out, (nameId - previousSourceSymbolNameId));
                    previousSourceSymbolNameId = nameId;
                }
            }
        }
    }

}
