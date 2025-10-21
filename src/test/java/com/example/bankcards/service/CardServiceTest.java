package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardFilter;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardUpdateRequest;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    CardRepository cards;
    UserRepository users;
    CardService service;

    @BeforeEach
    void setUp() {
        cards = mock(CardRepository.class);
        users = mock(UserRepository.class);
        service = new CardService(cards, users);
    }


    private static User user(long id, String name) {
        User u = new User();
        u.setId(id);
        u.setUsername(name);
        return u;
    }

    private static Card card(long id, User owner, String number,
                             String holder, LocalDate exp, CardStatus st, BigDecimal bal) {
        Card c = Card.builder()
                .id(id)
                .owner(owner)
                .cardNumberEnc(number)
                .holderName(holder)
                .expiry(exp)
                .status(st)
                .balance(bal)
                .build();
        return c;
    }

    // ---------- tests ----------

    @Test
    @DisplayName("create: находит owner, сохраняет карту и возвращает DTO")
    void create_ok() {
        var owner = user(42L, "user1");
        when(users.findById(42L)).thenReturn(Optional.of(owner));

        var req = new CardCreateRequest(
                "4111 1111 1111 1234",
                "User One",
                LocalDate.of(2030, 12, 31),
                new BigDecimal("1000.00"),
                42L // ownerId
        );

        when(cards.save(any(Card.class))).thenAnswer(inv -> {
            Card saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        CardResponse resp = service.create(req);


        assertNotNull(resp);
        assertEquals(10L, resp.id());
        assertEquals("User One", resp.holderName());
        assertTrue(resp.maskedNumber().endsWith("1234"));


        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cards).save(captor.capture());
        Card toSave = captor.getValue();
        assertEquals(owner, toSave.getOwner());
        assertEquals(CardStatus.ACTIVE, toSave.getStatus());
        assertEquals(new BigDecimal("1000.00"), toSave.getBalance());

        verify(users).findById(42L);
        verifyNoMoreInteractions(cards, users);
    }

    @Test
    @DisplayName("create: если owner не найден — orElseThrow бросит NoSuchElementException")
    void create_ownerNotFound_throws() {
        when(users.findById(777L)).thenReturn(Optional.empty());
        var req = new CardCreateRequest("4111", "X",
                LocalDate.now().plusYears(1), new BigDecimal("10.00"), 777L);
        assertThrows(NoSuchElementException.class, () -> service.create(req));
        verify(users).findById(777L);
        verifyNoInteractions(cards);
    }

    @Test
    @DisplayName("getForOwner: ищет по id и ownerId, возвращает DTO")
    void getForOwner_ok() {
        var owner = user(2L, "u");
        var c = card(100L, owner, "4111 1111 1111 9999", "U",
                LocalDate.of(2030,1,1), CardStatus.ACTIVE, new BigDecimal("5.00"));
        when(cards.findByIdAndOwner_Id(100L, 2L)).thenReturn(Optional.of(c));

        CardResponse resp = service.getForOwner(100L, 2L);
        assertEquals(100L, resp.id());
        assertTrue(resp.maskedNumber().endsWith("9999"));

        verify(cards).findByIdAndOwner_Id(100L, 2L);
    }

    @Test
    @DisplayName("findMyCards: собирает спецификацию, делегирует в репозиторий и мапит в DTO")
    void findMyCards_ok() {
        var ownerId = 7L;
        var f = new CardFilter(CardStatus.ACTIVE, LocalDate.of(2031,1,1), "user");
        var pageReq = PageRequest.of(0, 2, Sort.by("id"));

        var owner = user(ownerId, "u");
        var c1 = card(1L, owner, "4111 1111 1111 1111", "user one",
                LocalDate.of(2030,1,1), CardStatus.ACTIVE, new BigDecimal("1.00"));
        var c2 = card(2L, owner, "4111 1111 1111 2222", "user two",
                LocalDate.of(2030,6,1), CardStatus.ACTIVE, new BigDecimal("2.00"));

        when(cards.findAll(any(Specification.class), eq(pageReq)))
                .thenReturn(new PageImpl<>(List.of(c1, c2), pageReq, 2));

        Page<CardResponse> page = service.findMyCards(f, pageReq, ownerId);

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        assertEquals(1L, page.getContent().get(0).id());
        assertTrue(page.getContent().get(0).maskedNumber().endsWith("1111"));

        verify(cards).findAll(any(Specification.class), eq(pageReq));
        verifyNoMoreInteractions(cards);
    }

    @Test
    @DisplayName("findAllCardsForAdmin: делегирует в репозиторий с построенной спецификацией")
    void findAllCardsForAdmin_ok() {
        var f = new CardFilter(null, null, null);
        var pageReq = PageRequest.of(1, 5);
        var c = card(5L, user(1L,"a"), "4111 1111 1111 4444", "A",
                LocalDate.of(2029,1,1), CardStatus.BLOCKED, new BigDecimal("3.00"));

        when(cards.findAll(any(Specification.class), eq(pageReq)))
                .thenReturn(new PageImpl<>(List.of(c), pageReq, 1));

        Page<CardResponse> page = service.findAllCardsForAdmin(f, pageReq);

        assertEquals(6, page.getTotalElements());
        assertEquals(5L, page.getContent().get(0).id());

        verify(cards).findAll(any(Specification.class), eq(pageReq));
    }

    @Test
    @DisplayName("block: выставляет статус BLOCKED через findById")
    void block_ok() {
        var c = card(10L, user(1L,"u"), "4111", "U",
                LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("0"));
        when(cards.findById(10L)).thenReturn(Optional.of(c));

        service.block(10L);

        assertEquals(CardStatus.BLOCKED, c.getStatus());
        verify(cards).findById(10L);
        verifyNoMoreInteractions(cards);
    }

    @Test
    @DisplayName("activate: выставляет статус ACTIVE")
    void activate_ok() {
        var c = card(11L, user(1L,"u"), "4111", "U",
                LocalDate.now().plusYears(1), CardStatus.BLOCKED, new BigDecimal("0"));
        when(cards.findById(11L)).thenReturn(Optional.of(c));

        service.activate(11L);

        assertEquals(CardStatus.ACTIVE, c.getStatus());
        verify(cards).findById(11L);
    }

    @Test
    @DisplayName("delete: вызывает deleteById")
    void delete_ok() {
        service.delete(77L);
        verify(cards).deleteById(77L);
        verifyNoMoreInteractions(cards);
    }

    @Test
    @DisplayName("requestBlock: находит карту владельца и ставит BLOCKED")
    void requestBlock_ok() {
        var owner = user(3L, "u");
        var c = card(55L, owner, "4111", "U",
                LocalDate.now().plusYears(1), CardStatus.ACTIVE, new BigDecimal("0"));
        when(cards.findByIdAndOwner_Id(55L, 3L)).thenReturn(Optional.of(c));

        service.requestBlock(55L, 3L);

        assertEquals(CardStatus.BLOCKED, c.getStatus());
        verify(cards).findByIdAndOwner_Id(55L, 3L);
    }

    @Test
    @DisplayName("updateForAdmin: меняет только заданные поля и возвращает DTO")
    void updateForAdmin_ok() {
        var c = card(9L, user(1L,"a"), "4111 1111 1111 3333", "Old",
                LocalDate.of(2030,1,1), CardStatus.ACTIVE, new BigDecimal("10.00"));
        when(cards.findById(9L)).thenReturn(Optional.of(c));

        var req = new CardUpdateRequest("New Holder",
                LocalDate.of(2031, 3, 15),
                CardStatus.BLOCKED);

        CardResponse resp = service.updateForAdmin(9L, req);

        assertEquals("New Holder", c.getHolderName());
        assertEquals(LocalDate.of(2031,3,15), c.getExpiry());
        assertEquals(CardStatus.BLOCKED, c.getStatus());

        assertEquals(9L, resp.id());
        assertEquals("New Holder", resp.holderName());

        verify(cards).findById(9L);
    }

}