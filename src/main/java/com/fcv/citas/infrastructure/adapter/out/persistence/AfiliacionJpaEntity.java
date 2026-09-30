package com.fcv.citas.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Mapea `user_insurance_affiliations` (HU-005, RF-04). */
@Entity
@Table(name = "user_insurance_affiliations")
public class AfiliacionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "membership_number", nullable = false, length = 80)
    private String membershipNumber;

    @Column(name = "is_current", nullable = false)
    private boolean current = true;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    protected AfiliacionJpaEntity() {
        // JPA
    }

    public AfiliacionJpaEntity(Long id, Long userId, Long planId, String membershipNumber, boolean current,
                                LocalDate validFrom) {
        this.id = id;
        this.userId = userId;
        this.planId = planId;
        this.membershipNumber = membershipNumber;
        this.current = current;
        this.validFrom = validFrom;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getMembershipNumber() {
        return membershipNumber;
    }

    public boolean isCurrent() {
        return current;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }
}
