package com.example.bankcards.entity.requestBlock;


import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "request_block")
public class RequestBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestBlockStatus status = RequestBlockStatus.NOT_STARTED;

    private LocalDateTime requestedAt = LocalDateTime.now();

}
