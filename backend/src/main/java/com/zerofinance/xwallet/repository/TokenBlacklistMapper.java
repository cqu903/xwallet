package com.zerofinance.xwallet.repository;

import com.zerofinance.xwallet.model.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Token黑名单Mapper接口
 */
@Mapper
public interface TokenBlacklistMapper {

    /**
     * 插入黑名单token
     * @param tokenBlacklist token黑名单实体
     * @return 影响行数
     */
    int insert(TokenBlacklist tokenBlacklist);

    /**
     * 检查token是否在黑名单中
     * @param token token字符串
     * @return token黑名单实体，如果不存在则返回null
     */
    TokenBlacklist findByToken(@Param("token") String token);

    /**
     * 清理过期的黑名单token
     * @return 删除的行数
     */
    int deleteExpiredTokens();
}
