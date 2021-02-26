/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray

import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.lib.state.Action
import mozilla.components.lib.state.State
import mozilla.components.lib.state.Store

/**
 * Value type that represents the state of the tabs tray.
 */
data class TabsTrayState(
    val shareTabs: List<TabSessionState> = emptyList()
) : State

/**
 * [Action] implementation related to [TabsTrayStore].
 */
sealed class TabsTrayAction : Action {
    data class ShareTabsAction(val tabs: List<TabSessionState>) : TabsTrayAction()
}

/**
 * Reducer for [TabsTrayStore].
 */
internal object TabsTrayReducer {
    fun reduce(state: TabsTrayState, action: TabsTrayAction) : TabsTrayState {
        return when (action) {
            is TabsTrayAction.ShareTabsAction -> {
                state.copy(shareTabs = action.tabs)
            }
        }
    }
}

/**
 * A [Store] that holds the [TabsTrayState] for the tabs tray and reduces [TabsTrayAction]s dispatched to
 * the store.
 */
class TabsTrayStore(
    initialState: TabsTrayState = TabsTrayState()
) : Store<TabsTrayState, TabsTrayAction>(
    initialState,
    TabsTrayReducer::reduce
)