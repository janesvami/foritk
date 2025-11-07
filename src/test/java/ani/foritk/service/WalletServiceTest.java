package ani.foritk.service;

import ani.foritk.dto.OperationType;
import ani.foritk.dto.UpdateWalletDto;
import ani.foritk.entity.Wallet;
import ani.foritk.exception.InsufficientFundsException;
import ani.foritk.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletServiceTest {

    private final WalletRepository walletRepository = mock();
    private final WalletService walletService = new WalletService(walletRepository);

    @Test
    void getWallet_WhenWalletIsNotFound_ThenThrowEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(walletRepository.findById(any())).thenReturn(Optional.empty());
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> walletService.getWallet(id)
        );
        String expectedMessage = "Wallet with ID " + id + " is not found";
        assertEquals(expectedMessage, entityNotFoundException.getMessage());
    }

    @Test
    void getWallet_WhenWalletIsFound_ThenReturnsWallet() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setBalance(new BigDecimal(1));
        when(walletRepository.findById(any())).thenReturn(Optional.of(wallet));
        Wallet result = walletService.getWallet(id);

        Wallet expectedWallet = new Wallet();
        expectedWallet.setId(id);
        expectedWallet.setBalance(new BigDecimal(1));
        assertEquals(expectedWallet, result);
    }

    @Test
    void updateBalance_WhenWalletIsNotFound_ThenThrowEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        UpdateWalletDto updateWalletDto = new UpdateWalletDto(id, OperationType.DEPOSIT, new BigDecimal(1));

        when(walletRepository.findById(any())).thenReturn(Optional.empty());
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> walletService.updateBalance(updateWalletDto)
        );
        String expectedMessage = "Wallet with ID " + id + " is not found";
        assertEquals(expectedMessage, entityNotFoundException.getMessage());
    }

    @Test
    void updateBalance_WhenBalanceLessThanWithdrawAmount_ThenThrowInsufficientFundsException() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setBalance(new BigDecimal(500));
        BigDecimal withdrawAmount = new BigDecimal(600);
        UpdateWalletDto updateWalletDto = new UpdateWalletDto(id, OperationType.WITHDRAW, withdrawAmount);

        when(walletRepository.findByIdAndLock(any())).thenReturn(Optional.of(wallet));
        InsufficientFundsException insufficientFundsException = assertThrows(
                InsufficientFundsException.class,
                () -> walletService.updateBalance(updateWalletDto)
        );
        String expectedMessage = "Wallet with valletId " + id + " cannot withdraw " + withdrawAmount;
        assertEquals(expectedMessage, insufficientFundsException.getMessage());
    }

    @Test
    void updateBalance_WhenOperationWithdrawIsSuccessful_ThenReduceWalletBalance() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setBalance(new BigDecimal("50.3"));
        BigDecimal withdrawAmount = new BigDecimal("15.6");
        UpdateWalletDto updateWalletDto = new UpdateWalletDto(id, OperationType.WITHDRAW, withdrawAmount);

        when(walletRepository.findByIdAndLock(any())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(new Wallet());
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        walletService.updateBalance(updateWalletDto);
        verify(walletRepository).save(captor.capture());
        Wallet result = captor.getValue();

        Wallet expectedWallet = new Wallet();
        expectedWallet.setId(id);
        expectedWallet.setBalance(new BigDecimal("34.7"));
        assertEquals(expectedWallet, result);
    }

    @Test
    void updateBalance_WhenOperationDepositIsSuccessful_ThenReduceWalletBalance() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setBalance(new BigDecimal("50.3"));
        BigDecimal withdrawAmount = new BigDecimal("15.6");
        UpdateWalletDto updateWalletDto = new UpdateWalletDto(id, OperationType.DEPOSIT, withdrawAmount);

        when(walletRepository.findByIdAndLock(any())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(new Wallet());
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        walletService.updateBalance(updateWalletDto);
        verify(walletRepository).save(captor.capture());
        Wallet result = captor.getValue();

        Wallet expectedWallet = new Wallet();
        expectedWallet.setId(id);
        expectedWallet.setBalance(new BigDecimal("65.9"));
        assertEquals(expectedWallet, result);
    }
}