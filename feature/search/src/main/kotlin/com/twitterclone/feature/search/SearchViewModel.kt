package com.twitterclone.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.twitterclone.core.common.result.Async
import com.twitterclone.core.common.result.asAsync
import com.twitterclone.core.model.Tweet
import com.twitterclone.core.model.User
import com.twitterclone.core.model.repository.SearchRepository
import com.twitterclone.core.model.repository.TweetRepository
import com.twitterclone.core.model.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Result tabs shown while a query is active. */
enum class SearchTab { POSTS, PEOPLE }

/**
 * UDF state holder for the search screen.
 *
 * Raw text lives in [query]; [submittedQuery] debounces/trims/dedupes it and
 * drives both paged flows. A blank query keeps both flows empty so the screen
 * can show the trending section instead. Follow taps update
 * [followOverrides] optimistically and reconcile with the repository result —
 * the paged people list merges these overrides on top of `User.followedByMe`.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        searchRepository: SearchRepository,
        private val tweetRepository: TweetRepository,
        private val userRepository: UserRepository,
    ) : ViewModel() {
        private val _query = MutableStateFlow("")
        val query: StateFlow<String> = _query.asStateFlow()

        private val _selectedTab = MutableStateFlow(SearchTab.POSTS)
        val selectedTab: StateFlow<SearchTab> = _selectedTab.asStateFlow()

        private val _followOverrides = MutableStateFlow<Map<String, Boolean>>(emptyMap())
        val followOverrides: StateFlow<Map<String, Boolean>> = _followOverrides.asStateFlow()

        val trending: StateFlow<Async<List<Tweet>>> =
            searchRepository
                .trending()
                .asAsync()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = Async.Loading,
                )

        private val submittedQuery: Flow<String> =
            _query
                .debounce { text -> if (text.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
                .map { it.trim() }
                .distinctUntilChanged()

        val tweets: Flow<PagingData<Tweet>> =
            submittedQuery
                .flatMapLatest { q ->
                    if (q.isBlank()) {
                        flowOf(PagingData.empty())
                    } else {
                        searchRepository.searchTweets(q)
                    }
                }.cachedIn(viewModelScope)

        val users: Flow<PagingData<User>> =
            submittedQuery
                .flatMapLatest { q ->
                    if (q.isBlank()) {
                        flowOf(PagingData.empty())
                    } else {
                        searchRepository.searchUsers(q)
                    }
                }.cachedIn(viewModelScope)

        fun onQueryChange(newQuery: String) {
            _query.value = newQuery
        }

        fun onTabSelected(tab: SearchTab) {
            _selectedTab.value = tab
        }

        fun onLikeClick(tweetId: String) {
            viewModelScope.launch { tweetRepository.toggleLike(tweetId) }
        }

        fun onFollowClick(user: User) {
            val previous = _followOverrides.value[user.id] ?: user.followedByMe
            _followOverrides.update { it + (user.id to !previous) }
            viewModelScope.launch {
                userRepository
                    .toggleFollow(user.id)
                    .onSuccess { followed -> _followOverrides.update { it + (user.id to followed) } }
                    .onFailure { _followOverrides.update { it + (user.id to previous) } }
            }
        }
    }

private const val SEARCH_DEBOUNCE_MS = 300L
