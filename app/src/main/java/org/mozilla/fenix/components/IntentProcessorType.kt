/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components

import android.content.Intent
import mozilla.components.feature.intent.processing.IntentProcessor
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.customtabs.ExternalAppBrowserActivity
import org.mozilla.fenix.migration.MigrationProgressActivity

enum class IntentProcessorType {
    /**
     * An external app has tried to open a site link.
     */
    EXTERNAL_APP,

    /**
     * Launch a new tab from an internally routed intent.
     */
    NEW_TAB,

    /**
     * A migration is taking place so we should short-circuit other intents.
     */
    MIGRATION,

    /**
     * Launch a web app.
     */
    WEBAPP,

    /**
     * Launch a page shortcut.
     */
    SHORTCUT,

    /**
     * All other unknown intent types.
     */
    OTHER;

    /**
     * The destination activity based on this intent
     */
    val activityClassName: String
        get() = when (this) {
            EXTERNAL_APP, SHORTCUT, WEBAPP -> ExternalAppBrowserActivity::class.java.name
            NEW_TAB, OTHER -> HomeActivity::class.java.name
            MIGRATION -> MigrationProgressActivity::class.java.name
        }

    /**
     * Should this intent automatically navigate to the browser?
     */
    fun shouldOpenToBrowser(intent: Intent): Boolean = when (this) {
        EXTERNAL_APP, WEBAPP, SHORTCUT -> true
        NEW_TAB -> intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0
        MIGRATION, OTHER -> false
    }
}

/**
 * Classifies the [IntentProcessorType] based on the [IntentProcessor] that handled the [Intent].
 */
fun IntentProcessors.getType(processor: IntentProcessor?) = when {
    migrationIntentProcessor == processor -> {
        IntentProcessorType.MIGRATION
    }
    externalAppIntentProcessors.contains(processor) -> {
        IntentProcessorType.WEBAPP
    }
    customTabIntentProcessor == processor || privateCustomTabIntentProcessor == processor -> {
        IntentProcessorType.EXTERNAL_APP
    }
    intentProcessor == processor || privateIntentProcessor == processor -> {
        IntentProcessorType.NEW_TAB
    }
    fennecPageShortcutIntentProcessor == processor -> {
        IntentProcessorType.SHORTCUT
    }
    else -> {
        IntentProcessorType.OTHER
    }
}
