package com.respekt.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayListResponse(

	@field:SerializedName("data")
	val data: MutableList<DataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
) : Parcelable

@Parcelize
data class DataItem(

	@field:SerializedName("category_name")
	val categoryName: String? = null,

	@field:SerializedName("cover_photo")
	val coverPhoto: String? = null,

	@field:SerializedName("music_duration")
	val musicDuration: String? = null,

	@field:SerializedName("sub_category")
	val subCategory: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("music_title")
	val musicTitle: String? = null,

	@field:SerializedName("music_file")
	val musicFile: String? = null,

	@field:SerializedName("music_artist")
	val musicArtist: String? = null
) : Parcelable
