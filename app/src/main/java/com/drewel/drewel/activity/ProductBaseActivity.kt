package com.drewel.drewel.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import com.drewel.drewel.R
import com.drewel.drewel.rxbus.CartRxJavaBus
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.action_bar_notifitcation_icon.view.*


/**
 * Created by monikab on 2/21/2018.
 */

/* this activity is just to handle common option menu*/
open class ProductBaseActivity : BaseActivity() {


    lateinit var cartRxBustDisposable: Disposable
    private lateinit var cartItemView: View
    private lateinit var countItemView: View
    public var isAddressChange = false
    var driveActivityName = ""
    var menu: Menu? = null
    lateinit var unreadCountRxJavaBus:  Disposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        chat_id = "a1_u" + Prefs(DrewelApplication.getInstance()).getPreferenceStringData(Prefs(DrewelApplication.getInstance()).KEY_USER_ID)
        subscribeCartBus()

    }

    /* */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_whishlist -> {

                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@ProductBaseActivity,WelcomeActivity::class.java))
                    finish()
                }else{
                    val intent = Intent(this, WishListActivity::class.java)
                    startActivity(intent)
                }

                return true
            }
            R.id.menu_addrequest -> {
                if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
                {
                    startActivity(Intent(this@ProductBaseActivity,WelcomeActivity::class.java))
                    finish()
                }else
                startActivityForResult(Intent(this, RequestProductActivity::class.java), 2000)
//                val intent = Intent(this, RequestProductActivity::class.java)
//                startActivity(intent)
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* set layout of menu*/
        menuInflater.inflate(R.menu.product_list_menu, menu)
        this.menu = menu
        /* if child activity is product activity then visible filer menu icon*/
        menu.findItem(R.id.menu_filter).isVisible = driveActivityName == ProductActivity().javaClass.name
        /* if child activity is wishlist activity then hide wishlist menu icon*/
        menu.findItem(R.id.menu_whishlist).isVisible = driveActivityName != WishListActivity().javaClass.name
//        menu.findItem(R.id.menu_whishlist).isVisible = driveActivityName != RequestFragment().javaClass.name
//        menu.findItem(R.id.menu_filter).isVisible = driveActivityName != RequestFragment().javaClass.name
//        menu.findItem(R.id.menu_whishlist).isVisible = driveActivityName != RequestFragment().javaClass.name
//        menu.findItem(R.id.menu_addrequest).isVisible = driveActivityName == RequestFragment().javaClass.name
        getViewOfCartMenuItem(menu)
        /* se click listener of toolbar cart icon*/
        cartItemView.setOnClickListener {

            if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
            {
                startActivity(Intent(this@ProductBaseActivity,WelcomeActivity::class.java))
                finish()
            }else{
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }

        }
        return true
    }

    /* get view of cart menu item*/
    private fun getViewOfCartMenuItem(menu: Menu): View {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.itemId == R.id.menu_carts) {
                cartItemView = item.actionView
                /* get cart item quantity and set it*/
                val cartQuantity = pref!!.getPreferenceStringData(pref!!.KEY_CART_ITEM_COUNT)
                cartItemView.cartItemCountTv.text = cartQuantity

                setDynamicallyParam(cartQuantity)

                if (cartQuantity == "0" || cartQuantity.isEmpty())
                    cartItemView.cartItemCountTv.visibility = View.GONE
                else
                    cartItemView.cartItemCountTv.visibility = View.VISIBLE

                return cartItemView
            }
        }
        return cartItemView
    }

    override fun onDestroy() {
        super.onDestroy()
        cartRxBustDisposable.dispose()
    }

    /* when user call add to cart api then update count over cart menu item*/
    private fun subscribeCartBus() {
        cartRxBustDisposable = CartRxJavaBus.getInstance().cartPublishSubject.subscribe(
                { cartItemQuantity ->
                    cartItemView.cartItemCountTv.text = cartItemQuantity

                    setDynamicallyParam(cartItemQuantity)

                    pref!!.setPreferenceStringData(pref!!.KEY_CART_ITEM_COUNT, cartItemQuantity)
                    if (cartItemQuantity.isEmpty() || cartItemQuantity == "0")
                        cartItemView.cartItemCountTv.visibility = View.GONE
                    else
                        cartItemView.cartItemCountTv.visibility = View.VISIBLE
                },
                { error ->
                    Log.d("error", error.message)
                }
        )
    }

    /* change height width of cart item count*/
    private fun setDynamicallyParam(cartItemQuantity: String) {
        if (cartItemQuantity.length > 2) {
            cartItemView.cartItemCountTv.measure(0, 0)
            val width = cartItemView.cartItemCountTv.measuredWidth

            val linearPram = RelativeLayout.LayoutParams(width, width)
            val marginInDp = -5
            val marginInPx = marginInDp * resources.displayMetrics.density
            linearPram.setMargins(marginInPx.toInt(), marginInPx.toInt(), 0, 0)
            linearPram.addRule(RelativeLayout.END_OF, R.id.cart_icon)

            cartItemView.cartItemCountTv.layoutParams = linearPram
        }
    }


}





