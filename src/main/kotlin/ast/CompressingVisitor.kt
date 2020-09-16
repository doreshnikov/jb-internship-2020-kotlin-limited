package ast

import lexer.Token

class CompressingVisitor : Visitor<Tree>() {

    override fun visitExpression(expression: Tree.Expression): Tree {
        val newTerms = ArrayList<Tree.TermWithSign>()
        // 1. flatten all the children
        expression.children.forEach { child ->
            when (val newNode = visit(child)) {
                is Tree.Expression -> newTerms.addAll(newNode.children)
                is Tree.TermWithSign -> newTerms.add(newNode)
                else -> throw IllegalStateException("Unexpected $newNode when transforming expression's child $child")
            }
        }

        // 2. flatten all constants and variables
        var constant = 0
        val variables = HashMap<Tree.Term.Variable, Int>()
        for (signedTerm in newTerms) {
            when (signedTerm.term) {
                is Tree.Term.Integer -> if (signedTerm.positive)
                    constant += signedTerm.term.value
                else constant -=
                    signedTerm.term.value
                is Tree.Term.Variable -> if (signedTerm.positive) variables.compute(signedTerm.term) { _, count ->
                    (count ?: 0) + 1
                }
                is Tree.Term.Wrapper -> throw IllegalStateException("No wrappers should be left after flattening")
            }
        }

        // 3. dropping all zeroes
        val terms: List<Tree.TermWithSign> = variables
            .filter { (_, count) -> count != 0 }
            .map { (variable, count) -> List(count) { Tree.TermWithSign(count > 0, variable) } }
            .flatten().toMutableList()
            .also {
                if (constant != 0) it.add(
                    Tree.TermWithSign(
                        constant > 0,
                        Tree.Term.Integer(kotlin.math.abs(constant))
                    )
                )
            }

        return Tree.Expression(terms)
    }

    override fun visitTermWithSign(termWithSign: Tree.TermWithSign): Tree {
        var newTerm = visit(termWithSign.term)
        if (newTerm is Tree.Term) {
            newTerm = Tree.TermWithSign(true, newTerm)
        }
        return when {
            termWithSign.positive -> newTerm
            newTerm is Tree.TermWithSign -> when (newTerm.term) {
                is Tree.Term.Wrapper -> throw IllegalStateException("Wrapper should have been flattened")
                else -> Tree.TermWithSign(!newTerm.positive, newTerm.term)
            }
            newTerm is Tree.Expression -> Tree.Expression(newTerm.children.map {
                Tree.TermWithSign(
                    !it.positive,
                    it.term
                )
            })
            newTerm is Tree.TermWithSign -> Tree.TermWithSign(!newTerm.positive, newTerm.term)
            else -> throw IllegalStateException("Unexpected $newTerm after transforming term with sign $termWithSign")
        }
    }

    override fun visitInteger(integer: Tree.Term.Integer): Tree {
        return integer
    }

    override fun visitVariable(variable: Tree.Term.Variable): Tree {
        return variable
    }

    override fun visitWrapper(wrapper: Tree.Term.Wrapper): Tree {
        return visit(wrapper.expression)
    }
}