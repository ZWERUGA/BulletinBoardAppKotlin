package com.example.bulletinboardappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.bulletinboardappkotlin.HelperFunctions.toastMessage
import com.example.bulletinboardappkotlin.activities.EditAdsActivity
import com.example.bulletinboardappkotlin.databinding.ActivityMainBinding
import com.example.bulletinboardappkotlin.dialoghelper.DialogConsts
import com.example.bulletinboardappkotlin.dialoghelper.DialogHelper
import com.example.bulletinboardappkotlin.dialoghelper.GoogleAccountConsts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var tvAccountTitle: TextView

    private lateinit var rootElement: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_new_ad_button, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.new_advertisement) {
            val intent = Intent(this, EditAdsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)
    }

    private fun init() {
        setSupportActionBar(rootElement.headerActivityMain.tbActivityMain)
        val toggle = ActionBarDrawerToggle(this, rootElement.drawerLayout,
            rootElement.headerActivityMain.tbActivityMain, R.string.open, R.string.close)
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        rootElement.navigationView.setNavigationItemSelectedListener(this)
        tvAccountTitle = rootElement.navigationView
            .getHeaderView(0)
            .findViewById(R.id.tvAccountTitle)
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
        when(item.itemId) {
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
                updateUI(null)
                mAuth.signOut()
                dialogHelper.accountHelper.signOutGoogle()
                toastMessage(this, resources.getString(
                    R.string.sign_out_done))
            }
        }

        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateUI(user: FirebaseUser?) {
        tvAccountTitle.text = if (user == null) {
            resources.getString(R.string.need_auth_or_reg)
        } else {
            user.email
        }
    }
}