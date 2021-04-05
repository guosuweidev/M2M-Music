package com.m2mmusic.android.logic.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SearchMusicResponse(
    val code: Int,
    val result: SongsResult
) {
    data class SongsResult(
        val songs: List<SongResponse.Song>,
        val songCount: Int
    )
}

data class SearchAlbumResponse(
    val code: Int,
    val result: AlbumsResult
) {
    data class AlbumsResult(
        val albums: List<AlbumResult>,
        val albumCount: Int
    ) {
        @Parcelize
        data class AlbumResult(
            val id: Long,
            val name: String,
            val picUrl: String,
            val artist: NewResourcesResponse.Artist,
            val publishTime: Long
        ) : Parcelable
    }
}

data class SearchArtistResponse(
    val code: Int,
    val result: ArtistsResult
) {
    data class ArtistsResult(
        val artists: List<ArtistResult>,
        val artistCount: Int
    ) {
        data class ArtistResult(
            val id: Long,
            val name: String,
            val picUrl: String
        )
    }
}

data class SearchMusicListResponse(
    val code: Int,
    val result: PlayListsResult
) {
    data class PlayListsResult(
        val playlists: List<PlayListResult>
    ) {
        data class PlayListResult(
            val id: Long,
            val name: String,
            @SerializedName("coverImgUrl")
            val picUrl: String,
            val creator: PlaylistDetailResponse.Playlist.Creator,
            val trackCount: Int,
            val playCount: Long
        )
    }
}

data class SearchSuggestResponse(
    val code: Int,
    val result: AllMatch
) {
    data class AllMatch(
        val allMatch: List<Suggest>
    ) {
        data class Suggest(
            val keyword: String
        )
    }
}