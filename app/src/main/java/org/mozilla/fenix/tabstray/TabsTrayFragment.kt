/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.tabstray

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.component_tabstray2.*
import kotlinx.android.synthetic.main.component_tabstray2.view.*
import org.mozilla.fenix.HomeActivity
import kotlinx.android.synthetic.main.component_tabstray2.tab_layout
import kotlinx.android.synthetic.main.component_tabstray2.tabsTray
import kotlinx.android.synthetic.main.component_tabstray2.view.tab_wrapper
import kotlinx.android.synthetic.main.component_tabstray_fab.view.new_tab_button
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.fenix.R
import org.mozilla.fenix.components.StoreProvider
import org.mozilla.fenix.ext.requireComponents
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.tabstray.browser.BrowserTrayInteractor
import org.mozilla.fenix.tabstray.browser.DefaultBrowserTrayInteractor
import org.mozilla.fenix.tabstray.browser.RemoveTabUseCaseWrapper
import org.mozilla.fenix.tabstray.browser.SelectTabUseCaseWrapper
import org.mozilla.fenix.tabstray.syncedtabs.SyncedTabsInteractor

class TabsTrayFragment : AppCompatDialogFragment(), TabsTrayInteractor {

    private var fabView: View? = null
    private var hasAccessibilityEnabled: Boolean = false
    private lateinit var tabsTrayStore: TabsTrayStore
    private lateinit var browserTrayInteractor: BrowserTrayInteractor
    private lateinit var tabsTrayController: DefaultTabsTrayController
    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    private val tabLayoutMediator = ViewBoundFeatureWrapper<TabLayoutMediator>()

    private val selectTabUseCase by lazy {
        SelectTabUseCaseWrapper(
            requireComponents.analytics.metrics,
            requireComponents.useCases.tabsUseCases.selectTab
        ) {
            navigateToBrowser()
        }
    }

    private val removeUseCases by lazy {
        RemoveTabUseCaseWrapper(
            requireComponents.analytics.metrics
        ) {
            tabRemoved(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TabTrayDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        TabsTrayDialog(requireContext(), theme) { browserTrayInteractor }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val containerView = inflater.inflate(R.layout.fragment_tab_tray_dialog, container, false)
        val view: View = LayoutInflater.from(containerView.context)
            .inflate(R.layout.component_tabstray2, containerView as ViewGroup, true)

        behavior = BottomSheetBehavior.from(view.tab_wrapper)

        tabsTrayStore = StoreProvider.get(this) { TabsTrayStore() }

        fabView = LayoutInflater.from(containerView.context)
            .inflate(R.layout.component_tabstray_fab, containerView, true)

        return containerView
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as HomeActivity
        hasAccessibilityEnabled = activity.settings().accessibilityServicesEnabled

        tabsTrayController = DefaultTabsTrayController(
            browsingModeManager = activity.browsingModeManager,
            navController = findNavController()
        )

        browserTrayInteractor = DefaultBrowserTrayInteractor(
            tabsTrayStore,
            tabsTrayController,
            selectTabUseCase,
            removeUseCases,
            requireComponents.settings,
            this
        )

        val syncedTabsTrayInteractor = SyncedTabsInteractor(
            requireComponents.analytics.metrics,
            requireActivity() as HomeActivity,
            this
        )

        setupPager(
            view.context,
            tabsTrayStore,
            this,
            browserTrayInteractor,
            syncedTabsTrayInteractor
        )

        tabLayoutMediator.set(
            feature = TabLayoutMediator(
                tabLayout = tab_layout,
                interactor = this,
                store = requireComponents.core.store
            ), owner = this,
            view = view
        )

        consumeFrom(requireComponents.core.store) {
            view.findViewById<TabCounter>(R.id.tab_counter)?.apply {
                setCount(requireComponents.core.store.state.normalTabs.size)
            }
        }
        setupNewTabButtons(tabsTray.currentItem)
    }

    override fun setCurrentTrayPosition(position: Int, smoothScroll: Boolean) {
        tabsTray.setCurrentItem(position, smoothScroll)
        setupNewTabButtons(position)
    }

    override fun navigateToBrowser() {
        dismissAllowingStateLoss()

        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.browserFragment) {
            return
        }

        if (!navController.popBackStack(R.id.browserFragment, false)) {
            navController.navigate(R.id.browserFragment)
        }
    }

    override fun tabRemoved(tabId: String) {
        // TODO re-implement these methods
        // showUndoSnackbarForTab(sessionId)
        // removeIfNotLastTab(sessionId)

        // Temporary
        requireComponents.useCases.tabsUseCases.removeTab(tabId)
    }

    private fun setupPager(
        context: Context,
        store: TabsTrayStore,
        trayInteractor: TabsTrayInteractor,
        browserInteractor: BrowserTrayInteractor,
        syncedTabsTrayInteractor: SyncedTabsInteractor
    ) {
        tabsTray.apply {
            adapter = TrayPagerAdapter(
                context,
                store,
                browserInteractor,
                syncedTabsTrayInteractor,
                trayInteractor
            )
            isUserInputEnabled = false
        }
    }

    private fun setupNewTabButtons(currentPage: Int) {
        fabView?.let { fabView ->
            when (currentPage) {
                NORMAL -> {
                    fabView.new_tab_button.shrink()
                    fabView.new_tab_button.show()
                    fabView.new_tab_button.setOnClickListener {
                        browserTrayInteractor.onFabClicked(false)
                    }
                }
                PRIVATE -> {
                    fabView.new_tab_button.text =
                        requireContext().resources.getText(R.string.tab_drawer_fab_content)
                    fabView.new_tab_button.extend()
                    fabView.new_tab_button.show()
                    fabView.new_tab_button.setOnClickListener {
                        browserTrayInteractor.onFabClicked(true)
                    }
                }
                SYNC -> {
                    fabView.new_tab_button.text =
                        requireContext().resources.getText(R.string.preferences_sync_now)
                    fabView.new_tab_button.extend()
                    fabView.new_tab_button.show()
                    fabView.new_tab_button.setOnClickListener {
                    }
                }
            }
        }
    }

    companion object {
        // TabsTray Pages
        const val NORMAL = 0
        const val PRIVATE = 1
        const val SYNC = 2
    }
}
