package lexer

sealed class Token {

    companion object Factory {
        val all = listOf(
            SymbolToken.Open,
            SymbolToken.Close,
            SymbolToken.Plus,
            SymbolToken.Minus,

            IntegerToken,
            VariableToken
        )

        fun start(char: Char): TokenValue {
            for (token in all) {
                if (token.acceptsFirst(char)) {
                    return TokenValue(token).apply { eat(char) }
                }
            }
            throw IllegalArgumentException("No token starts with char '$char'")
        }
    }

    internal abstract fun acceptsFirst(char: Char): Boolean
    internal abstract fun acceptsNext(char: Char): Boolean

    open class SymbolToken(private val symbol: Char) : Token() {
        override fun acceptsFirst(char: Char): Boolean {
            return char == symbol
        }

        override fun acceptsNext(char: Char): Boolean {
            return false
        }

        object Open : SymbolToken('(')
        object Close : SymbolToken(')')
        object Plus : SymbolToken('+')
        object Minus : SymbolToken('-')
    }

    object IntegerToken : Token() {
        override fun acceptsFirst(char: Char): Boolean {
            return char in '0'..'9';
        }

        override fun acceptsNext(char: Char): Boolean {
            return char in '0'..'9';
        }
    }

    object VariableToken : Token() {
        override fun acceptsFirst(char: Char): Boolean {
            return char in 'a'..'z' || char in 'A'..'Z'
        }

        override fun acceptsNext(char: Char): Boolean {
            return acceptsFirst(char) || char in '0'..'9'
        }
    }

}

class TokenValue(val token: Token) {

    private val valueBuilder = StringBuilder()
    val value get() = valueBuilder.toString()

    fun eat(char: Char): Boolean {
        return (if (value.isBlank()) token.acceptsFirst(char) else token.acceptsNext(char))
            .also {
                if (it) valueBuilder.append(char)
            }
    }

}