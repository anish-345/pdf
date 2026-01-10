package com.example.superfastbrowser

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BrowserPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments: MutableList<BrowserFragment> = mutableListOf()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: BrowserFragment) {
        fragments.add(fragment)
        notifyDataSetChanged()
    }

    fun getFragment(position: Int): BrowserFragment? {
        return if (position in 0 until fragments.size) {
            fragments[position]
        } else {
            null
        }
    }

    fun removeFragment(position: Int) {
        if (position in 0 until fragments.size) {
            fragments.removeAt(position)
            notifyDataSetChanged()
        }
    }
}
