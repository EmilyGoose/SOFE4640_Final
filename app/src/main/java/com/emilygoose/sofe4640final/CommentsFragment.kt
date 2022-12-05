package com.emilygoose.sofe4640final

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.adapter.CommentsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_ID = "ID"

class CommentsFragment : Fragment() {
    private var id: String? = null

    private val commentList = ArrayList<Pair<String, String>>()

    private lateinit var commentBox: EditText
    private lateinit var commentButton: Button
    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_ID)
        }

        db = Firebase.firestore
        auth = Firebase.auth

        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                userName = documentSnapshot.get("name") as String
            }


        db.collection("comments").document("comments").get()
            .addOnSuccessListener { documentSnapshot ->
                // Create document if no comments for ID yet
                if (!documentSnapshot.contains(id!!)) {
                    db.collection("comments").document("comments")
                        .update(id!!, ArrayList<Pair<*, *>>())
                } else {
                    val comments = documentSnapshot.get(id!!) as List<*>
                    for (comment in comments) {
                        comment as HashMap<*, *>

                        commentList.add(
                            Pair(
                                comment["first"] as String,
                                comment["second"] as String
                            )
                        )
                        commentsAdapter.notifyItemInserted(commentList.size - 1)
                        commentRecyclerView.scrollToPosition(commentList.size - 1)
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_comments, container, false)
        // Init view variables
        commentBox = view.findViewById(R.id.input_comment)
        commentButton = view.findViewById(R.id.button_comment)
        commentButton.setOnClickListener {
            val commentText = commentBox.text.toString()
            commentBox.text.clear()


            // Post document to database
            val document = db.collection("comments").document("comments")
            document.update(id!!, FieldValue.arrayUnion(Pair(userName, commentText)))

        }

        // Configure recycler view
        commentRecyclerView = view.findViewById(R.id.recycler_comments)
        commentsAdapter = CommentsAdapter(commentList)
        commentRecyclerView.adapter = commentsAdapter
        commentRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)

        return view

    }
}