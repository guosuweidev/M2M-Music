package com.m2mmusic.android.logic.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * 获取新歌、新碟、数字专辑
 * 不需要登录
 */
data class NewResourcesResponse(
    val code: Int,                                  // 响应code
    val data: Data                                  // 响应数据
) {
    data class Data(
        val blocks: List<Block>                     // 数据块
    )

    data class Block(
        val blockCode: String,
        val creatives: List<Creative>               // 块中生成的数据
    )

    @Parcelize
    data class Creative(
        val resources: List<Resource>               // 资源集
    ) : Parcelable

    @Parcelize
    data class Resource(
        val uiElement: UiElement,                   // 资源概要
        val resourceId: String,                       // 资源id（歌曲、专辑id）
        @SerializedName("resourceExtInfo")
        val resourceExtInfo: Info                              // 资源附加信息
    ):Parcelable{
        /*val resType: String = "未知"
        val mainTitle: String = "未知"
        val subTitle: String = "未知"
        val imageUrl: String? = null
        val artists: ArrayList<Long>
            get() {
                info.artists.forEach { artist ->
                    artists.add(artist.id)
                }
                return artists
            }*/
    }

    @Parcelize
    data class UiElement(
        val mainTitle: MainTitle,                   // 名称
        val subTitle: SubTitle,                     // 简介
        val image: Image                            // 图片
    ):Parcelable

    @Parcelize
    data class Info(
        val artists: List<Artist>                   // 创作者集
    ):Parcelable

    @Parcelize
    data class MainTitle(
        val title: String                           // 名称数据
    ):Parcelable

    @Parcelize
    data class SubTitle(
        val title: String                           // 简介数据
    ):Parcelable

    @Parcelize
    data class Image(
        val imageUrl: String                        // 图片URL
    ):Parcelable

    @Parcelize
    data class Artist(
        val name: String,                           // 创作者姓名
        val id: Long                                // 创作者id
    ):Parcelable
}