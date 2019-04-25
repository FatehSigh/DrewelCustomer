package com.drewel.drewel.model

import android.os.Parcel
import android.os.Parcelable

class ChatModel() :Parcelable {

    public var channel_info:ChannelInfoModel?=null
    public var messages:MessageModel?=null

    constructor(parcel: Parcel) : this() {
        channel_info = parcel.readParcelable(ChannelInfoModel::class.java.classLoader)
        messages = parcel.readParcelable(MessageModel::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(channel_info, flags)
        parcel.writeParcelable(messages, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ChatModel(channel_info=$channel_info, messages=$messages)"
    }

    companion object CREATOR : Parcelable.Creator<ChatModel> {
        override fun createFromParcel(parcel: Parcel): ChatModel {
            return ChatModel(parcel)
        }

        override fun newArray(size: Int): Array<ChatModel?> {
            return arrayOfNulls(size)
        }
    }

}
