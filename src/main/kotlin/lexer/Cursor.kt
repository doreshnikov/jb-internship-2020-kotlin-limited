package lexer

class Cursor<T>(private val line: List<T>, private var location: Int) {

    companion object {
        operator fun invoke(line: String, location: Int) =
            Cursor(line.toList(), location)
    }

    init {
        String
        if (location < 0 || location > line.size) {
            throw IllegalArgumentException("Location must be between 0 and ${line.size}, got $location instead")
        }
    }

    fun eoln(): Boolean {
        return location == line.size
    }

    operator fun invoke(move: Boolean = false): T {
        if (!eoln()) {
            return line[location].also {
                if (move) step()
            }
        }
        throw IllegalStateException("Cursor called when <eoln> is reached")
    }

    fun step() {
        if (!eoln()) {
            location++
        }
    }

}