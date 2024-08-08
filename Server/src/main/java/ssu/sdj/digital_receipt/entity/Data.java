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
    @Column(nullable = false, length = 256)
    private String decoded_data;

    @Column(nullable = false)
    private String data_key;

    private LocalDateTime expiryDate;
}
