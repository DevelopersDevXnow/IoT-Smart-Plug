package com.devxnow.smartplug

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devxnow.smartplug.databinding.FragmentLoginBinding
import com.devxnow.smartplug.databinding.FragmentSignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    companion object {
        private const val TAG = "SIGNUP_TAG"
    }

    private var binding: FragmentSignUpBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

// Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSignUpBinding.bind(view)




    }


}