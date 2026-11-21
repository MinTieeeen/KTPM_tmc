package com.tmc.central.iam.infrastructure;

import com.tmc.central.iam.domain.UserAccount;
import com.tmc.central.iam.domain.UserAccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data cài đặt port {@link UserAccountRepository} (DIP). */
interface JpaUserAccountRepository extends JpaRepository<UserAccount, String>, UserAccountRepository {
}
