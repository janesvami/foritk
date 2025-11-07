package ani.foritk.controller;

import ani.foritk.dto.GetWalletDto;
import ani.foritk.entity.Wallet;
import ani.foritk.service.WalletService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletControllerTest {

    private final WalletService walletService = mock(WalletService.class);
    private final WalletController walletController = new WalletController(walletService);

    @Test
    void getWallet_WhenValidId_ThenReturnsWallet() {
        UUID id = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("100.00");
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setBalance(balance);

        when(walletService.getWallet(id)).thenReturn(wallet);

        GetWalletDto result = walletController.getById(id);
        assertNotNull(result);
        assertEquals(wallet.getId(), result.valletId());
        assertEquals(wallet.getBalance(), result.balance());
    }



}