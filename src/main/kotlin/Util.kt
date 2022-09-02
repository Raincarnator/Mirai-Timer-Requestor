package net.reincarnatey

object Util {
    fun buildRegex(list: List<String>): Regex {
        val sb = StringBuilder()
        sb.append('(')
        list.forEachIndexed { index, s ->
            sb.append(s)
            if (index < list.size - 1) {
                sb.append('|')
            }
        }
        sb.append(')')
        return Regex(sb.toString())
    }

    fun buildTips(list: List<String>): String {
        val sb = StringBuilder()
        list.forEachIndexed { index, s ->
            sb.append(s)
            if (index < list.size - 1) {
                sb.append('/')
            }
        }
        return sb.toString()
    }
}