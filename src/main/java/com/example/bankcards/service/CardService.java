package com.example.bankcards.service;

        import com.example.bankcards.entity.*;
        import com.example.bankcards.dto.*;
        import com.example.bankcards.repository.CardRepository;
        import com.example.bankcards.repository.UserRepository;
        import jakarta.persistence.criteria.Predicate;
        import lombok.RequiredArgsConstructor;
        import org.springframework.data.domain.*;
        import org.springframework.data.jpa.domain.Specification;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.*;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cards;
    private final UserRepository users;

    @Transactional
    public CardResponse create(CardCreateRequest req) {
        User owner = users.findById(req.ownerId()).orElseThrow();
        Card c = Card.builder()
                .cardNumberEnc(req.cardNumber())
                .holderName(req.holderName())
                .expiry(req.expiry())
                .status(CardStatus.ACTIVE)
                .balance(req.initialBalance())
                .owner(owner)
                .build();
        return toDto(cards.save(c));
    }

    public CardResponse getForOwner(Long id, Long ownerId) {
        Card c = cards.findByIdAndOwner_Id(id, ownerId).orElseThrow();
        return toDto(c);
    }

    public Page<CardResponse> findMyCards(CardFilter f, Pageable p, Long ownerId) {
        Specification<Card> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("owner").get("id"), ownerId));
            if (f != null) {
                if (f.status() != null) ps.add(cb.equal(root.get("status"), f.status()));
                if (f.expiryBefore() != null) ps.add(cb.lessThan(root.get("expiry"), f.expiryBefore()));
                if (f.holderNameLike()!=null && !f.holderNameLike().isBlank())
                    ps.add(cb.like(cb.lower(root.get("holderName")), "%"+f.holderNameLike().toLowerCase()+"%"));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return cards.findAll(spec, p).map(this::toDto);
    }

    public Page<CardResponse> findAllCardsForAdmin(CardFilter f, Pageable p) {
        Specification<Card> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (f != null) {
                if (f.status() != null) ps.add(cb.equal(root.get("status"), f.status()));
                if (f.expiryBefore() != null) ps.add(cb.lessThan(root.get("expiry"), f.expiryBefore()));
                if (f.holderNameLike()!=null && !f.holderNameLike().isBlank())
                    ps.add(cb.like(cb.lower(root.get("holderName")), "%"+f.holderNameLike().toLowerCase()+"%"));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return cards.findAll(spec, p).map(this::toDto);
    }

    @Transactional
    public void block(Long id){
        setStatus(id, CardStatus.BLOCKED);
    }
    @Transactional
    public void activate(Long id){
        setStatus(id, CardStatus.ACTIVE);
    }
    @Transactional
    public void delete(Long id){
        cards.deleteById(id);
    }

    @Transactional
    public void requestBlock(Long id, Long ownerId){
        Card c = cards.findByIdAndOwner_Id(id, ownerId).orElseThrow();
        c.setStatus(CardStatus.BLOCKED);
    }

    @Transactional
    public CardResponse updateForAdmin(Long id, CardUpdateRequest req) {
        Card c = cards.findById(id).orElseThrow();
        if (req.holderName()!=null) c.setHolderName(req.holderName());
        if (req.expiry()!=null) c.setExpiry(req.expiry());
        if (req.status()!=null) c.setStatus(req.status());
        return toDto(c);
    }

    private void setStatus(Long id, CardStatus st) {
        Card c = cards.findById(id).orElseThrow();
        c.setStatus(st);
    }

    private CardResponse toDto(Card c){
        return new CardResponse(
                c.getId(), c.maskedNumber(), c.getHolderName(), c.getExpiry(),
                c.getStatus(), c.getBalance()
        );
    }
}
