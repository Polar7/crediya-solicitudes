package co.com.pragma.creditapplication.api.mapper;

import co.com.pragma.creditapplication.api.dto.CreateCreditApplicationDTO;
import co.com.pragma.creditapplication.model.creditapplication.CreditApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditApplicationMapper {


    @Mapping(source = "amountCredit", target = "amount")
    @Mapping(source = "termCredit", target = "term")
    @Mapping(target = "emailClient", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    CreditApplication toModel(CreateCreditApplicationDTO createCreditApplicationDTO);

}
