package com.finance.backend.service;

import com.finance.backend.dto.request.UpdateRoleRequest;
import com.finance.backend.dto.request.UpdateStatusRequest;
import com.finance.backend.dto.response.PagedUsersResponse;
import com.finance.backend.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

/**
 * Administrative user management operations (admin-only at the API layer).
 */
public interface UserService {

    /**
     * Returns a page of all users in the system.
     */
    PagedUsersResponse getAllUsers(Pageable pageable);

    /**
     * Loads a single user by primary key.
     */
    UserResponse getUserById(Long id);

    /**
     * Updates the user's role.
     */
    UserResponse updateRole(Long id, UpdateRoleRequest request);

    /**
     * Updates the user's active/inactive status.
     */
    UserResponse updateStatus(Long id, UpdateStatusRequest request);

    /**
     * Permanently removes the user from the database.
     */
    void deleteUser(Long id);
}
