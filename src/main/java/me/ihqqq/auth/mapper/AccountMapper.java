package me.ihqqq.auth.mapper;

import me.ihqqq.auth.dto.response.AccountResponse;
import me.ihqqq.auth.entity.AccountMeta;
import me.ihqqq.auth.entity.NLoginAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "username", source = "account.lastName")
    @Mapping(target = "uniqueId", source = "account.uniqueId")
    @Mapping(target = "premium", source = "account.premium")
    @Mapping(target = "role", source = "meta.role")
    AccountResponse toAccountResponse(NLoginAccount account, AccountMeta meta);
}