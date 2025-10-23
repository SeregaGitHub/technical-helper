package ru.kraser.technical_helper.common_module.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", length = 36, nullable = false)
    public String id;

    @Column(name = "created_by", length = 36)
    public String createdBy;

    @Column(name = "created_date")
    public LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 36)
    public String lastUpdatedBy;

    @Column(name = "last_updated_date")
    public LocalDateTime lastUpdatedDate;
}
