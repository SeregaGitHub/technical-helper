package ru.kraser.technical_helper.common_module.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "department")
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {
    @Column(name = "name", length = 64, nullable = false, unique = true)
    private String name;

    @JsonProperty(value = "isEnabled")
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
