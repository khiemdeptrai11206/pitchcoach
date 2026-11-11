package com.pitchcoach.app.view.login;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.AuthController;
import com.pitchcoach.app.databinding.FragmentLoginBinding;
import com.pitchcoach.app.view.util.UiUtils;

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
                if (isAdded()) {
                    goToHome();
                }
            }

            @Override
            public void onLogoutSuccess() {
            }

            @Override
            public void onError(String message) {
                if (isAdded() && binding != null) {
                    UiUtils.showSnackbar(binding.getRoot(), message);
                }
            }
        };

        binding.googleButton.setOnClickListener(v ->
                controller.loginWithGoogle(requireContext(), callback));
        binding.emailButton.setOnClickListener(v ->
                showEmailLoginDialog(callback));
    }

    private void showEmailLoginDialog(AuthController.AuthCallback callback) {
        int padH = UiUtils.dp(requireContext(), 20);
        int padV = UiUtils.dp(requireContext(), 8);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padH, padV, padH, 0);

        EditText emailInput = new EditText(requireContext());
        emailInput.setHint("Email (VD: khoa@gmail.com)");
        emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        container.addView(emailInput);

        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint("Mật khẩu (tối thiểu 6 ký tự)");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(passwordInput);

        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_PitchCoach_Dialog)
                .setTitle(R.string.use_email)
                .setView(container)
                .setPositiveButton("Đăng nhập / Đăng ký", (dialog, which) -> {
                    String email = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    controller.loginOrRegisterWithEmail(requireContext(), email, password, callback);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
