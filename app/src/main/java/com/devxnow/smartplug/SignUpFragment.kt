package com.devxnow.smartplug

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devxnow.smartplug.databinding.FragmentSignUpBinding
import com.devxnow.smartplug.methods.Methods
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import es.dmoral.toasty.Toasty
import java.util.*

class SignUpFragment : Fragment() {

    companion object {
        private const val TAG = "SIGNUP_TAG"
    }

    val args: SignUpFragmentArgs by navArgs()


    var methods: Methods? = null

    private var binding: FragmentSignUpBinding? = null
    private lateinit var mAuth: FirebaseAuth

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

// Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        methods = Methods(requireContext())

        mAuth = FirebaseAuth.getInstance()



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSignUpBinding.bind(view)

        //Fetch Safe args
        val userName = args.userName
        val userEmail = args.userEmail

        binding.etFullName.setText(userName)
        binding.etEmail.setText(userEmail)


        binding.btnSignUp.background =
            ActivityCompat.getDrawable(requireContext(), R.drawable.btn_background_ripple)

        //Sign Up Button  Clicked

        binding.btnSignUp.setOnClickListener {

            if (!methods!!.validateUserName(binding.etFullName) || !methods!!.validatePassword(binding.etPassword) || !methods!!.validateConfirmPassword(binding.etConfirmPassword)) {

                return@setOnClickListener
            }
            val mEmail: String = binding.etEmail.text.toString().trim { it <= ' ' }
            val mPassword: String = binding.etPassword.text.toString().trim { it <= ' ' }
            val mName: String =binding.etFullName.text.toString().trim { it <= ' ' }
            val mUsername: String= ""


            register(mEmail, mPassword, mName, mUsername)

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

        //Sign In text Button Click
        binding.tvSignIn.setOnClickListener {

            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }


    }


    private fun register(mEmail: String, mPassword: String, mName: String, mUsername: String) {
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
            .addOnCompleteListener(OnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val userId =
                        Objects.requireNonNull<FirebaseUser>(mAuth.getCurrentUser()).uid
                    val hashMap =
                        HashMap<String, Any>()
                    hashMap["id"] = userId
                    hashMap["name"] = mName
                    hashMap["email"] = mEmail
                    hashMap["username"] = mUsername
                    hashMap["bio"] = ""
                    hashMap["verified"] = ""
                    hashMap["location"] = ""
                    hashMap["phone"] = ""
                    hashMap["status"] = "" + System.currentTimeMillis()
                    hashMap["typingTo"] = "noOne"
                    hashMap["link"] = ""
                    hashMap["photo"] = ""
                    FirebaseDatabase.getInstance().getReference("Users").child(userId)
                        .setValue(hashMap)
                        .addOnCompleteListener { task1: Task<Void?> ->
                            if (task1.isSuccessful) {
                                val intent = Intent(requireContext(), HomeActivity::class.java)
//                                intent.putExtra("email", account.email)
//                                intent.putExtra("name", account.displayName)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                requireContext().startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                } else {
                    val msg =
                        Objects.requireNonNull(task.exception)?.message
                    if (msg != null) {
                        Toasty.error(requireContext(), msg, Toasty.LENGTH_LONG).show()
                    }

                }
            })
    }


}