package ani.foritk.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record GetWalletDto(
        @NotNull
        UUID valletId,

        @NotNull
        BigDecimal balance
) {}