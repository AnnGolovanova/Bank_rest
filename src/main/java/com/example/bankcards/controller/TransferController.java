package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transfers;
    private final UserService users;

    @PostMapping
    public void transfer(@Valid @RequestBody TransferRequest req) {
        var u = users.getCurrent();
        transfers.transfer(u.getId(), req.fromCardId(), req.toCardId(), req.amount());
    }
}
