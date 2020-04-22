package org.mozilla.rocket.awesomebar

import android.util.Log
import mozilla.components.concept.awesomebar.AwesomeBar
import org.mozilla.focus.repository.BookmarkRepository
import java.util.UUID

class BookmarkSuggestionProvider(
    private val bookmarkRepo: BookmarkRepository,
    private val onSuggestionClicked: ((text: String) -> Unit)
) : AwesomeBar.SuggestionProvider {
    companion object {
        private const val TAG = "AAAAA"
        private const val BOOKMARKS_SUGGESTION_LIMIT = 5
    }

    override val id: String = UUID.randomUUID().toString()

    override suspend fun onInputChanged(text: String): List<AwesomeBar.Suggestion> {
        Log.d(TAG, "onInputChanged=======$text")
        if (text.isEmpty()) {
            return emptyList()
        }

        val map = bookmarkRepo.searchBookmarks(text, BOOKMARKS_SUGGESTION_LIMIT)
                .filter { it.url != null }
                .distinctBy { it.url }
                .sortedBy { it.url }
                .map {
                    AwesomeBar.Suggestion(
                            provider = this,
                            id = it.id,
                            icon = null,
                            title = it.title,
                            description = it.url,
                            onSuggestionClicked = { onSuggestionClicked.invoke(it.url) }
                    )
                }
        Log.d(TAG, "map=======${map.size}")
        return map
    }
}