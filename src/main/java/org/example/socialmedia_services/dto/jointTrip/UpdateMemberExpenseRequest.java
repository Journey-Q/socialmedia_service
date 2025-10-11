package org.example.socialmedia_services.dto.jointTrip;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateMemberExpenseRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Double amount;
}