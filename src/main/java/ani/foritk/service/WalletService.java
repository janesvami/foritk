package ani.foritk.service;

import ani.foritk.dto.OperationType;
import ani.foritk.dto.UpdateWalletDto;
import ani.foritk.entity.Wallet;
import ani.foritk.exception.InsufficientFundsException;
import ani.foritk.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet with ID " + id + " is not found"));

    }

    @Transactional
    public Wallet updateBalance(UpdateWalletDto updateWalletDto) {
        final UUID id = updateWalletDto.valletId();
        final Wallet walletToUpdate = walletRepository.findByIdAndLock(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet with ID " + id + " is not found"));

        final OperationType operationType = updateWalletDto.operationType();
        final BigDecimal currentBalance = walletToUpdate.getBalance();
        final BigDecimal amount = updateWalletDto.amount();

        switch (operationType) {
            case DEPOSIT -> {
                final BigDecimal newBalance = currentBalance.add(amount);
                walletToUpdate.setBalance(newBalance);
            }
            case WITHDRAW -> {
                if(currentBalance.compareTo(amount) < 0){
                   throw new InsufficientFundsException("Wallet with valletId " + id + " cannot withdraw " + amount);
                }
                final BigDecimal newBalance = currentBalance.subtract(amount);
                walletToUpdate.setBalance(newBalance);
            }
        }

        return walletRepository.save(walletToUpdate);
    }
}
