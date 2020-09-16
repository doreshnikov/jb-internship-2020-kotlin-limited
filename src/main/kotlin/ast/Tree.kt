package ast

import lexer.Cursor
import lexer.Token
import lexer.TokenValue

sealed class Tree {

    class Expression(val children: List<TermWithSign>): Tree() {
        companion object {
            fun parse(cursor: Cursor<TokenValue>): Expression {
                val terms = ArrayList<TermWithSign>()
                terms.add(TermWithSign(positive = true, Term.parse(cursor)))

                while (!cursor.eoln()) {
                    val sign = when (val token = cursor().token) {
                        is Token.SymbolToken.Plus -> true
                        is Token.SymbolToken.Minus -> false
                        is Token.SymbolToken.Close -> break
                        else -> throw IllegalStateException("Expected sign, got $token instead")
                    }
                    cursor.step()
                    terms.add(TermWithSign(sign, Term.parse(cursor)))
                }

                return Expression(terms)
            }
        }

        override fun toString(): String {
            return buildString {
                append('(')
                children.forEachIndexed { index, signedTerm ->
                    if (index != 0) {
                        append(if (signedTerm.positive) " + " else " - ")
                    }
                    append(signedTerm.term.toString())
                }
                append(')')
            }
        }
    }

    sealed class Term: Tree() {
        class Integer(val value: Int): Term() {
            override fun toString(): String {
                return value.toString()
            }
        }
        class Variable private constructor(val name: String): Term() {
            companion object {
                private val factory = HashMap<String, Variable>()

                operator fun invoke(name: String): Variable =
                    factory.getOrPut(name) { Variable(name) }
            }

            override fun toString(): String {
                return name
            }
        }
        class Wrapper(val expression: Expression): Term() {
            override fun toString(): String {
                return expression.toString()
            }
        }

        companion object {
            fun parse(cursor: Cursor<TokenValue>): Term {
                return when (val token = cursor().token) {
                    is Token.IntegerToken -> Integer(cursor(move = true).value.toInt())
                    is Token.VariableToken -> Variable(cursor(move = true).value)
                    is Token.SymbolToken.Open -> {
                        cursor.step()
                        Wrapper(Expression.parse(cursor)).also {
                            require(cursor(move = true).token is Token.SymbolToken.Close) { "No closing parenthesis" }
                        }
                    }
                    else -> throw IllegalStateException("Expected variable token, got $token instead")
                }
            }
        }
    }

    class TermWithSign(val positive: Boolean, val term: Term): Tree() {
        override fun toString(): String {
            return buildString {
                if (positive) {
                    append(" + ")
                } else {
                    append(" - ")
                }
                append(term.toString())
            }
        }
    }

}