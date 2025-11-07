package ani.foritk.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateWalletDto(
        @NotNull
        UUID valletId,

        @NotNull
        OperationType operationType,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {}