package aded.first_web_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import aded.first_web_api.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {}
