package com.example.twittercloneapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.twittercloneapp.R
import com.example.twittercloneapp.commons.ProgressDialog
import com.example.twittercloneapp.extensions.isValidEmail
import com.example.twittercloneapp.viewmodel.FirebaseAuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.etEmail

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {
    private val authViewModel: FirebaseAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        initView()
    }

    private fun initView() {
        btn_back.setOnClickListener {
            onBackPressed()
        }
        showProgressBar()
        btnRegister.setOnClickListener {
            if (isValidData()) {
                authViewModel.createUser(
                        etName.text.toString(),
                        etEmail.text.toString(),
                        etPhone.text.toString(),
                        etPassword.text.toString()
                )
            }
        }

        authViewModel.userLiveData?.observe(this, Observer {
            if (it != null) {
                Toast.makeText(this, "Utilisateur ${it.email}  connecté avec succés", Toast.LENGTH_LONG)
                        .show()
                navigateToMainActivity()
            }
            authViewModel.progress.value = false
        })

    }

    private fun showProgressBar() {
        val dialog =  ProgressDialog.dialog(this, "Connexion ...")
        authViewModel.progress.observe(this, Observer { showing ->
            if (showing) {
               dialog.show()
            } else {
                dialog.dismiss()
            }
        })
    }

    private fun isValidData(): Boolean {
        if (etName.text.isNullOrEmpty() || etName.text.toString().length <= 3) {
            etName.error = "Veuillez entrer un nom valide"
            return false
        } else if (etEmail.text.isNullOrEmpty() || !etEmail.text.toString().isValidEmail()) {
            etEmail.error = "Veuillez saisir un e-mail valide"
            return false
        } else if (etPhone.text.isNullOrEmpty() || etPhone.text.toString().length != 10) {
            etPhone.error = "Veuillez entrer un numéro de téléphone valide"
            return false
        } else if (etPassword.text.isNullOrEmpty() || etPassword.text!!.length < 6) {
            etPassword.error = "La longueur du mot de passe doit être supérieure à 6"
            return false
        } else if (etConfirmPassword.text.isNullOrEmpty() || (etPassword.text.toString() != etConfirmPassword.text.toString())) {
            etConfirmPassword.error = "Les mots de passe doivent correspondre"
            return false
        }
        return true
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
