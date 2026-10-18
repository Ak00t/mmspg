package com.ojt_22.mmspg.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff_login_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffLoginActivity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "staff_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID staffId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "device", length = 100)
    private String device;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
}
