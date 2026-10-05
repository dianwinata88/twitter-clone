package com.twitterclone.feature.composetweet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitterclone.core.model.repository.TweetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_MEDIA_ATTACHMENTS = 4

data class ComposerUiState(
    val text: String = "",
    val mediaUris: List<String> = emptyList(),
    val remainingChars: Int = TweetLengthCounter.MAX_LENGTH,
    val posting: Boolean = false,
    val error: String? = null,
    val posted: Boolean = false,
) {
    val isOverLimit: Boolean get() = remainingChars < 0

    val canPost: Boolean
        get() = !posting && !isOverLimit && (text.isNotBlank() || mediaUris.isNotEmpty())
}

@HiltViewModel
class ComposerViewModel
    @Inject
    constructor(
        private val tweetRepository: TweetRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        // Draft is mirrored into SavedStateHandle so it survives process death.
        private val text = savedStateHandle.getStateFlow(KEY_TEXT, "")
        private val mediaUris = savedStateHandle.getStateFlow(KEY_MEDIA, emptyList<String>())
        private val posting = MutableStateFlow(false)
        private val error = MutableStateFlow<String?>(null)
        private val posted = MutableStateFlow(false)

        val uiState: StateFlow<ComposerUiState> =
            combine(text, mediaUris, posting, error, posted) { t, m, p, e, done ->
                ComposerUiState(
                    text = t,
                    mediaUris = m,
                    remainingChars = TweetLengthCounter.remaining(t),
                    posting = p,
                    error = e,
                    posted = done,
                )
            }.stateIn(viewModelScope, SharingStarted.Eagerly, ComposerUiState())

        fun onTextChange(newText: String) {
            savedStateHandle[KEY_TEXT] = newText
        }

        fun onMediaPicked(uris: List<String>) {
            savedStateHandle[KEY_MEDIA] =
                ArrayList((mediaUris.value + uris).distinct().take(MAX_MEDIA_ATTACHMENTS))
        }

        fun onMediaRemoved(uri: String) {
            savedStateHandle[KEY_MEDIA] = ArrayList(mediaUris.value - uri)
        }

        /** Clear the one-shot error after the UI has shown it. */
        fun onErrorShown() {
            error.value = null
        }

        fun post() {
            val draftText = text.value.trim()
            val attachments = mediaUris.value
            val overLimit = TweetLengthCounter.remaining(draftText) < 0
            if (posting.value || overLimit || (draftText.isEmpty() && attachments.isEmpty())) return

            viewModelScope.launch {
                posting.value = true
                error.value = null
                tweetRepository
                    .postTweet(draftText, attachments)
                    .onSuccess { posted.value = true }
                    .onFailure {
                        error.value = it.message ?: "Couldn't post your tweet. Try again."
                    }
                posting.value = false
            }
        }

        private companion object {
            const val KEY_TEXT = "composer_text"
            const val KEY_MEDIA = "composer_media"
        }
    }
