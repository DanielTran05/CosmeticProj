package com.dtp.cosmemgt.warehouse.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "supplier")
@SQLDelete(sql = "update supplier SET deleted_at = NOW() where id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Supplier extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Basic(optional = false)
    @NotNull
    String name;

    @Basic(optional = false)
    @NotNull
    String contactName;

    @Basic(optional = false)
    @NotNull
    String phoneNum;

    @Basic(optional = false)
    @NotNull
    String address;
}