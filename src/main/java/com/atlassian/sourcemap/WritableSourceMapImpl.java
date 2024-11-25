package com.atlassian.sourcemap;

import java.util.Arrays;
import java.util.List;

import static com.atlassian.sourcemap.InternalUtil.join;
import static java.util.Collections.emptyList;

public class WritableSourceMapImpl implements WritableSourceMap {
    private final SourceMapGenerator sourceMapGenerator;
    private int lastGeneratedLine;
    private int lastGeneratedColumn;

    /**
     * We do not want to expose the constructor and instead encourage using the provided builder instead
     */
    private WritableSourceMapImpl(List<String> sources, List<String> sourcesContent) {
        sourceMapGenerator = new SourceMapGenerator();
        addSourcesAndContents(sources, sourcesContent);
    }

    @Override
    public void addSourcesAndContents(List<String> sources, List<String> sourcesContent) {
        sourceMapGenerator.addSourceAndContents(sources, sourcesContent);
    }

    @Override
    public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName) {
        addMapping(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, null);
    }

    @Override
    public void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName) {
        addMapping(new MappingImpl(generatedLine, generatedColumn, sourceLine, sourceColumn, sourceFileName, sourceSymbolName));
    }

    @Override
    public void addMapping(Mapping mapping) {
        // The current implementation of the generator requires that lines are added in proper order
        if (lastGeneratedLine > mapping.getGeneratedLine()) {
            throw new RuntimeException("Mappings need to be added line by line.\n" +
                    "The last added line was " + lastGeneratedLine + " the current mapping, however, is for the previous line: " +
                    mapping.getGeneratedLine()+ ".\n" +
                    "Please ensure mappings are provided in the proper order!");
        }

        if (lastGeneratedLine == mapping.getGeneratedLine() && lastGeneratedColumn > mapping.getGeneratedColumn()) {
            throw new RuntimeException("Mappings need to be added line by line and column by column.\n" +
                    "The last added column for this line was " + lastGeneratedColumn + " the current mapping, however, is for the previous column: " +
                    mapping.getGeneratedColumn()+ ".\n" +
                    "Please ensure mappings are provided in the proper order!");
        }

        lastGeneratedLine = mapping.getGeneratedLine();
        lastGeneratedColumn = mapping.getGeneratedColumn();

        sourceMapGenerator.addMapping(mapping);
    }

    @Override
    public String generate() {
        return sourceMapGenerator.generate();
    }

    @Override
    public String generateForHumans() {
        final SourceMapConsumer sourcemapConsumer = new SourceMapConsumer(generate());
        final StringBuilder buff = new StringBuilder();
        buff.append("{\n");
        buff.append("  sources  : [\n    ")
                .append(join(sourcemapConsumer.getSourceFileNames(), "\n    "))
                .append("\n  ]\n");
        buff.append("  mappings : [\n    ");
        final int[] previousLine = {-1};
        sourcemapConsumer.eachMapping(mapping -> {
            if ((mapping.getGeneratedLine() != previousLine[0]) && (previousLine[0] != -1)) {
                buff.append("\n    ");
            } else if (previousLine[0] != -1) {
                buff.append(", ");
            }

            previousLine[0] = mapping.getGeneratedLine();

            String shortName = mapping.getSourceFileName().replaceAll(".*/", "");
            buff.append("(")
                        .append(mapping.getGeneratedLine())
                        .append(":")
                        .append(mapping.getGeneratedColumn())
                        .append(" -> ")
                        .append(shortName)
                        .append(":")
                        .append(mapping.getSourceLine())
                        .append(":")
                        .append(mapping.getSourceColumn())
                    .append(")");
        });
        buff.append("\n  ]\n}");
        return buff.toString();
    }

    public static class Builder {
        private List<String> sources;
        private List<String> sourcesContent;

        public Builder withSourcesAndSourcesContent(List<String> sources, List<String> sourcesContent) {
            this.sources = sources;
            this.sourcesContent = sourcesContent;
            return this;
        }

        public Builder withSources(List<String> sources) {
            this.sources = sources;
            sourcesContent = Arrays.asList(new String[sources.size()]);
            return this;
        }

        public Builder empty() {
            sources = emptyList();
            sourcesContent = emptyList();
            return this;
        }

        public WritableSourceMap build() {
            if (sources == null) {
                throw new RuntimeException("No sources were specified");
            }

            if (sourcesContent == null) {
                throw new RuntimeException("No sourcesContent was specified");
            }

            if (sources.size() != sourcesContent.size()) {
                throw new RuntimeException("The number of sources does not match the number of sourcesContents provided.");
            }

            return new WritableSourceMapImpl(sources, sourcesContent);
        }
    }
}
