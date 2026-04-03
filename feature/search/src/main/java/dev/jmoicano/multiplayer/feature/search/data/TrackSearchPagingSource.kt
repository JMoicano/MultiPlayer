package dev.jmoicano.multiplayer.feature.search.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.jmoicano.multiplayer.core.network.datasource.NetworkException
import dev.jmoicano.multiplayer.core.network.model.Track
import dev.jmoicano.multiplayer.core.network.repository.SearchRepository

/** PagingSource responsible for loading paged track search results. */
class TrackSearchPagingSource(
    private val searchRepository: SearchRepository,
    private val query: String
) : PagingSource<Int, Track>() {

    companion object {
        private const val TAG = "MPSearchPaging"
        private const val ITUNES_MAX_LIMIT = 200
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        return try {
            val currentOffset = params.key ?: 0
            val requestedTotal = currentOffset + params.loadSize
            val fetchLimit = requestedTotal.coerceAtMost(ITUNES_MAX_LIMIT)
            Log.d(
                TAG,
                "load offset=$currentOffset loadSize=${params.loadSize} fetchLimit=$fetchLimit query='$query'",
            )

            val result = searchRepository.searchTracks(
                query = query,
                limit = fetchLimit,
                offset = 0,
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                val allTracks = response?.results ?: emptyList()
                val tracks = allTracks.drop(currentOffset)
                val reachedApiLimit = fetchLimit >= ITUNES_MAX_LIMIT
                val reachedAvailableResults = allTracks.size < fetchLimit
                val endOfPaginationReached = tracks.isEmpty() || reachedAvailableResults || reachedApiLimit
                val nextOffset = currentOffset + tracks.size
                Log.d(
                    TAG,
                    "load success offset=$currentOffset count=${tracks.size} nextKey=${if (endOfPaginationReached) "null" else nextOffset}",
                )

                LoadResult.Page(
                    data = tracks,
                    prevKey = if (currentOffset == 0) null else maxOf(currentOffset - params.loadSize, 0),
                    nextKey = if (endOfPaginationReached) null else nextOffset,
                )
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Unknown error")
                Log.e(TAG, "load failed offset=$currentOffset: ${exception.message}", exception)
                LoadResult.Error(exception)
            }
        } catch (exception: NetworkException) {
            Log.e(TAG, "load network exception: ${exception.message}", exception)
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            Log.e(TAG, "load unexpected exception: ${exception.message}", exception)
            LoadResult.Error(exception)
        }
    }

    /** Computes the key used to restart loading from the current anchor. */
    override fun getRefreshKey(state: PagingState<Int, Track>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

