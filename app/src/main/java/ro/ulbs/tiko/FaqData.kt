package ro.ulbs.tiko

val faqData = mapOf(
    "academic calendar" to "You can find the academic calendar on the official ULBS website. Just search for 'structura anului universitar'.",
    "scholarship" to "Information about scholarships, including eligibility criteria and deadlines, is available on the ULBS website under the 'Students' section.",
    "faculties" to "A list of all faculties and departments can be found on the ULBS website. Each faculty has its own dedicated page with more information.",
    "contact" to "You can find contact information for each faculty on the ULBS website. General inquiries can be directed to the university's main office."
)

fun findFaqAnswer(question: String): String? {
    for ((keyword, answer) in faqData) {
        if (question.contains(keyword, ignoreCase = true)) {
            return answer
        }
    }
    return null
}
