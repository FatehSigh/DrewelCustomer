package com.drewel.drewel.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.drewel.drewel.R
import com.drewel.drewel.utill.ShareAppConstant
import kotlinx.android.synthetic.main.share_product_dialog.*


/**
 * Created by monikab on 3/21/2018.
 */
class ShareBottomSheetDialog(val mContext: Context, private var callbackManager: CallbackManager) : BottomSheetDialog(mContext), View.OnClickListener {


    var shareImagePath = ""
    var productPrice = ""
    var productTitle = ""
    var shareImageURL = ""
    var activity: Activity? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(context.getString(R.string.share_product))
        setContentView(R.layout.share_product_dialog)

        shareWithGmailBt.setOnClickListener(this)
        shareWithFacebookBt.setOnClickListener(this)
        shareWithMessageBt.setOnClickListener(this)
        shareWithWhatsappBt.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()
        val lp = WindowManager.LayoutParams()
        val window = window
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
    }


    override fun onClick(view: View) {

        when (view.id) {

            R.id.shareWithGmailBt -> {
                dismiss()
                sharingImageToGmail()
            }

            R.id.shareWithWhatsappBt -> {
                dismiss()
                shareImageToTwitter()
            }

            R.id.shareWithFacebookBt -> {
                dismiss()
                val isAppInstalled = ShareAppConstant.isAppInstalled(ShareAppConstant.PACKAGE_FACEBOOK_KATANA, mContext)
                if (isAppInstalled) {
                    shareToFb()
                } else {
                    com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.install_facebook))
                    val shareIntent = Intent(Intent.ACTION_VIEW)
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    shareIntent.data = Uri.parse(ShareAppConstant.playStoreURL + ShareAppConstant.PACKAGE_FACEBOOK_KATANA)
                    mContext.startActivity(shareIntent)
                }
            }

            R.id.shareWithMessageBt -> {
                dismiss()
                sharingImageToMessage()
            }

        }
    }


/*    private fun shareImageToWhatsApp() {

        val productDescription = mContext.getString(R.string.check_out_this) + " " + productTitle + " " + mContext.getString(R.string.at) + " " + productPrice

        val isAppinstalled = ShareAppConstant.isAppInstalled(ShareAppConstant.PACKAGE_WHATSAPP, mContext)
        if (isAppinstalled) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.`package` = ShareAppConstant.PACKAGE_WHATSAPP
            try {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(mContext.contentResolver, shareImagePath, ShareAppConstant.dummyTextShare, productDescription)))
                shareIntent.type = ShareAppConstant.INTENT_TYPE
                shareIntent.putExtra(Intent.EXTRA_TEXT, productDescription)
                mContext.startActivity(shareIntent)

            } catch (e: Exception) {
                Toast.makeText(mContext, mContext.getString(R.string.install_whatsapp), Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(mContext, mContext.getString(R.string.install_whatsapp), Toast.LENGTH_SHORT).show()
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.data = Uri.parse(ShareAppConstant.playStoreURL + ShareAppConstant.PACKAGE_WHATSAPP)
            mContext.startActivity(shareIntent)
        }

    }*/


    private fun shareImageToTwitter() {

        val productDescription = mContext.getString(R.string.check_out_this) + " " + productTitle + " " + mContext.getString(R.string.at) + " " + productPrice

        val isAppInstalled = ShareAppConstant.isAppInstalled(ShareAppConstant.PACKAGE_TWITTER, mContext)
        if (isAppInstalled) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.`package` = ShareAppConstant.PACKAGE_TWITTER
            try {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(mContext.contentResolver, shareImagePath, ShareAppConstant.dummyTextShare, productDescription)))
                shareIntent.type = ShareAppConstant.INTENT_TYPE
                shareIntent.putExtra(Intent.EXTRA_TEXT, productDescription)
                mContext.startActivity(shareIntent)

            } catch (e: Exception) {
                com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.install_twitter))
            }

        } else {
            com.drewel.drewel.utill.Utils.getInstance().showToast(mContext, mContext.getString(R.string.install_twitter))
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.data = Uri.parse(ShareAppConstant.playStoreURL + ShareAppConstant.PACKAGE_TWITTER)
            mContext.startActivity(shareIntent)
        }

    }


    private fun sharingImageToGmail() {

        val productDescription = mContext.getString(R.string.check_out_this) + " " + productTitle + " " + mContext.getString(R.string.at) + " " + productPrice
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.`package` = ShareAppConstant.PACKAGE_GMAIL
        try {
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(mContext.contentResolver, shareImagePath, ShareAppConstant.dummyTextShare, productDescription)))
            shareIntent.type = ShareAppConstant.INTENT_TYPE
            shareIntent.putExtra(Intent.EXTRA_TEXT, productDescription)
            mContext.startActivity(shareIntent)
        } catch (e: Exception) {
            //Toast.makeText(mContext, mContext.getString(R.string.install_whatsapp), Toast.LENGTH_SHORT).show();
        }
    }


    private fun shareToFb() {
        val permissionNeeds = listOf("publish_actions")
        // Set permissions
        LoginManager.getInstance().logInWithPublishPermissions(mContext as AppCompatActivity, permissionNeeds)
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken == null) {
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    shareOnFacebook()
                }

                override fun onCancel() {
                    println("onCancel")
                }

                override fun onError(exception: FacebookException) {
                    println("onError")
                    LoginManager.getInstance().logOut()
                }
            })
        } else {
            shareOnFacebook()
        }
    }

    private fun shareOnFacebook() {
        val productDescription = mContext.getString(R.string.check_out_this) + " " + productTitle + " " + mContext.getString(R.string.at) + " " + productPrice
        val content = ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(shareImageURL))
                .setQuote(productDescription)
                .build()

        val shareDialog = ShareDialog(mContext as AppCompatActivity)
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC)
    }


    private fun sharingImageToMessage() {
        try {
            val productDescription = mContext.getString(R.string.check_out_this) + " " + productTitle + " " + mContext.getString(R.string.at) + " " + productPrice

            val uri = "smsto:"
            val sendIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(uri))
            sendIntent.putExtra("sms_body", productDescription)
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(mContext.contentResolver, shareImagePath, ShareAppConstant.dummyTextShare, productDescription)))
            mContext.startActivity(sendIntent)
        } catch (e: Exception) {

        }

    }


}