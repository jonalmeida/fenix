package org.mozilla.fenix.tabstray

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import org.mozilla.fenix.R

class TabsTrayFragment : AppCompatDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.TabTrayDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_tray_dialog, container, false).apply {
            TabsTrayView(this)
        }
    }
}