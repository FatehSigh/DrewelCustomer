package com.drewel.drewel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.drewel.drewel.R
import com.drewel.drewel.adapter.ChatMessageAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.application.DrewelApplication.Companion.admin_unread_count
import com.drewel.drewel.application.DrewelApplication.Companion.chat_id
import com.drewel.drewel.application.DrewelApplication.Companion.user_unread_count
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.firebase.MessageDataSource
import com.drewel.drewel.model.ChannelInfoModel
import com.drewel.drewel.model.ChatModel
import com.drewel.drewel.model.ChatUserModel
import com.drewel.drewel.model.MessageModel
import com.drewel.drewel.prefrences.Prefs
import com.drewel.drewel.rxbus.UnreadCountRxJavaBus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.chat.*
import kotlinx.android.synthetic.main.content_activity_otp_verification.*
import java.util.*
import java.util.concurrent.TimeUnit


class MessageDetail_Activity : BaseActivity(), View.OnClickListener, MessageDataSource.MessagesCallbacks {
    override fun onChannelAdded(message: ChatModel?) {
        if (message!!.channel_info!!.user_count != null) {
            user_unread_count = message.channel_info!!.user_count!!.toInt()
            UnreadCountRxJavaBus.getInstance().unreadCountRxJavaBus.onNext(user_unread_count.toString())
            if (user_unread_count > 0) {
                MessageDataSource.chatRoot.child(chat_id).child("channel_info").child("user_count").setValue("0")
                user_unread_count = 0
            }
        }
    }

