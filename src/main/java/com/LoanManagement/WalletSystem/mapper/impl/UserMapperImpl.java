package com.LoanManagement.WalletSystem.mapper.impl;

import com.LoanManagement.WalletSystem.dto.Auth.UserResponse;
import com.LoanManagement.WalletSystem.mapper.UserMapper;
import com.LoanManagement.WalletSystem.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }
}

