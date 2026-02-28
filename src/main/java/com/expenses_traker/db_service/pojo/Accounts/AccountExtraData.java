package com.expenses_traker.db_service.pojo.Accounts;

import com.expenses_traker.db_service.Entity.CreditCardEntity;
import com.expenses_traker.db_service.Entity.EMIAccountEntity;
import com.expenses_traker.db_service.Entity.FriendAccountEntity;
import com.expenses_traker.db_service.Entity.InvestmentAccountEntity;
import lombok.Data;

@Data
public class AccountExtraData {
    private CreditCardEntity creditCard;
    private EMIAccountEntity emi;
    private InvestmentAccountEntity investment;
    private FriendAccountEntity friend;
}
