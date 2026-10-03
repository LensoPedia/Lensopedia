package lensopedia.myapp.models

/**
 * User data class for Firestore document mapping under Users/{userId}
 * Includes default parameter values for Firebase reflection / no-arg deserialization.
 */
data class User(
    val uid: String = "",
    val firstName: String = "",
    val surname: String = "",
    val email: String = "",
    val createdAt: Long = 0L
) {
    // Helper to get formatted full name
    fun getFullName(): String {
        return "$firstName $surname".trim()
    }
}
