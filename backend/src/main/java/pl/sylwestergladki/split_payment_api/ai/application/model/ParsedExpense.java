package pl.sylwestergladki.split_payment_api.ai.application.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ParsedExpense(
        UUID groupId,
        String description,
        BigDecimal amount,
        String currency,
        String paidBy,
        List<String> participants
) {
}