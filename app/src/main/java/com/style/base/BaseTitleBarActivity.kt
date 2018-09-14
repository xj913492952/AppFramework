package com.style.base

import android.os.Build
import android.support.annotation.StringRes
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.style.framework.R


abstract class BaseTitleBarActivity : BaseActivity() {

    private lateinit var statusBar: View
    private lateinit var tvTitleBase: TextView

    override fun setContentView(contentView: View) {
        super.setContentView(contentView)
        initTitleBar(contentView)
    }

    private fun initTitleBar(mContentView: View) {
        statusBar = mContentView.findViewById(R.id.status_bar)
        val ivBaseToolbarReturn = mContentView.findViewById<ImageView>(R.id.iv_base_toolbar_Return)
        tvTitleBase = mContentView.findViewById(R.id.tv_base_toolbar_title)
        ivBaseToolbarReturn.setOnClickListener { v -> onClickTitleBack() }
        initStatusBarHeight()
    }

    private fun initStatusBarHeight() {
        when (getStatusBarStyle()) {
            STATUS_BAR_TRANSPARENT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setTransparentStatusBarHeight(getStatusHeight())
                } else {
                    setTransparentStatusBarHeight(0)
                }
            }
            STATUS_BAR_TRANSLUCENT -> {
                setTransparentStatusBarHeight(getStatusHeight())
            }
            STATUS_BAR_COLOR -> {
                setTransparentStatusBarHeight(0)
            }
            STATUS_BAR_THEME -> {
                setTransparentStatusBarHeight(0)
            }
        }
    }

    open fun setTransparentStatusBarHeight(height: Int) {
        statusBar.layoutParams.height = height
    }

    open fun onClickTitleBack() {
        onBackPressed()
    }

    open fun setToolbarTitle(title: String) {
        tvTitleBase.text = title
    }

    open fun setToolbarTitle(@StringRes resId: Int) {
        tvTitleBase.text = context.getString(resId)
    }

}
