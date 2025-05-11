package com.example.textn.ui.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.textn.R
import com.example.textn.data.model.Comment
import com.example.textn.data.model.Post
import com.example.textn.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var ivPostImage: ImageView
    private lateinit var tvPostDescription: TextView
    private lateinit var tvPostLocation: TextView
    private lateinit var tvPostDate: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvLikesCount: TextView
    private lateinit var btnLike: Button
    private lateinit var etComment: EditText
    private lateinit var btnSendComment: ImageView
    private lateinit var rvComments: RecyclerView

    private val args: PostDetailFragmentArgs by navArgs()
    private lateinit var post: Post

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[CommunityViewModel::class.java]

        // Ánh xạ views
        ivPostImage = view.findViewById(R.id.iv_post_image)
        tvPostDescription = view.findViewById(R.id.tv_post_description)
        tvPostLocation = view.findViewById(R.id.tv_post_location)
        tvPostDate = view.findViewById(R.id.tv_post_date)
        tvUsername = view.findViewById(R.id.tv_username)
        tvLikesCount = view.findViewById(R.id.tv_likes_count)
        btnLike = view.findViewById(R.id.btn_like)
        etComment = view.findViewById(R.id.et_comment)
        btnSendComment = view.findViewById(R.id.btn_send_comment)
        rvComments = view.findViewById(R.id.rv_comments)

        // Thiết lập RecyclerView cho comments
        rvComments.layoutManager = LinearLayoutManager(requireContext())
        val commentAdapter = CommentAdapter()
        rvComments.adapter = commentAdapter

        // Quan sát danh sách posts
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            val currentPost = posts.find { it.id == args.postId }
            if (currentPost != null) {
                post = currentPost
                displayPostDetails()
                commentAdapter.submitList(post.comments.reversed())
            }
        }

        // Quan sát thông báo lỗi
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        // Nút like
        btnLike.setOnClickListener {
            viewModel.likePost(args.postId)
        }

        // Nút gửi comment
        btnSendComment.setOnClickListener {
            val commentText = etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                viewModel.addComment(args.postId, commentText)
                etComment.text.clear()
            }
        }

        // Tải thông tin của post
        viewModel.loadPosts()
    }

    private fun displayPostDetails() {
        // Hiển thị hình ảnh
        Glide.with(requireContext())
            .load(post.imageUrl)
            .into(ivPostImage)
        Log.d("PostDetailFragment", "Image URL: ${post.imageUrl}")

        // Hiển thị thông tin chi tiết
        tvPostDescription.text = post.description
        tvPostLocation.text = post.location.locationName
        tvUsername.text = post.displayName
        tvLikesCount.text = "${post.likes} lượt thích"

        // Định dạng và hiển thị ngày đăng
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(post.timestamp)
        tvPostDate.text = dateFormat.format(date)
    }

    // Adapter cho danh sách comments
    private class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

        private var comments: List<Comment> = emptyList()

        fun submitList(newComments: List<Comment>) {
            comments = newComments
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            val comment = comments[position]
            holder.bind(comment)
        }

        override fun getItemCount(): Int = comments.size

        class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvCommentUsername: TextView = itemView.findViewById(R.id.tv_comment_username)
            private val tvCommentText: TextView = itemView.findViewById(R.id.tv_comment_text)
            private val tvCommentDate: TextView = itemView.findViewById(R.id.tv_comment_date)

            fun bind(comment: Comment) {
                tvCommentUsername.text = comment.displayName
                tvCommentText.text = comment.text

                // Định dạng và hiển thị ngày bình luận
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = Date(comment.timestamp)
                tvCommentDate.text = dateFormat.format(date)
            }
        }
    }
}
