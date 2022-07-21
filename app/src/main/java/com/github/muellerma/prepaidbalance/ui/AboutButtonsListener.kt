package com.github.muellerma.prepaidbalance.ui

import android.view.View
import com.github.muellerma.prepaidbalance.utils.openInBrowser
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.SpecialButton

class AboutButtonsListener : LibsConfiguration.LibsListener {
    override fun onExtraClicked(v: View, specialButton: SpecialButton): Boolean {
        val link = when (specialButton) {
            SpecialButton.SPECIAL1 -> "https://github.com/mueller-ma/PrepaidBalance/"
            SpecialButton.SPECIAL2 -> "https://f-droid.org/packages/com.github.muellerma.prepaidbalance/"
            SpecialButton.SPECIAL3 -> "https://crowdin.com/project/prepaidbalance"
        }
        link.openInBrowser(v.context)
        return true
    }

    override fun onIconClicked(v: View) {
        // no-op
    }

    override fun onIconLongClicked(v: View): Boolean {
        return false
    }

    override fun onLibraryAuthorClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryAuthorLongClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryBottomClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryBottomLongClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryContentClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryContentLongClicked(v: View, library: Library): Boolean {
        return false
    }
}