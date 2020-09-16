package lexer

object Lexer {

    fun process(cursor: Cursor<Char>): Cursor<TokenValue> {
        val tokens = ArrayList<TokenValue>()
        var lastToken: TokenValue? = null

        while (!cursor.eoln()) {
            if (lastToken == null) {
                lastToken = Token.start(cursor(move = true))
            }
            while (!cursor.eoln() && lastToken.eat(cursor())) {
                cursor.step()
            }
            tokens.add(lastToken)
            lastToken = null
        }

        return Cursor(tokens, 0)
    }

}