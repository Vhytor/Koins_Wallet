package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.model.Role;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.model.Wallet;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import com.LoanManagement.WalletSystem.repository.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalArgumentException("Phone already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        User saved = userRepository.save(user);

        // create wallet automatically
        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        return saved;
    }
}

