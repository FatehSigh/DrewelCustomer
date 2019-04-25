package com.drewel.drewel.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.jakewharton.rxbinding2.widget.RxTextView
import com.drewel.drewel.R
import com.drewel.drewel.adapter.SearchSuggestionAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.searchsuggestionresponsemodel.SearchSuggestionResponse
import com.drewel.drewel.application.DrewelApplication
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.search_suggestion_layout.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * Created by monikab on 3/30/2018.
 */
class SearchSuggestionActivity : BaseActivity() {

    private var searchSuggestionAdapter: SearchSuggestionAdapter? = null
    private var searchSuggestionList: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_suggestion_layout)
        initView()

    }

    private fun initView() {

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        RxTextView.textChangeEvents(search_product)
                .debounce(400, TimeUnit.MILLISECONDS)
                // Better store the value in a constant like Constant.DEBOUNCE_SEARCH_REQUEST_TIMEOUT
                .map {
                    if (it.text().toString().trim().isEmpty()) {
                        clearSearchResult()
                    }
                    it.text().toString().trim()
                }
                .filter {
                    it.length > 2
                }
                .flatMap {

                    getSearch(it).subscribeOn(Schedulers.io())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    Log.d("result", result.toString())
                    if (result.response!!.status == true) {
                        searchSuggestionList = result.response!!.data!!.suggestions!!
                        searchSuggestionRecyclerView.visibility = View.VISIBLE
                        setSearchAdapter()
                    } else {
                        searchSuggestionRecyclerView.visibility = View.INVISIBLE
                        com.drewel.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
                    }
                }
                        , { error ->
                    //Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    private fun clearSearchResult() {
        runOnUiThread{
            searchSuggestionList = ArrayList()
            setSearchAdapter()
        }
    }

    private fun setSearchAdapter() {
        if (searchSuggestionAdapter == null) {
            searchSuggestionRecyclerView.layoutManager = LinearLayoutManager(this)
            searchSuggestionAdapter = SearchSuggestionAdapter(this, searchSuggestionList)
            searchSuggestionRecyclerView.adapter = searchSuggestionAdapter

        } else
            searchSuggestionAdapter?.searchSuggestionList = searchSuggestionList
        searchSuggestionRecyclerView.adapter?.notifyDataSetChanged()

    }

    private fun getSearch(searchText: String): Observable<SearchSuggestionResponse> {

        val getSearchSuggestionRequest = HashMap<String, String>()
        getSearchSuggestionRequest["key"] = searchText
        if(pref!!.getPreferenceStringData(pref!!.KEY_USER_ID).isEmpty())
            getSearchSuggestionRequest["user_id"] = "1"
        else
        getSearchSuggestionRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getSearchSuggestionRequest["language"] = DrewelApplication.getInstance().getLanguage()
        //search_product.isEnabled=false
        return DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getSearchSuggestion(getSearchSuggestionRequest)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                KeyboardUtils.hideSoftInput(this)
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}