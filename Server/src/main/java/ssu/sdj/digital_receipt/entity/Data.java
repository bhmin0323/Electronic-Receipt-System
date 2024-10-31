package ssu.sdj.digital_receipt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Data {
    @Id
    private Long id;

    @Lob
    private String encrypted_data;

    @Column(nullable = false)
    private String check_val;

    private String IV;

    private LocalDateTime expiryDate;
}
