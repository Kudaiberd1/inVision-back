package com.u.invision.repository;

import com.u.invision.entity.CVReview;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CVReviewRepository extends JpaRepository<CVReview, Long> {

	Optional<CVReview> findByForm_Id(Long formId);
}
