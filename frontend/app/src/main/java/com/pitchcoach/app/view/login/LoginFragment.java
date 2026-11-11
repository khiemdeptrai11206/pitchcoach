package com.pitchcoach.app.view.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.AuthController;
import com.pitchcoach.app.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private final AuthController controller = new AuthController();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthController.AuthCallback callback = new AuthController.AuthCallback() {
            @Override
            public void onLoginSuccess() {
                goToHome();
            }

            @Override
            public void onLogoutSuccess() {
            }
        };

        binding.googleButton.setOnClickListener(v -> controller.loginWithGoogle(callback));
        binding.emailButton.setOnClickListener(v -> controller.loginWithEmail(callback));
    }

    private void goToHome() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_login_to_home);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
