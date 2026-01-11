package aded.first_web_api.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import aded.first_web_api.model.Wallet;

public interface WalletAtomicRepository extends JpaRepository<Wallet, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE wallets SET balance = balance - :cost, updated_at = now() " +
                   "WHERE user_id = :userId AND balance >= :cost", nativeQuery = true)
    int debitIfEnough(@Param("userId") Long userId, @Param("cost") int cost);

    @Modifying
    @Transactional
    @Query(value = "UPDATE wallets SET balance = balance + :amount, updated_at = now() " +
                   "WHERE user_id = :userId", nativeQuery = true)
    int credit(@Param("userId") Long userId, @Param("amount") int amount);
}
