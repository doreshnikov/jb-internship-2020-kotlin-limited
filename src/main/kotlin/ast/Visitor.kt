package ast

abstract class Visitor<T> {

    fun visit(astNode: Tree): T {
        return when (astNode) {
            is Tree.Expression -> visitExpression(astNode)
            is Tree.TermWithSign -> visitTermWithSign(astNode)
            is Tree.Term.Integer -> visitInteger(astNode)
            is Tree.Term.Variable -> visitVariable(astNode)
            is Tree.Term.Wrapper -> visitWrapper(astNode)
        }
    }

    abstract fun visitExpression(expression: Tree.Expression): T
    abstract fun visitTermWithSign(termWithSign: Tree.TermWithSign): T
    abstract fun visitInteger(integer: Tree.Term.Integer): T
    abstract fun visitVariable(variable: Tree.Term.Variable): T
    abstract fun visitWrapper(wrapper: Tree.Term.Wrapper): T

}