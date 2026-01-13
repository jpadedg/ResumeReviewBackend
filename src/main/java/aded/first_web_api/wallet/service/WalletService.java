package aded.first_web_api.wallet.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import aded.first_web_api.wallet.model.Wallet;
import aded.first_web_api.wallet.model.WalletTransaction;
import aded.first_web_api.wallet.repository.WalletAtomicRepository;
import aded.first_web_api.wallet.repository.WalletRepository;
import aded.first_web_api.wallet.repository.WalletTransactionRepository;

@Service
public class WalletService {

    public static final int SIGNUP_BONUS = 100;

    private final WalletRepository walletRepository;
    private final WalletAtomicRepository walletAtomicRepository;
    private final WalletTransactionRepository txRepository;

    public WalletService(WalletRepository walletRepository,
                         WalletAtomicRepository walletAtomicRepository,
                         WalletTransactionRepository txRepository) {
        this.walletRepository = walletRepository;
        this.walletAtomicRepository = walletAtomicRepository;
        this.txRepository = txRepository;
    }

    @Transactional
    public void createWalletWithSignupBonus(Long userId) {
        if (walletRepository.existsById(userId)) return;

        walletRepository.save(new Wallet(userId, SIGNUP_BONUS));
        txRepository.save(new WalletTransaction(userId, "SIGNUP_BONUS", SIGNUP_BONUS, null));
    }

    public int getBalanceOrThrow(Long userId) {
        return walletRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet não encontrada"))
                .getBalance();
    }

    @Transactional
    public void debitOrThrow(Long userId, int cost, UUID referenceId) {
        int updated = walletAtomicRepository.debitIfEnough(userId, cost);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Saldo insuficiente.");
        }
        txRepository.save(new WalletTransaction(userId, "RESUME_REVIEW_DEBIT", -cost, referenceId));
    }

    @Transactional
    public void refund(Long userId, int amount, UUID referenceId) {
        walletAtomicRepository.credit(userId, amount);
        txRepository.save(new WalletTransaction(userId, "REFUND", amount, referenceId));
    }
}
