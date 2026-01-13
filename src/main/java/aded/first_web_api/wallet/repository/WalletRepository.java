package aded.first_web_api.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aded.first_web_api.wallet.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {}
