package ro.ulbs.tiko

private val outOfScopeKeywords = setOf(
    "weather", "movie", "recipe", "politics", "sports", "game", "music", "celebrity",
    "stock market", "news", "history", "geography", "tell me a joke", "sing a song"
)

const val OUT_OF_SCOPE_RESPONSE = "As Tiko, the ULBS student assistant, I can only answer questions about academic life and university procedures. How can I help you with ULBS-related topics?"

/**
 * Checks if a question is out of scope based on a keyword list.
 */
fun isQuestionOutOfScope(question: String): Boolean {
    val lowercasedQuestion = question.lowercase()
    return outOfScopeKeywords.any { lowercasedQuestion.contains(it) }
}
