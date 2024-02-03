package com.adentech.recovery.ui.result.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adentech.recovery.core.recyclerview.RecoveryListAdapter
import com.adentech.recovery.data.model.FileModel

class GalleryAdapter(
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
        return GalleryViewHolder(parent, inflater)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GalleryViewHolder) {
            val item = getItem(position)
            holder.bind(item, onItemClicked)
        }
    }
}