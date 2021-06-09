package org.mozilla.fenix.home.sessioncontrol.viewholders.recenttabs

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recent_tabs_list_row.view.*
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.R

class RecentTabViewHolder (
    private val view: View
) : RecyclerView.ViewHolder(view) {
    fun bindTab(tab: TabSessionState) {
        view.recent_tab_title.text = tab.content.title
        view.recent_tab_icon.setImageBitmap(tab.content.icon)
        view.setOnClickListener {

            Logger.info("TEST recent tab clicked: $tab")
            // TODO in interactor
            // components.useCases.selectTab(tab)
            // navGraph().navigate(browserId)
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.recent_tabs_list_row
    }
}