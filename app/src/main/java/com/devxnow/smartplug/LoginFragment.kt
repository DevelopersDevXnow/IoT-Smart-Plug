package com.devxnow.smartplug


import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devxnow.smartplug.databinding.FragmentLoginBinding
import com.devxnow.smartplug.services.CheckInternet
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import es.dmoral.toasty.Toasty


class LoginFragment : Fragment() {


    companion object {
        private const val TAG = "LOGIN_TAG"
    }

    private var binding: FragmentLoginBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var email_id: EditText
    private lateinit var email: String
    private lateinit var password: String


    var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // reload();
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

// Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater)


        binding!!.etPassword.disableCopyPaste()

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)




        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLoginBinding.bind(view)

        //   Toast.makeText(requireContext(), "Google sign In", Toast.LENGTH_SHORT).show()

        //    getActivity()?.let { Alerter.create(it).setTitle("Google sign In").setText("text").show() };


        binding.rlSignInWithGoogle.setOnClickListener {
            // Do some work here


            //Check Internet Connection
            val checkInternet = CheckInternet()
            if (!checkInternet.isNetworkAvailable(requireContext())) {
                checkInternet.showNetworkDisconnectPopupDialog(requireActivity())
                return@setOnClickListener
            }


            signInGoogle()
        }

        binding.btnSignin.setOnClickListener {

            email = binding.etEmail.text.toString().trim()
            password = binding.etPassword.text.toString().trim()

            //validate Email and Password
            if (!validateEmail() or !validatePassword()) {

                return@setOnClickListener

            }


            //Check Internet Connection
            val checkInternet = CheckInternet()
            if (!checkInternet.isNetworkAvailable(requireContext())) {
                checkInternet.showNetworkDisconnectPopupDialog(requireActivity())
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        updateEmailLoginUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.", LENGTH_SHORT
                        ).show()
                        //  updateUI(null)
                    }
                }


        }

        //Text click to show dialog
        binding.tvForgotPassword.setOnClickListener {

            //Inflate the dialog with custom view
            val mDialogView =
                LayoutInflater.from(context).inflate(R.layout.layout_reset_pop_dialog, null)

            //Material Alert Dialog Builder
            val mDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(mDialogView)
                .setCancelable(false)
                .setTitle("Reset Forgot Password?")
                .setMessage("Enter Your Email to get Password Reset Link")
                .setNegativeButton(resources.getString(R.string.cancle)) { dialog, which ->
                    // Respond to negative button press
                    dialog.dismiss()

                }.setPositiveButton(resources.getString(R.string.reset)) { dialog, which ->
                    // Respond to positive button press


                }

            val alertDialog = mDialogBuilder.create()

            alertDialog.show()
            val pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            pbutton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black));
            pbutton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            pbutton.setOnClickListener() {

                email_id =
                    mDialogView.findViewById(R.id.editTextTextEmailAddress) as EditText



                if (email_id.text.toString() == "") {

                    email_id.error = "Email cannot be empty!"

                    Toasty.info(
                        requireContext(), "Reset Email cannot be null ", Toasty.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                val checkEmail = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
                if (!email_id.text.toString().matches(checkEmail)) {

                    email_id.error = "Invalid Email!"
                    Toasty.info(
                        requireContext(), "Please Enter Valid Email ID ", Toasty.LENGTH_LONG
                    ).show()

                    return@setOnClickListener

                }


                Toasty.info(requireContext(), "Please Wait a while", Toasty.LENGTH_LONG).show()


                //Send Reset Verification Email
                auth.sendPasswordResetEmail(email_id.text.toString())
                    .addOnSuccessListener {
                        Toasty.success(
                            requireContext(),
                            "Reset Email Sent",
                            Toasty.LENGTH_LONG
                        ).show()
                    }.addOnFailureListener { e ->
                        Toasty.error(
                            requireContext(), """
     Error in sending email
     ${e.message}
     """.trimIndent(), Toasty.LENGTH_LONG
                        ).show()
                    }


            }


        }

        // Image view password icon
        binding.ivPasswordIcon.setOnClickListener {

            if (binding.etPassword.transformationMethod is PasswordTransformationMethod) {
                binding.etPassword.transformationMethod = null
                binding.ivPasswordIcon.setImageResource(R.drawable.password_hide)
            } else {
                binding.etPassword.transformationMethod = PasswordTransformationMethod()
                binding.ivPasswordIcon.setImageResource(R.drawable.password_show)
            }
            binding.etPassword.setSelection(binding.etPassword.length())


        }


        //Text click to navigate to Signup Screen

        binding.tvSignUp.setOnClickListener {

            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)

        }


    }


    private fun updateEmailLoginUI(user: FirebaseUser?) {

        activity?.let {
            val intent = Intent(it, HomeActivity::class.java)
            it.startActivity(intent)
        }

    }


    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }

        }

    /*
   Validation Functions
    */


    /*
 Email Validation Functions
   */
    fun validateEmail(): Boolean {


        val mEmail: String = binding!!.etEmail.text.toString().trim()
        val checkEmail = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return if (mEmail.isEmpty()) {
            binding!!.etEmail.error = "Field can not be empty"
            false
        } else if (!mEmail.matches(checkEmail)) {
            binding!!.etEmail.error = "Invalid Email!"
            false
        } else {
            binding!!.etEmail.error = null
            true
        }
    }


    /*
 Password Validation Functions
   */

    private fun validatePassword(): Boolean {
        val mPassword: String = binding!!.etPassword.getText().toString().trim()
        val checkPassword = "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                "(?=.*[a-z])" +  //at least 1 lower case letter
                "(?=.*[A-Z])" +  //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{4,}" +  //at least 4 characters
                "$"


        return if (mPassword.isEmpty()) {
            binding!!.etPassword.error = "Field can not be empty"
            false
        } else if (!mPassword.matches(checkPassword.toRegex())) {
            binding!!.etPassword.error = "Password format is incorrect!"
            false
        } else {
            binding!!.etPassword.error = null
            true
        }
    }


}


private fun handleResults(task: Task<GoogleSignInAccount>) {

    if (task.isSuccessful) {
        val account: GoogleSignInAccount? = task.result
        if (account != null) {

            updateUI(account)

        } else {

            val activity = requireContext().applicationContext as Activity

            Toast.makeText(activity, task.exception.toString(), LENGTH_SHORT).show()

            //           Alerter.create(activity).setTitle("Alert Title").setText("Alert text...").show()


        }


    }


}

fun requireContext(): Context {

    return requireContext().applicationContext
}


private fun updateUI(account: GoogleSignInAccount) {

    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
        if (it.isSuccessful) {
//            val intent : Intent = Intent(this , HomeActivity::class.java)
//            intent.putExtra("email" , account.email)
//            intent.putExtra("name" , account.displayName)
//            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), it.exception.toString(), LENGTH_SHORT).show()

        }
    }


}

fun TextView.disableCopyPaste() {
    isLongClickable = false
    setTextIsSelectable(false)
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
}
