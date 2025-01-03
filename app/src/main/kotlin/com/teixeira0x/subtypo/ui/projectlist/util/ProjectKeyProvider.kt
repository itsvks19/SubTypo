package com.teixeira0x.subtypo.ui.projectlist.util

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.ui.projectlist.adapter.ProjectListAdapter

class ProjectKeyProvider(private val recyclerView: RecyclerView) :
  ItemKeyProvider<Long>(SCOPE_CACHED) {

  override fun getKey(position: Int): Long {
    val adapter = recyclerView.adapter as ProjectListAdapter
    return adapter.currentList[position].id
  }

  override fun getPosition(key: Long): Int {
    val adapter = recyclerView.adapter as ProjectListAdapter
    return adapter.currentList.indexOfFirst { it.id == key }
  }
}
