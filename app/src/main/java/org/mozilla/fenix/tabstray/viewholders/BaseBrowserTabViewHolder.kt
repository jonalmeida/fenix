/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray.viewholders

import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import org.mozilla.fenix.R
import org.mozilla.fenix.tabstray.TabsTrayInteractor
import org.mozilla.fenix.tabstray.TabsTrayStore
import org.mozilla.fenix.tabstray.browser.BaseBrowserTrayList
import org.mozilla.fenix.tabstray.browser.PrivateBrowserTrayList

/**
 * A shared view holder for browser tabs tray list.
 */
abstract class BaseBrowserTabViewHolder(
    containerView: View,
    interactor: TabsTrayInteractor,
    tabsTrayStore: TabsTrayStore
) : AbstractTrayViewHolder(containerView) {

    private val trayList: BaseBrowserTrayList = itemView.findViewById(R.id.tray_list_item)

    init {
        trayList.interactor = interactor
        trayList.tabsTrayStore = tabsTrayStore
    }

    @CallSuper
    override fun bind(
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
        layoutManager: RecyclerView.LayoutManager
    ) {
        // Figure out how to distinguish between the two view types based on the selected tab.
        // We have a reference to the `TabsTrayStore` here, we just need to know if
        // "this@BaseBrowserTabViewHolder" is the correct type based on the
        // selected page in the store.
        if (trayList is PrivateBrowserTrayList) {
            adapter.registerAdapterDataObserver(OneTimeAdapterObserver(adapter) {
                trayList.scrollToPosition(8)

                // TODO get this reference passed here. Maybe pass a ref to BrowserStore
                //  in the constructor?
                // trayList.scrollToPosition(getCurrentTabListIndex(browserStore))
            })
        }
        trayList.layoutManager = layoutManager
        trayList.adapter = adapter
    }

    private fun getCurrentTabListIndex(store: BrowserStore): Int {
        val selectedTab = store.state.selectedTab
        val selectedTabIsPrivate = selectedTab?.content?.private == true
        return if (selectedTabIsPrivate) {
            store.state.privateTabs.indexOf(selectedTab)
        } else {
            store.state.normalTabs.indexOf(selectedTab)
        }
    }
}

/**
 * Observes the adapter and invokes the callback when data is first inserted.
 */
class OneTimeAdapterObserver(
    private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    private val onAdapterReady: () -> Unit
) : RecyclerView.AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        onAdapterReady.invoke()
        adapter.unregisterAdapterDataObserver(this)
    }
}
