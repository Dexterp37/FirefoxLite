package org.mozilla.rocket.awesomebar

import mozilla.components.concept.awesomebar.AwesomeBar
import org.mozilla.focus.repository.BookmarkRepository
import java.util.UUID

class BookmarkSuggestionProvider(
    private val bookmarkRepo: BookmarkRepository,
    private val onSuggestionClicked: (() -> Unit)?
) : AwesomeBar.SuggestionProvider {
    companion object {
        private const val BOOKMARKS_SUGGESTION_LIMIT = 5
    }

    override val id: String = UUID.randomUUID().toString()

    override suspend fun onInputChanged(text: String): List<AwesomeBar.Suggestion> {
        if (text.isEmpty()) {
            return emptyList()
        }

        return bookmarkRepo.searchBookmarks(text, BOOKMARKS_SUGGESTION_LIMIT)
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
                            onSuggestionClicked = onSuggestionClicked
                    )
                }
    }
}