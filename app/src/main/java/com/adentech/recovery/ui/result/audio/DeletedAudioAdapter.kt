package com.adentech.recovery.ui.result.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.R
import com.adentech.recovery.RecoveryApplication
import com.adentech.recovery.core.recyclerview.RecoveryListAdapter
import com.adentech.recovery.data.model.FileModel
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.BlurTransformation


class DeletedAudioAdapter(
    val context: Context,
    private val onItemClicked: ((item: FileModel) -> Unit)? = null
) : RecoveryListAdapter<FileModel>(
    itemsSame = { old, new -> old == new },
    contentsSame = { old, new -> old == new }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return DeletedAudioViewHolder(parent, inflater)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DeletedAudioViewHolder) {
            val item = getItem(position)
            holder.bind(item, onItemClicked)

            if (!item.fileName.isNullOrBlank()) {
                val imageView: AppCompatImageView =
                    holder.itemView.findViewById(R.id.soundIconImageView)
//
//                if (item.isDeleted == true) {
//                    Glide.with(context)
//                        .load(item.imageUri)
//                        .into(imageView)
//                } else {
//                    if (RecoveryApplication.hasSubscription) {
//                        Glide.with(context)
//                            .load(item.imageUri)
//                            .into(imageView)
//                    } else {
//                        Glide.with(context)
//                            .load(item.imageUri)
//                            .transform(BlurTransformation(75))
//                            .into(imageView)
//                    }
//                }

            } else {
                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    Handler(Looper.getMainLooper()).post {
                        val updatedList =
                            currentList.filterIndexed { index, _ -> index != currentPosition }
                        submitList(updatedList)
                        notifyItemRemoved(currentPosition)
                        notifyItemRangeChanged(currentPosition, itemCount - currentPosition)
                    }
                }
            }
        }
    }
}
