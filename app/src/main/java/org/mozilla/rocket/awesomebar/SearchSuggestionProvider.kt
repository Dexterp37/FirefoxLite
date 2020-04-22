/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.rocket.awesomebar

import android.os.AsyncTask
import android.util.Log
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.fetch.MutableHeaders
import mozilla.components.concept.fetch.Request
import mozilla.components.concept.fetch.interceptor.withInterceptors
import mozilla.components.lib.fetch.httpurlconnection.HttpURLConnectionClient
import org.json.JSONArray
import org.mozilla.focus.BuildConfig
import org.mozilla.focus.search.SearchEngine
import org.mozilla.focus.utils.SupportUtils
import org.mozilla.rocket.msrp.data.LoggingInterceptor
import java.io.IOException
import java.util.UUID

class SearchSuggestionProvider(
    private val searchEngine: SearchEngine,
    private val userAgent: String,
    private val searchUseCase: (text: String) -> Unit
) : AwesomeBar.SuggestionProvider {

    private var queryTask: AsyncTask<*, *, *>? = null

    companion object {
        private const val TAG = "awesome_s_p"
        private const val MAX_SUGGESTION_COUNT = 5
        private const val ID_OF_ENTERED_TEXT = "<@@@entered_text_id@@@>"
    }

    override val id: String = UUID.randomUUID().toString()

    override fun onInputCancelled() {
        queryTask?.cancel(true)
    }

    override suspend fun onInputChanged(text: String): List<AwesomeBar.Suggestion> {

        if (SupportUtils.isUrl(text)) {
            return listOf()
        }
        try {
            val url = searchEngine.buildSearchSuggestionUrl(text) ?: return emptyList()
            val request = Request(
                    url = url,
                    headers = MutableHeaders(
                            "User-Agent" to userAgent)
            )
            HttpURLConnectionClient().withInterceptors(LoggingInterceptor()).fetch(request).use { http ->
                return when (http.status) {
                    200 -> {
                        val responseStr = http.body.string()
                        val response = JSONArray(responseStr)
                        val suggestions = response.getJSONArray(1)
                        val size = suggestions.length()
//                        val suggests = mutableListOf<AwesomeBar.Suggestion>()

                        val coerceAtMost = size.coerceAtMost(MAX_SUGGESTION_COUNT)

                        val chips = mutableListOf<AwesomeBar.Suggestion.Chip>()
                        for (i in 0..coerceAtMost) {
                            chips.add(AwesomeBar.Suggestion.Chip(suggestions[i] as String))
                        }
                        listOf(AwesomeBar.Suggestion(
                                provider = this,
                                id = text,
                                title = searchEngine.name,
                                icon = searchEngine.icon,
                                chips = chips,
                                score = Int.MAX_VALUE,
                                onChipClicked = { chip ->
                                    searchUseCase.invoke(chip.title)
                                }
                        ))
//                        for (i in 0..coerceAtMost) {
//                            val item = suggestions[i] as String
//                            suggests.add(
//                                    AwesomeBar.Suggestion(
//                                            provider = this,
//                                            // We always use the same ID for the entered text so that this suggestion gets replaced "in place".
//                                            id = if (item == text) ID_OF_ENTERED_TEXT else item,
//                                            title = item,
//                                            description = searchEngine.name,
//                                            score = Int.MAX_VALUE - i,
//                                            onSuggestionClicked = {
//                                                searchUseCase.invoke(item)
//                                            }
//                                    )
//                            )
//                        }
//                        Log.d(TAG, "onInputChanged=======suggests:${suggests.size}")
//
//                        suggests
                    }
                    else -> {
                        if (BuildConfig.DEBUG) {
                            throw RuntimeException("Should not reach this")
                        } else {
                            return listOf()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "onInputChanged=======$e")

            return listOf()
        } catch (e: Exception) {
            Log.e(TAG, "onInputChanged=======$e")
            return listOf()
        }
    }
}
