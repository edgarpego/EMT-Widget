package es.tamarit.widgetemt.services.cardbalance;

import java.io.IOException;

public interface CardBalanceService {
    
    public String findByCardNumber(String cardNumber) throws IOException;
    
}