    override fun onMessageAdded(message: ChatModel) {
        if(remainingTime < wait)
            timer?.dispose()

        Log.e("Model ==", message.toString())
        if (message.messages != null) {
            mylibArrayList.add(message)
            myLibAdapter!!.notifyDataSetChanged()
            rv_posts.smoothScrollToPosition(myLibAdapter!!.itemCount)
            MessageDataSource.chatRoot.child(chat_id).child("channel_info").child("message").setValue(message.messages!!.message)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                KeyboardUtils.hideSoftInput(this)
                onBackPressed()
                if (intent.getIntExtra(AppIntentExtraKeys.FROM, 0).equals(1)) {
                    var intent = Intent(this, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    var chatUserModel: ChatUserModel? = null
    var myLibAdapter: ChatMessageAdapter? = null
    lateinit var mylibArrayList: ArrayList<ChatModel>
    internal var imageLoader: com.nostra13.universalimageloader.core.ImageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance()
    internal var options = DisplayImageOptions.Builder().cacheInMemory(true)
            .cacheOnDisc(true).resetViewBeforeLoading(true).build()

    /*firebase chat variables*/
    var prefs: Prefs? = null
    var admin_id = ""
    var admin_img = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)
        prefs = Prefs(this)
        if (intent.getIntExtra(AppIntentExtraKeys.FROM, 0).equals(1)) {
            callChatAPI()
        } else {
            admin_img = intent.getStringExtra("admin_img")
            admin_id = intent.getStringExtra("admin_id")
            mListener = MessageDataSource.addMessagesListener(chat_id, this)
            setView()
        }

    }

    private fun callChatAPI() {
        val logoutRequest = HashMap<String, String>()
        logoutRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        logoutRequest["language"] = DrewelApplication.getInstance().getLanguage()

        val logoutObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).add_chat(logoutRequest)
        logoutObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    // DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!,context!!)
//                    com.os.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        admin_img = result.response!!.data!!.img!!
                        admin_id = result.response!!.data!!.admin_id!!
                        chat_id = result.response!!.data!!.admin_id + Prefs(DrewelApplication.getInstance()).getPreferenceStringData(Prefs(DrewelApplication.getInstance()).KEY_USER_ID)
                        mListener = MessageDataSource.addMessagesListener(chat_id, this)
                        setView()
//                        val intent = Intent(activity, WelcomeActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(intent)
//                        activity!!.finish()
                    }
                }, { error ->
                    Log.e("TAG", "{$error.message}")
                })
    }

    private var mListener: MessageDataSource.MessagesListener? = null
    private var channelListener: MessageDataSource.ChannelListener? = null
    private fun setView() {
        txt_msg.hint = "Type message"
        toolbarTitleTv.text = getString(R.string.chat_support)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        mylibArrayList = ArrayList()
        rv_posts.layoutManager = LinearLayoutManager(this)
        rv_posts.setHasFixedSize(true)
        rv_posts.adapter = myLibAdapter
        rv_posts.itemAnimator = DefaultItemAnimator()
        myLibAdapter = ChatMessageAdapter(this, mylibArrayList)
        rv_posts.adapter = myLibAdapter

        val timeStamp = System.currentTimeMillis().toString() + ""
        var channelInfoModel = ChannelInfoModel()
        channelInfoModel.admin_count = admin_unread_count
        channelInfoModel.receiver_id = admin_id
        channelInfoModel.receiver_name = "admin"
        channelInfoModel.receiver_profile_image = admin_img
        channelInfoModel.sender_id = prefs!!.getPreferenceStringData(prefs!!.KEY_USER_ID)
        channelInfoModel.user_count = 0
        channelInfoModel.time = timeStamp
        user_unread_count = 0
        MessageDataSource.chatRoot.child(chat_id).child("channel_info").setValue(channelInfoModel)

        txt_msg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                img_send.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        img_send.setOnClickListener {

            admin_unread_count += 1
            val timeStamp = System.currentTimeMillis().toString() + ""
            var channelInfoModel: ChannelInfoModel = ChannelInfoModel()
            channelInfoModel.message = txt_msg.text.toString().trim()
            channelInfoModel.admin_count = admin_unread_count
            channelInfoModel.receiver_id = admin_id
            channelInfoModel.receiver_name = "admin"
            channelInfoModel.receiver_profile_image = admin_img
            channelInfoModel.sender_id = prefs!!.getPreferenceStringData(prefs!!.KEY_USER_ID)
            channelInfoModel.user_count = 0
            channelInfoModel.time = timeStamp
            user_unread_count = 0

            var messageModel: MessageModel = MessageModel()
            messageModel.message = txt_msg.text.toString().trim()
            messageModel.msg_channel = chat_id
            messageModel.receiver_id = admin_id
            messageModel.receiver_name = "admin"
            messageModel.receiver_profile_image = admin_img
            messageModel.sender_id = prefs!!.getPreferenceStringData(prefs!!.KEY_USER_ID)
            messageModel.sender_name = prefs!!.getPreferenceStringData(prefs!!.KEY_FIRST_NAME) + " " + prefs!!.getPreferenceStringData(prefs!!.KEY_LAST_NAME)
            messageModel.sender_profile_image = prefs!!.getPreferenceStringData(prefs!!.KEY_PHOTO)
            messageModel.time = timeStamp
            var chatModel = ChatModel()
            chatModel.channel_info = channelInfoModel
            chatModel.messages = messageModel

            if(timer==null)
            setTimerForIdealMsg()

            MessageDataSource.saveMessage(chatModel, chat_id)
            txt_msg.setText("")
        }
    }


    override fun onDestroy() {
        MessageDataSource.chatRoot.child(chat_id).child("channel_info").child("user_count").setValue("0")
        user_unread_count = 0
        UnreadCountRxJavaBus.getInstance().unreadCountRxJavaBus.onNext("0")

        if(timer!=null)
            timer?.dispose()

        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (intent.getIntExtra(AppIntentExtraKeys.FROM, 0).equals(1)) {
            var intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
        }
    }

    private var remainingTime: Long=0
    private val wait = 15
    private var timer: Disposable? = null
    private fun setTimerForIdealMsg() {
        timer = Observable.intervalRange(0, 61, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({ result ->
                     remainingTime = wait - result
                    val resendTxt = remainingTime.toString()
                     //resendOTPTv.text = resendTxt
                    if (result >= wait) {
                        timer!!.dispose()
                        Log.d("10", ">>Done")

                        admin_unread_count += 1
                        val timeStamp = System.currentTimeMillis().toString() + ""
                        var channelInfoModel: ChannelInfoModel = ChannelInfoModel()
                        channelInfoModel.message = getString(R.string.static_msg)
                        channelInfoModel.admin_count = admin_unread_count
                        channelInfoModel.receiver_id =  prefs!!.getPreferenceStringData(prefs!!.KEY_USER_ID)
                        channelInfoModel.receiver_name =prefs!!.getPreferenceStringData(prefs!!.KEY_FIRST_NAME) + " " + prefs!!.getPreferenceStringData(prefs!!.KEY_LAST_NAME)
                        channelInfoModel.receiver_profile_image =  prefs!!.getPreferenceStringData(prefs!!.KEY_PHOTO)
                        channelInfoModel.sender_id =admin_id
                        channelInfoModel.user_count = 0
                        channelInfoModel.time = timeStamp
                        user_unread_count = 0

                        var messageModel: MessageModel = MessageModel()
                        messageModel.message = getString(R.string.static_msg)
                        messageModel.msg_channel = chat_id
                        messageModel.receiver_id = prefs!!.getPreferenceStringData(prefs!!.KEY_USER_ID)
                        messageModel.receiver_name = prefs!!.getPreferenceStringData(prefs!!.KEY_FIRST_NAME) + " " + prefs!!.getPreferenceStringData(prefs!!.KEY_LAST_NAME)
                        messageModel.receiver_profile_image =  prefs!!.getPreferenceStringData(prefs!!.KEY_PHOTO)
                        messageModel.sender_id =admin_id
                        messageModel.sender_name = "admin"
                        messageModel.sender_profile_image = admin_img
                        messageModel.time = timeStamp
                        var chatModel = ChatModel()
                        chatModel.channel_info = channelInfoModel
                        chatModel.messages = messageModel
                        
                        MessageDataSource.saveMessage(chatModel, chat_id)

                    }
                },
                        { error -> Log.e("TAG", "{$error.message}") },
                        { Log.d("TAG", "completed") })
    }

}
