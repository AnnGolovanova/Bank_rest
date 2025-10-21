package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByIdAndOwner_Id(Long id, Long ownerId);

    @Query("select c from Card c join fetch c.owner where c.id=:id")
    Optional<Card> findWithOwner(@Param("id") Long id);
}
