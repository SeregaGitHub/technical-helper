package ru.kraser.technical_helper.main_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity extends BaseEntityShort {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", length = 36, nullable = false)
    protected String id;

    @Column(name = "created_by", length = 36)
    protected String createdBy;

    @Column(name = "created_date")
    protected LocalDateTime createdDate;

//    @Column(name = "last_updated_by", length = 36)
//    protected String lastUpdatedBy;

//    @Column(name = "last_updated_date")
//    protected LocalDateTime lastUpdatedDate;
}
