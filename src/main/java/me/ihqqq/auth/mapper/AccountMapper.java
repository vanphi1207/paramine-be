package me.ihqqq.auth.mapper;

import me.ihqqq.auth.dto.response.AccountResponse;
import me.ihqqq.auth.entity.NLoginAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "username", source = "lastName")
    AccountResponse toAccountResponse(NLoginAccount account);
}