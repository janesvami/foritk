package ani.foritk.controller;

import ani.foritk.dto.OperationType;
import ani.foritk.dto.UpdateWalletDto;
import ani.foritk.entity.Wallet;
import ani.foritk.exception.InsufficientFundsException;
import ani.foritk.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    @Test
    void getById_WhenFound_ThenSuccess() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100.00"));

        when(walletService.getWallet(walletId)).thenReturn(wallet);

        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valletId").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void getById_WhenNotFound_Then404() throws Exception {
        String errorMessage = "Not Found";
        when(walletService.getWallet(any())).thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(get("/api/v1/wallets/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    void updateBalance_WhenWrongEnum_ThenBadRequest() throws Exception {
        String badJson = String.format("""
                {
                    "valletId": "%s",
                    "operationType": "INVALID_TYPE",
                    "amount": 100
                }
                """, UUID.randomUUID());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("INVALID_TYPE")));
    }

    @Test
    void updateBalance_WhenInvalidValues_ThenBadRequest() throws Exception {
        String badJson = """
                {
                    "valletId": null,
                    "operationType": "DEPOSIT",
                    "amount": -200
                }
                """;

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("valletId")))
                .andExpect(jsonPath("$.message", containsString("amount")));
    }

    @Test
    void updateWallet_WhenValidValues_ThenOk() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(amount);
        UpdateWalletDto request = new UpdateWalletDto(walletId, OperationType.DEPOSIT, amount);

        when(walletService.updateBalance(any())).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void updateWallet_WhenInsufficientFundsException_ThenBadRequest() throws Exception {
        UpdateWalletDto request = new UpdateWalletDto(
                UUID.randomUUID(),
                OperationType.WITHDRAW,
                new BigDecimal("500.00")
        );

        String errorMessage = "Not enough money";
        when(walletService.updateBalance(any())).thenThrow(new InsufficientFundsException(errorMessage));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

}