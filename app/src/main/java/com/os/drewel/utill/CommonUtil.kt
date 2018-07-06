@file:Suppress("DEPRECATION")

package com.os.drewel.utill

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.os.drewel.R

object CommonUtil {

    fun showLoadingDialog(context: Context): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()
        if (progressDialog.window != null) {
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setContentView(R.layout.progress_dialog_loading)
        //        progressDialog.setContentView(R.layout.progress_dialog_logo_loading);
        //        ((AnimationDrawable) (progressDialog.findViewById(R.id.progress_dialog_logo_iv_e_1)).getBackground()).start();
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        return progressDialog
    }

    fun isValidString(string: String?): Boolean {
        return string!!.isNotEmpty() && !string.equals("null", ignoreCase = true)
    }
    /*fun showLoadingDialog(context: Context, message: String): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()
        if (progressDialog.window != null) {
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setContentView(R.layout.progress_dialog_loading)
        //        progressDialog.setContentView(R.layout.progress_dialog_logo_loading);
        //        ((TextView) progressDialog.findViewById(R.id.progress_dialog_logo_tv_message)).setText(message);
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        return progressDialog
    }*/
}