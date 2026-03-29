package com.u.invision.repository;

import com.u.invision.entity.InterviewResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {

	Optional<InterviewResult> findBySessionId(String sessionId);

	Optional<InterviewResult> findFirstByCandidateIdIgnoreCaseOrderByCompletedAtDesc(String candidateId);
}
