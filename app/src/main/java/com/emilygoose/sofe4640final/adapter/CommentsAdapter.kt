package com.emilygoose.sofe4640final.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.R

class CommentsAdapter(private val dataSet: ArrayList<Pair<String, String>>) :RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentAuthor: TextView = view.findViewById(R.id.comment_name)
        val commentBody: TextView = view.findViewById(R.id.comment_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = dataSet[position]

        holder.commentAuthor.text = comment.first
        holder.commentBody.text = comment.second
    }

    override fun getItemCount() = dataSet.size
}