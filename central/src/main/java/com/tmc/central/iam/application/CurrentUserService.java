package com.tmc.central.iam.application;

import com.tmc.central.iam.api.AuthApi;
import com.tmc.central.iam.api.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** Cài đặt {@link AuthApi} từ JWT trong {@code SecurityContextHolder}. */
@Service
public class CurrentUserService implements AuthApi {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_EMPLOYEE_CODE = "employeeCode";

    @Override
    public CurrentUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("Không có người dùng đã đăng nhập");
        }
        return new CurrentUser(jwt.getSubject(), jwt.getClaimAsString(CLAIM_ROLE),
                jwt.getClaimAsString(CLAIM_EMPLOYEE_CODE));
    }

    @Override
    public String currentEmployeeCode() {
        CurrentUser user = currentUser();
        if (!CurrentUser.ROLE_EMPLOYEE.equals(user.role()) || user.employeeCode() == null) {
            throw new AccessDeniedException("Chỉ nhân viên mới có mã nhân viên");
        }
        return user.employeeCode();
    }
}
