package com.LoanManagement.WalletSystem.mapper;

import com.LoanManagement.WalletSystem.dto.Auth.AuthResponse;
import com.LoanManagement.WalletSystem.dto.Auth.UserResponse;
import com.LoanManagement.WalletSystem.model.User;

public interface UserMapper {
    UserResponse toUserResponse(User user);
}

