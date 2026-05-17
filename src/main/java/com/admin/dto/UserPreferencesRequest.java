package com.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserPreferencesRequest {
    @NotBlank
    @Pattern(regexp = "light|dark")
    private String colorMode;

    @NotBlank
    private String preset;

    @Min(0)
    @Max(1)
    private Double borderRadius;
}
