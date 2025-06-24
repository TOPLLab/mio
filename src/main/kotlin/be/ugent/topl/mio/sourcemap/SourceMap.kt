package be.ugent.topl.mio.sourcemap

import org.fife.ui.rsyntaxtextarea.SyntaxConstants

data class SourceMapLine(val line: Int, val col_start: Int, val col_end: Int)

interface SourceMap {
    fun getLineForPc(pc: Int): Int
    fun getPcForLine(line: Int, filename: String): Int
    fun getSourceFile(pc: Int): String
    fun getSourceFileName(pc: Int): String

    fun getSourceLineForPc(pc: Int): String = getSourceFile(pc).split('\n')[getLineForPc(pc)]
    fun getStyle() = SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86
}
