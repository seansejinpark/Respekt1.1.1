package com.respekt.fragments

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.respekt.R
import com.respekt.utils.MeData

class PurchaseDialogFragment() {
    lateinit var onClosePurchasedDialog: OnClosePurchasedDialog

    var dialog: Dialog? = null
    fun openDialog(
        context: Context,
        isFromMeScreen: Boolean,
        onClosePurchasedDialog: OnClosePurchasedDialog
    ) {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(isFromMeScreen)
        dialog?.setContentView(R.layout.dialog_purchase)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        this.onClosePurchasedDialog = onClosePurchasedDialog

        val tvSubscribe = dialog?.findViewById<TextView>(R.id.tvSubscribe)
        val tvPolicy = dialog?.findViewById<TextView>(R.id.tvPolicy)
        val tvTermsUse = dialog?.findViewById<TextView>(R.id.tvTermsUse)
        val tvClose = dialog?.findViewById<TextView>(R.id.tvClose)

        val purchasedTitle = dialog?.findViewById<TextView>(R.id.purchasedTitle)
        val purchasedDesc = dialog?.findViewById<TextView>(R.id.purchasedDesc)

        if (MeData.getPurchasedState() == MeData.NotPurchased) {
            purchasedTitle?.text = context.getString(R.string.policy_subscription_title)
            purchasedDesc?.text = context.getString(R.string.policy_subscription)
        } else {
            purchasedTitle?.text = context.getString(R.string.policy_subscription_expire_title)
            purchasedDesc?.text = context.getString(R.string.policy_subscription_expire)
        }

        tvSubscribe?.setOnClickListener {
            onClosePurchasedDialog.onSubscribeClicked()
            dialog?.dismiss()

        }
        tvPolicy?.setOnClickListener {
            dialog?.dismiss()
            onClosePurchasedDialog.onPrivacyClicked()

        }
        tvTermsUse?.setOnClickListener {
            dialog?.dismiss()
            onClosePurchasedDialog.onTermsClicked()
        }
        tvClose?.setOnClickListener {
            if (isFromMeScreen)
                dialog?.dismiss()
            else {
                dialog?.dismiss()
            }
            onClosePurchasedDialog.onClose()
        }
        dialog?.show()
    }

    public fun getPurchasedDialog(): Dialog? {
        return dialog
    }


    public interface OnClosePurchasedDialog {
        public fun onClose()
        public fun onSubscribeClicked()
        public fun onPrivacyClicked()
        public fun onTermsClicked()
    }

}