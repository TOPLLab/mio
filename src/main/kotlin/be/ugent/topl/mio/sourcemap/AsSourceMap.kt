package be.ugent.topl.mio.sourcemap

import com.atlassian.sourcemap.ReadableSourceMapImpl
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

class AsSourceMapping(mapping: String): SourceMap {
    private val map = ReadableSourceMapImpl.fromSource(mapping)
    override fun getLineForPc(pc: Int): Int {
        return map.getMapping(0, pc).sourceLine + 1
    }

    override fun getPcForLine(line: Int, filename: String): Int {
        var pc = -1
        map.eachMapping {
            if (it.sourceFileName == filename &&
                it.sourceLine + 1 == line &&
                (pc < 0 || it.generatedColumn < pc)
            ) {
                pc = it.generatedColumn
            }
        }
        if (pc < 0) throw Exception("No pc matches this line number")
        return pc
    }

    override fun getSourceFile(pc: Int): String {
        return map.sourcesContent[map.sources.indexOf(getSourceFileName(pc))]
    }

    override fun getSourceFileName(pc: Int): String {
        val mapping = map.getMapping(0, pc) ?: return map.sources[0]
        return mapping.sourceFileName
    }

    override fun getStyle(): String {
        return SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT
    }
}
