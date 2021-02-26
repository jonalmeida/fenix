package org.mozilla.fenix.tabstray

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.component_tabstray.*
import kotlinx.android.synthetic.main.component_tabstray.view.*
import org.mozilla.fenix.R

class TabsTrayView(override val containerView: View) : LayoutContainer {
    val view: View = LayoutInflater.from(containerView.context)
        .inflate(R.layout.component_tabstray, containerView as ViewGroup, true)
    init {
        BottomSheetBehavior.from(view.tab_wrapper)
        //tabsTray.adapter = TrayPagerAdapter()
    }
}
