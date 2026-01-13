package aded.first_web_api.wallet.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import aded.first_web_api.wallet.model.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {}
