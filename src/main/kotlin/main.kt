import ast.CompressingVisitor
import ast.Tree
import lexer.Cursor
import lexer.Lexer

fun main(args: Array<String>) {

    val expression = "(1+2-4)-(1-3)+x+y-xx-x-(y+z)+0"
    val result = CompressingVisitor().visit(
        Tree.Expression.parse(
            Lexer.process(Cursor(expression))
        )
    )
    require(result.toString() == "(x + y + 1)") { "The answer should be valid" }

}