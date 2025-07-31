package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.request.RequestBlock;
import com.example.bankcards.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestBlockRepository extends JpaRepository<RequestBlock, Long> {

    Page<RequestBlock> findByUser(User user, Pageable pageable);
    boolean existsByUserAndCard(User user, Card card);

}
