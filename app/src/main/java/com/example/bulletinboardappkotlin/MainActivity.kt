package com.example.bulletinboardappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.accounthelper.AccountHelper
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.adapters.AdvertisementRecyclerViewAdapter
import com.example.bulletinboardappkotlin.databinding.ActivityMainBinding
import com.example.bulletinboardappkotlin.dialoghelper.DialogConsts
import com.example.bulletinboardappkotlin.dialoghelper.DialogHelper
import com.example.bulletinboardappkotlin.dialoghelper.GoogleAccountConsts
import com.example.bulletinboardappkotlin.model.Advertisement
import com.example.bulletinboardappkotlin.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdvertisementRecyclerViewAdapter.AdvertisementHolder.Listener {
    private lateinit var tvAccountTitle: TextView

    private lateinit var rootElement: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val advertisementRecyclerViewAdapter = AdvertisementRecyclerViewAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAds()
        bottomMenuOnClick()
    }

    override fun onResume() {
        super.onResume()
        rootElement.headerActivityMain.bNavView.selectedItemId = R.id.id_home
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this) {
            advertisementRecyclerViewAdapter.updateAdvertisementAdapter(it)
            rootElement.headerActivityMain.tvEmpty.visibility =
                if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun init() {
        setSupportActionBar(rootElement.headerActivityMain.tbActivityMain)
        val toggle = ActionBarDrawerToggle(
            this, rootElement.drawerLayout,
            rootElement.headerActivityMain.tbActivityMain, R.string.open, R.string.close
        )
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        rootElement.navigationView.setNavigationItemSelectedListener(this)
        tvAccountTitle = rootElement.navigationView
            .getHeaderView(0)
            .findViewById(R.id.tvAccountTitle)
    }

    private fun bottomMenuOnClick() = with(rootElement) {
        headerActivityMain.bNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.id_home -> {
                    firebaseViewModel.loadAllAds()
                    headerActivityMain.tbActivityMain.title = getString(R.string.toolbar_all_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavourites()
                    headerActivityMain.tbActivityMain.title =
                        getString(R.string.toolbar_favourite_ads)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    headerActivityMain.tbActivityMain.title = getString(R.string.toolbar_mine_ads)
                }
                R.id.id_new_advertisement -> {
                    val intent = Intent(this@MainActivity, EditAdsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        rootElement.apply {
            headerActivityMain.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            headerActivityMain.rcView.adapter = advertisementRecyclerViewAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleAccountConsts.GOOGLE_SIGN_IN_REQUEST_CODE) {
//            Log.d(TAG, "Sign in result!")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Api error: ${e.message}")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ads_mine_title -> {
                toastMessage(this, "Мои объявления")
            }
            R.id.ads_cars_title -> {
                toastMessage(this, "Автомобили")
            }
            R.id.ads_computers_title -> {
                toastMessage(this, "Компьютеры")
            }
            R.id.ads_smartphones_title -> {
                toastMessage(this, "Смартфоны")
            }
            R.id.ads_appliances_title -> {
                toastMessage(this, "Бытовая техника")
            }
            R.id.account_sign_up_title -> {
                dialogHelper.createSignDialog(DialogConsts.SIGN_UP_STATE)
            }
            R.id.account_sign_in_title -> {
                dialogHelper.createSignDialog(DialogConsts.SIGN_IN_STATE)
            }
            R.id.account_sign_out_title -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    rootElement.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                updateUI(null)
                mAuth.signOut()
                dialogHelper.accountHelper.signOutGoogle()
                toastMessage(
                    this, resources.getString(
                        R.string.sign_out_done
                    )
                )
            }
        }

        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accountHelper.signInAnonymously(object: AccountHelper.Listener {
                override fun onComplete() {
                    tvAccountTitle.text = getString(R.string.account_sign_in_anonymously)
                }
            })
        } else if (user.isAnonymous) {
            tvAccountTitle.text = getString(R.string.account_sign_in_anonymously)
        } else if (!user.isAnonymous) {
            tvAccountTitle.text = user.email
        }
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(advertisement: Advertisement) {
        firebaseViewModel.deleteItem(advertisement)
    }

    override fun onAdvertisementViewed(advertisement: Advertisement) {
        firebaseViewModel.advertisementViewed(advertisement)
    }

    override fun onFavouriteClicked(advertisement: Advertisement) {
        firebaseViewModel.onFavouriteClick(advertisement)
    }
}