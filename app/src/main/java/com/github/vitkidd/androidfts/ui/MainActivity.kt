package com.github.vitkidd.androidfts.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.facebook.stetho.Stetho
import com.github.vitkidd.androidfts.R
import com.github.vitkidd.androidfts.db.DbHelper
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private val movieAdapter: MovieAdapter = MovieAdapter(mutableListOf())
    val PREFS_FILENAME = "mgg"
    val DATA = "data"
    var prefs: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Stetho.initializeWithDefaults(this)
        prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        dbHelper = DbHelper(applicationContext)
        val data = prefs!!.getBoolean(DATA, false)
        if(!data) {
            dbHelper.populate(applicationContext)
            val editor = prefs!!.edit()
            editor.putBoolean(DATA, true)
            editor.apply()
        }
        findViewById<RecyclerView>(R.id.recycler_view).apply {
            adapter = movieAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        RxTextView.afterTextChangeEvents(findViewById(R.id.edit_text))
                //.debounce(500, TimeUnit.MILLISECONDS)
                .map { it.editable().toString() }
                .filter { it.isNotEmpty() && it.length > 0 }
                .flatMap { Observable.just(dbHelper.search_two(it)) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { movieAdapter.updateMovies(it) }
    }
}