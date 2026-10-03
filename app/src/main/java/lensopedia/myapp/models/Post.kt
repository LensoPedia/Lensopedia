package lensopedia.myapp.models

/**
 * Post model representing community photography entries in Firestore Posts collection.
 */
data class Post(
    val postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)
