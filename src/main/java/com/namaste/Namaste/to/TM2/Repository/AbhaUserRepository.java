package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.AbhaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbhaUserRepository extends JpaRepository<AbhaUser, Long> {
    Optional<AbhaUser> findByHealthId(String healthId);
    Optional<AbhaUser> findByHealthIdNumber(String healthIdNumber);
    Optional<AbhaUser> findByMobile(String mobile);
    boolean existsByHealthId(String healthId);
    boolean existsByMobile(String mobile);
}
