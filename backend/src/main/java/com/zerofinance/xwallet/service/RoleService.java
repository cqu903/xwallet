package com.zerofinance.xwallet.service;

import com.zerofinance.xwallet.model.dto.RoleDTO;
import com.zerofinance.xwallet.model.entity.SysRole;
import com.zerofinance.xwallet.repository.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper sysRoleMapper;

    /**
     * 获取所有角色
     * @return 角色列表
     */
    public List<RoleDTO> getAllRoles() {
        log.info("获取所有角色");
        List<SysRole> roles = sysRoleMapper.selectAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    public RoleDTO getRoleById(Long id) {
        log.info("获取角色详情 - id: {}", id);
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            return null;
        }
        return convertToDTO(role);
    }

    /**
     * 转换为DTO
     */
    private RoleDTO convertToDTO(SysRole role) {
        return new RoleDTO(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus()
        );
    }
}
