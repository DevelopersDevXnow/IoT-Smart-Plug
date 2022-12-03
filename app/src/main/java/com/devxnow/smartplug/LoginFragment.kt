package com.devxnow.smartplug


import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.devxnow.smartplug.databinding.FragmentLoginBinding
import com.devxnow.smartplug.methods.Methods
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
import com.google.firebase.database.*
import com.tapadoo.alerter.Alerter
import com.tapadoo.alerter.OnHideAlertListener
import com.tapadoo.alerter.OnShowAlertListener
import es.dmoral.toasty.Toasty


class LoginFragment : Fragment() {


    companion object {
        private const val TAG = "LOGIN_TAG"
    }

    var methods: Methods? = null

    private var binding: FragmentLoginBinding? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mRef: DatabaseReference
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var email_id: EditText
    private lateinit var email: String
    private lateinit var password: String


    var mContext: Context? = null

    var callback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {

            binding?.btnSignIn?.dispose()
            showAppClosingDialog()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        );


    }


    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            // reload();
            activity?.let {
                val intent = Intent(it, HomeActivity::class.java)
                it.startActivity(intent)
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

// Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater)

        methods = Methods(requireContext())

        binding!!.etPassword.disableCopyPaste()

        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)




        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLoginBinding.bind(view)




        binding.btnSignIn.background =
            ActivityCompat.getDrawable(requireContext(), R.drawable.btn_background_ripple)

        //   Toast.makeText(requireContext(), "Google sign In", Toast.LENGTH_SHORT).show()

        //    getActivity()?.let { Alerter.create(it).setTitle("Google sign In").setText("text").show() };


        binding.rlSignInWithGoogle.setOnClickListener {
            // Do some work here


            //Check Internet Connection
            val checkInternet = CheckInternet()
            if (!checkInternet.isNetworkAvailable(requireContext())) {
                methods!!.vibrate(false)
                checkInternet.showNetworkDisconnectPopupDialog(requireActivity())
                return@setOnClickListener
            }


            signInGoogle()
        }

        binding.btnSignIn.setOnClickListener {

            binding.btnSignIn.startAnimation()

            email = binding.etEmail.text.toString().trim()
            password = binding.etPassword.text.toString().trim()

            //validate Email and Password
            if (!methods?.validateEmail(binding.etEmail)!! or !methods?.validatePassword(binding.etPassword)!!) {
                methods!!.vibrate(false)
                errorLoadingButton()
                return@setOnClickListener

            }


            //Check Internet Connection
            val checkInternet = CheckInternet()
            if (!checkInternet.isNetworkAvailable(requireContext())) {
                methods!!.vibrate(false)
                warningLoadingButton()
                checkInternet.showNetworkDisconnectPopupDialog(requireActivity())
                return@setOnClickListener
            }



            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = mAuth.currentUser
                        updateEmailLoginUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)

                        Alerter.create(requireActivity())
                            .setTitle("Authentication failed.")
                            .setText("Invalid email or password.")
                            .setDuration(2000)
                            .setIcon(R.drawable.ic_error_icon)
                            .setBackgroundColorRes(R.color.red_maroon)
                            //  .setIconColorFilter(0) // Optional - Removes white tint
                            .setOnShowListener(OnShowAlertListener {
                                Toasty.normal(
                                    requireContext(), "Please Login again!", Toasty.LENGTH_SHORT
                                ).show()
                            }).setOnHideListener(OnHideAlertListener {

                                errorLoadingButton()

                            })
                            .show()

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
                mAuth.sendPasswordResetEmail(email_id.text.toString())
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


            signInGoogle()
          //  findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)

        }


    }

    /*
    Loading Button Animations
     */
    private fun errorLoadingButton() {
        binding?.btnSignIn?.doneLoadingAnimation(
            ContextCompat.getColor(
                requireContext(),
                R.color.red_maroon
            ),
            methods!!.getBitmap(requireContext(), R.drawable.ic_error_outline_white_24)
        )

        Handler(Looper.getMainLooper()).postDelayed({

            //Revert the Loading button Animation.
            binding?.btnSignIn?.revertAnimation()


        }, 1000)
    }
    private fun warningLoadingButton() {
        binding?.btnSignIn?.doneLoadingAnimation(
            ContextCompat.getColor(
                requireContext(),
                R.color.dull_blue
            ),
            methods!!.getBitmap(requireContext(), R.drawable.ic_error_outline_white_24)
        )

        Handler(Looper.getMainLooper()).postDelayed({

            //Revert the Loading button Animation.
            binding?.btnSignIn?.revertAnimation()


        }, 1000)
    }
    private fun successLoadingButton() {
        binding?.btnSignIn?.doneLoadingAnimation(
            ContextCompat.getColor(
                requireContext(),
                R.color.green_a700
            ),
            methods!!.getBitmap(requireContext(), R.drawable.ic_check_24)
        )

//        Handler(Looper.getMainLooper()).postDelayed({
//
//            //Revert the Loading button Animation.
//            binding?.btnSignIn?.revertAnimation()
//
//
//        }, 1000)
    }

/*
Dialog Box
 */
    private fun showAppClosingDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Warning")
            .setMessage("Do you really want to close the app?")
            .setPositiveButton("Yes") { _, _ -> activity?.finish() }
            .setNegativeButton("No", null)
            .show()
    }

/*
Google SignIn
 */

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

                result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(requireView(), task)
            }

        }
    private fun handleResults(view: View, task: Task<GoogleSignInAccount>) {

        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {

                updateUI(view, account)

            } else {

                val activity = requireContext().applicationContext as Activity

                Toast.makeText(activity, task.exception.toString(), LENGTH_SHORT).show()

                //           Alerter.create(activity).setTitle("Alert Title").setText("Alert text...").show()


            }


        }


    }

/*
Update UI
 */
    private fun updateEmailLoginUI(user: FirebaseUser?) {

        successLoadingButton()

        Toasty.success(
            requireContext(),
            "User Login Successful",
            Toasty.LENGTH_LONG
        ).show()

        activity?.let {
            val intent = Intent(it, HomeActivity::class.java)
            it.startActivity(intent)
        }

    }
    private fun updateUI(v: View, account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        //Check weather User exists or not in database
        val checkUser: Query =
            FirebaseDatabase.getInstance().getReference("Users").orderByChild("email")
                .equalTo(account.email)
        checkUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //If Phone Number exists then get password
                if (dataSnapshot.exists()) {

                    //user details created in database redirect them to home page

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.putExtra("email", account.email)
                            intent.putExtra("name", account.displayName)
                            requireContext().startActivity(intent)
                        } else {
                            Toast.makeText(requireContext(), it.exception.toString(), LENGTH_SHORT)
                                .show()

                        }
                    }

                } else {
                    //No such user exist! or User Data not created in database redirect them to signup


                    val direction = LoginFragmentDirections.actionLoginFragmentToSignUpFragment(
                        account.displayName.toString(),
                        account.email.toString()
                    )
                    findNavController(v).navigate(direction)


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                Toasty.error(requireContext(), databaseError.message, Toasty.LENGTH_SHORT).show()
            }
        })


    }


    private fun TextView.disableCopyPaste() {
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

}





