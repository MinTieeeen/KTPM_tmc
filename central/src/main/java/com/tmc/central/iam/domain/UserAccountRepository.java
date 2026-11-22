package com.tmc.central.iam.domain;

import java.util.Optional;

/** Kho tài khoản (cài đặt: JpaUserAccountRepository). */
public interface UserAccountRepository {

    Optional<UserAccount> findByUsername(String username);
}
