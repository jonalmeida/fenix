package org.mozilla.fenix.home.sessioncontrol

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.helpers.AbstractBinding
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.fenix.home.HomeFragmentAction
import org.mozilla.fenix.home.HomeFragmentStore

@OptIn(ExperimentalCoroutinesApi::class)
class RecentTabsListBinding(
    browserStore: BrowserStore,
    private val homeStore: HomeFragmentStore,
): AbstractBinding<BrowserState>(browserStore) {
    override suspend fun onState(flow: Flow<BrowserState>) {
        flow.map { it.selectedTab }
            .ifChanged()
            .collect { selectedTab ->
                val recentTabsList = if (selectedTab != null) {
                    listOf(selectedTab)
                } else {
                    emptyList()
                }

                homeStore.dispatch(HomeFragmentAction.RecentTabsChange(recentTabsList))
            }
    }
}
