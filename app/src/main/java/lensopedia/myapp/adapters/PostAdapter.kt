package lensopedia.myapp.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import lensopedia.myapp.R
import lensopedia.myapp.models.Post

class PostAdapter(
    private val context: Context,
    private val posts: List<Post>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthorName: TextView = itemView.findViewById(R.id.tvItemAuthor)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvItemTime)
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvItemDescription)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivItemImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.tvAuthorName.text = if (post.authorName.isNotBlank()) post.authorName else "Anonymous Photographer"
        holder.tvTitle.text = post.title
        holder.tvDescription.text = post.description

        // Format relative timestamp (e.g. "12 minutes ago")
        if (post.timestamp > 0) {
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                post.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            holder.tvTimestamp.text = relativeTime
        } else {
            holder.tvTimestamp.text = "Just now"
        }

        // Load image using Glide with disk caching
        Glide.with(context)
            .load(post.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(holder.ivPostImage)
    }

    override fun getItemCount(): Int = posts.size
}
