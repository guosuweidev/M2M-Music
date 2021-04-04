package com.m2mmusic.android.logic.model

/**
 * 登录Response
 */
data class LoginResponse(
    val loginType: Int,      // 0邮箱 1手机号
    val code: Int,           // 响应编号，200成功 501账号不存在 502密码错误
    val account: Account,   // 账户信息
    val token: String,      // token
    val profile: Profile,   // 用户信息
    val cookie: String,    // 可供浏览器保存的cookie
) {
    data class Account(
        val id: Long,            // UserId
        val userName: String,    // 用户名，暂时无用
        val createTime: Long,    // 创建时间，时间戳
        val vipType: Int         // 0非VIP，>0是VIP
    )

    data class Profile(
        val userId: Long,             // UserId
        val backgroundUrl: String,  // 背景图URL
        val vipType: Int,            // 0非VIP，>0是VIP
        val gender: Int,             // 性别，0未知，1男，2女
        val avatarUrl: String,       // 头像URL
        val birthday: Long,          // 生日，时间戳
        val nickname: String,        // 昵称
        val city: Int,               // 市级邮政编码
        val province: Int,           // 省级邮政编码
        val signature: String,       // 签名
        val followeds: Int,          // 被多少人关注
        val follows: Int,            // 关注的用户数
    )
}

/**
 * 退出登录Response
 */
data class LogoutResponse(
    val code: Int       // 返回200
)

/**
 * 刷新登录状态
 * 200已登录
 * 301需要登录
 */
data class RefreshLoginResponse(
    val code: Int
)

/**
 * 用户等级信息
 * 需要登录
 */
data class UserLevelResponse(
    val code: Int,                   // 响应编号，200成功,301需要登录
    val data: Data
) {
    data class Data(
        val level: Int,              // 等级
        val info: String,            // 等级权益信息，例如：60G音乐云盘免费容量$黑名单上限120$云音乐商城满100减12元优惠券$价值1200云贝
        val progress: Double,        // 升级进度
        val nextPlayCount: Int,      // 下一级需要的播放量
        val nextLoginCount: Int,     // 下一级需要的登录天数
        val nowPlayCount: Int,       // 已经播放了
        val nowLoginCount: Int       // 已经登录了
    )
}

/**
 * 获取用户歌单，收藏数量
 * 需要登录
 */
data class UserSubCountResponse(
    val code: Int,                   // 响应编号，200成功，301需要登录
    val djRadioCount: Int,           // 我的电台（订阅的电台数）
    val artistCount: Int,            // 收藏的歌手数
    val createdPlaylistCount: Int,   // 创建的歌单数
    val subPlaylistCount: Int        // 收藏的歌单数
)

/**
 * 获取用户播放记录统计
 * 需要登录
 * 200成功,-2无权限访问
 */
data class UserRecordResponse(
    val code: Int,                      // 响应编号，200成功,-2无权限访问
    val allData: List<PlayRecord>
) {
    data class PlayRecord(
        val playCount: Int,             // 播放次数
        val song: SongResponse.Song     // 歌曲详情
    )
}

/**
 * 用户喜欢音乐列表
 * 需要登录
 * ids数组实际只有一项ids[0]
 */
data class UserLikeListResponse(
    val code:Int,
    val ids:List<String>        // 用户喜欢音乐id列表，可直接拿去申请Music信息
)

/**
 * 喜欢音乐
 * 需要登录
 * 重复喜欢 / 取消喜欢 只生效一次
 * 每次搜索用户喜欢列表查看是否已喜欢
 */
data class LikeSongResponse(
    val code: Int               // 响应编号，200成功,301需要登录
)

/**
 * 获取用户歌单
 * 需要登录才能获取全部
 * 否则只能获取某个id的部分歌单
 * 获取到的Playlist中不包含tracks字段信息，需要根据歌单id去查找
 */
data class UserPlayListResponse(
    val code: Int,
    val playlist: List<PlaylistDetailResponse.Playlist>
)

/**
 * 收藏/取消收藏歌单
 * 401需要登录
 * 501已经收藏，提示重复操作
 * 200 收藏/取消收藏成功
 * 取消收藏重复操作不会501
 */
data class SubscribePlayListResponse(
    val code: Int
)