package ani.foritk.controller;

import ani.foritk.dto.GetWalletDto;
import ani.foritk.dto.UpdateWalletDto;
import ani.foritk.entity.Wallet;
import ani.foritk.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(
        name = "Wallet controllers",
        description = "Controllers for all operations with wallets"
)
@RequestMapping("/api/v1")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/wallets/{WALLET_UUID}")
    @Operation(summary = "Get wallet by ID",
            description = """
                    This operation returns the wallet for the given ID.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation is successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "404", description = "Wallet is not found",
                            content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "500", description = "Server error",
                            content = @Content(schema = @Schema()))
            })
    public GetWalletDto getById(@PathVariable UUID WALLET_UUID) {
        final Wallet wallet = walletService.getWallet(WALLET_UUID);
        return constructWalletDto(wallet);
    }


    @PostMapping("/wallet")
    @Operation(summary = "Update wallet balance",
            description = """
                    This operation updates the balance for the specified identifier:
                    replenishes the balance or writes off funds.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Balance is updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid input",
                            content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "500", description = "Server error",
                            content = @Content(schema = @Schema()))
            })
    public GetWalletDto updateWallet(@Valid @RequestBody UpdateWalletDto updateWalletDto) {
        final Wallet wallet = walletService.updateBalance(updateWalletDto);
        return constructWalletDto(wallet);
    }

    private GetWalletDto constructWalletDto(Wallet wallet) {
        return new GetWalletDto(
                wallet.getId(),
                wallet.getBalance()
        );
    }
}