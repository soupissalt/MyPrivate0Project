package com.record.myprivateproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    @Query("SELECT m FROM MemberEntity m WHERE m.email = ?1")
    Optional<MemberEntity> findMemberEntitiesByEmail(String email);
}
