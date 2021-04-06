/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.fenix.R
import org.mozilla.fenix.tabstray.browser.BrowserTrayInteractor

class FloatingActionButtonBinding(
    private val store: TabsTrayStore,
    private val actionButton: ExtendedFloatingActionButton,
    private val browserTrayInteractor: BrowserTrayInteractor
) : LifecycleAwareFeature {

    private var scope: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun start() {
        scope = store.flowScoped { flow ->
            flow.map { it.selectedPage }
                .ifChanged()
                .collect { page ->
                    setFab(page)
                }
        }
    }

    override fun stop() {
        scope?.cancel()
    }

    private fun setFab(selectedPage: Page) {
        when (selectedPage) {
            Page.NormalTabs -> {
                actionButton.apply {
                    shrink()
                    show()
                    setOnClickListener {
                        browserTrayInteractor.onFabClicked(false)
                    }
                }
            }
            Page.PrivateTabs -> {
                actionButton.apply {
                    text = context.getText(R.string.tab_drawer_fab_content)
                    extend()
                    show()
                    setOnClickListener {
                        browserTrayInteractor.onFabClicked(true)
                    }
                }
            }
            Page.SyncedTabs -> {
                actionButton.apply {
                    text = context.getText(R.string.preferences_sync_now)
                    extend()
                    show()
                    setOnClickListener {
                        // Notify the store observers (one of which is the SyncedTabsFeature), that
                        // a sync was requested.
                        store.dispatch(TabsTrayAction.SyncNow)
                    }
                }
            }
        }
    }
}
