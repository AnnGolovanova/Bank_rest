package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CardController {
    private final CardService cards;
    private final UserService users;

    // ADMIN
    @PostMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse create(@Valid @RequestBody CardCreateRequest req) {
        return cards.create(req);
    }

    @PatchMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse update(@PathVariable Long id, @Valid @RequestBody CardUpdateRequest req) {
        return cards.updateForAdmin(id, req);
    }

    @PatchMapping("/admin/cards/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public void block(@PathVariable Long id) { cards.block(id); }

    @PatchMapping("/admin/cards/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public void activate(@PathVariable Long id) { cards.activate(id); }

    @DeleteMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { cards.delete(id); }

    @GetMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponse> all(@ParameterObject CardFilter f, @ParameterObject Pageable p) {
        return cards.findAllCardsForAdmin(f, p == null ? PageRequest.of(0,10) : p);
    }

    // USER
    @GetMapping("/cards")
    public Page<CardResponse> myCards(@ParameterObject CardFilter f, @ParameterObject Pageable p) {
        var u = users.getCurrent();
        return cards.findMyCards(f, p == null ? PageRequest.of(0,10) : p, u.getId());
    }

    @GetMapping("/cards/{id}")
    public CardResponse myCard(@PathVariable Long id) {
        var u = users.getCurrent();
        return cards.getForOwner(id, u.getId());
    }

    @PatchMapping("/cards/{id}/request-block")
    public void requestBlock(@PathVariable Long id) {
        var u = users.getCurrent();
        cards.requestBlock(id, u.getId());
    }
}
