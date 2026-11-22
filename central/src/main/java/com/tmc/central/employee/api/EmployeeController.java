package com.tmc.central.employee.api;

import com.tmc.central.employee.application.IssueQrCredentialUseCase;
import com.tmc.central.employee.application.ListEmployeesQuery;
import com.tmc.central.employee.application.RevokeQrCredentialUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API quản trị nhân viên và credential QR (vai trò ADMIN – D2: ADMIN gộp quyền HR). */
@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final ListEmployeesQuery listEmployees;
    private final IssueQrCredentialUseCase issueCredential;
    private final RevokeQrCredentialUseCase revokeCredential;

    public EmployeeController(ListEmployeesQuery listEmployees, IssueQrCredentialUseCase issueCredential,
                              RevokeQrCredentialUseCase revokeCredential) {
        this.listEmployees = listEmployees;
        this.issueCredential = issueCredential;
        this.revokeCredential = revokeCredential;
    }

    @GetMapping
    public List<EmployeeView> list(@RequestParam(required = false) String department,
                                   @RequestParam(required = false) @Size(max = 100) String q) {
        return listEmployees.list(department, q);
    }

    @PostMapping("/{code}/qr/issue")
    public EmployeeView issue(@PathVariable String code) {
        issueCredential.issue(code);
        return listEmployees.view(code);
    }

    @PostMapping("/{code}/qr/revoke")
    public EmployeeView revoke(@PathVariable String code, @Valid @RequestBody RevokeRequest request) {
        revokeCredential.revoke(code, request.reason());
        return listEmployees.view(code);
    }
}
