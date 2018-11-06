package com.os.drewel.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.jakewharton.rxbinding2.widget.RxTextView
import com.os.drewel.R
import com.os.drewel.adapter.SearchSuggestionAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.searchsuggestionresponsemodel.SearchSuggestionResponse
import com.os.drewel.application.DrewelApplication
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.search_suggestion_layout.*
import kotlinx.android.synthetic.main.search_suggestion_layout.view.*
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

        /*jave code*/
        /*      RxTextView.textChangeEvents(searchbar.getEditText())
                      .debounce(400, TimeUnit.MILLISECONDS)
                      .map(new Func1<TextViewTextChangeEvent, String>() {
                          @Override
                          public String call(TextViewTextChangeEvent text) {
                              return text.text().toString();
                          }
                      })
                      .filter(new Func1<String, Boolean>() {
                          @Override
                          public Boolean call(String text) {
                              return (text.length() > 2);
                          }
                      })
                      .flatMap(new Func1<String, rx.Observable<Data>>() {
                          @Override
                          public rx.Observable<Data> call(String text) {
                              return getSearch(text);
                          }
                      })
                      .subscribeOn(Schedulers.io()) // Or Schedulers.newThread()
                      .observeOn(AndroidSchedulers.mainThread());*/

        /* handle api call when user type fast to search product.*/
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
                        com.os.drewel.utill.Utils.getInstance().showToast(this,result.response!!.message!!)
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
        searchSuggestionRecyclerView.adapter.notifyDataSetChanged()

    }

    private fun getSearch(searchText: String): Observable<SearchSuggestionResponse> {

        val getSearchSuggestionRequest = HashMap<String, String>()
        getSearchSuggestionRequest["key"] = searchText
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