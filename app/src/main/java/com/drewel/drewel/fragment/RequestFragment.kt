package com.drewel.drewel.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.drewel.drewel.R
import com.drewel.drewel.activity.HomeActivity
import com.drewel.drewel.activity.RequestProductActivity
import com.drewel.drewel.adapter.ProductRequestListAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.productRequestresponsemodel.ProductRequest
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.delegate.OnClickItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_discount.*
import android.view.MenuInflater


/**
 * Created by sharukhb on 3/13/2018.
 */

class RequestFragment : BaseFragment(), OnClickItem {
    var position = 0
    override fun onClick(tag: String, position: Int) {
        Log.e("position==", position.toString())
        this@RequestFragment.position = position
        if (tag.equals("Delete")) {
            if (isNetworkAvailable())
                callDeleteApi(position)
        } else if (tag.equals("edit")) {
            startActivityForResult(Intent(activity, RequestProductActivity::class.java).putExtra("From", 1).putExtra("Data", couponList[position]), 2000)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_addrequest -> {
                (Intent(activity, RequestProductActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2000) {
            if (data != null) {
//                var productRequest = data.getSerializableExtra("Data") as ProductRequest
//                couponList[position] = productRequest
//                discountAdapter!!.notifyDataSetChanged()
                if (isNetworkAvailable())
                    callCategoryListApi()
            }
        }
    }

    private fun callDeleteApi(position: Int) {
        setProgressState(View.VISIBLE)
        val reorderRequest = java.util.HashMap<String, String>()
        reorderRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        reorderRequest["language"] = DrewelApplication.getInstance().getLanguage()
        reorderRequest["request_id"] = couponList[position].request_id!!

        val cancelOrderObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).delete_product_request(reorderRequest)

        val disposable = cancelOrderObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, activity!!)
                    setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(activity, result.response!!.message!!)
                    if (result.response!!.status!!) {
                        if (isNetworkAvailable())
                            callCategoryListApi()
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        updateMenuTitles()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
//        var activity = activity as HomeActivity
//        if (activity.menu == null)
//            return
        menu!!.findItem(R.id.menu_addrequest).isVisible = true
        menu!!.findItem(R.id.menu_filter).isVisible = false
        menu!!.findItem(R.id.menu_carts).isVisible = false
        menu!!.findItem(R.id.menu_whishlist).isVisible = false
    }

    private var discountAdapter: ProductRequestListAdapter? = null
    var disposable: Disposable? = null

    var couponList: ArrayList<ProductRequest> = ArrayList<ProductRequest>()
    var isFromCheckout = false
    var isCalled = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_discount, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* call api if network available8*/
//        updateMenuTitles()
//        activity.driveActivityName = this.javaClass.name
    }

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable())
            callCategoryListApi()
    }

    override fun onDetach() {
        super.onDetach()

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val intentFilter = IntentFilter()
        intentFilter.addAction("UPDATE_ACCEPTED")
        activity!!.registerReceiver(broadcastReceiver, intentFilter)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            Toast.makeText(context, "This is the broadcast", Toast.LENGTH_SHORT).show();
            if (isNetworkAvailable())
                callCategoryListApi()
        }
    }

    private fun updateMenuTitles() {
        var activity = activity as HomeActivity
        if (activity.menu == null)
            return
        activity.menu!!.findItem(R.id.menu_addrequest).isVisible = true
        activity.menu!!.findItem(R.id.menu_filter).isVisible = false
        activity.menu!!.findItem(R.id.menu_carts).isVisible = false
        activity.menu!!.findItem(R.id.menu_whishlist).isVisible = false
    }

    private fun setAdapter() {
//        if (discountAdapter == null) {
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        discountRv!!.layoutManager = llm
//            discountRv.addItemDecoration(EqualSpacingItemDecoration(26, EqualSpacingItemDecoration.VERTICAL))
        discountAdapter = ProductRequestListAdapter(context, couponList, this)
        discountRv!!.adapter = discountAdapter
//        } else
//            discountAdapter!!.notifyDataSetChanged()
    }

    private fun callCategoryListApi() {
        setProgressState(View.VISIBLE)
        val categoryRequest = HashMap<String, String>()
        categoryRequest.put("user_id", pref!!.getPreferenceStringData(pref!!.KEY_USER_ID))
        categoryRequest.put("language", DrewelApplication.getInstance().getLanguage())

        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getProductRequestList(categoryRequest)
        disposable = signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE)
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, context!!)
                    if (result.response!!.status!!) {
                        couponList = result.response!!.data!!.productRequest!!
                        setAdapter()
                        if (couponList.isEmpty()) {
                            val intent = Intent(activity, RequestProductActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        if (couponList.isNotEmpty()) {
                            couponList.removeAt(0)
                            discountAdapter!!.notifyDataSetChanged()
                        }
                        if (isCalled) {
                            if (couponList.isEmpty()) {
                                isCalled = false
                                val intent = Intent(activity, RequestProductActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }, { error ->
                    setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(activity, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun setProgressState(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun onDestroyView() {
        for (i in couponList) {
            i.stopTimer = true
        }
        if (discountAdapter != null)
            discountAdapter!!.notifyDataSetChanged()
        super.onDestroyView()

    }

    override fun onDestroy() {
        for (i in couponList) {
            i.stopTimer = true
        }
        if (discountAdapter != null)
            discountAdapter!!.notifyDataSetChanged()
        super.onDestroy()
        if (disposable != null)
            disposable!!.dispose()
        activity!!.unregisterReceiver(broadcastReceiver)
    }

}
