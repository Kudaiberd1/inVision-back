package com.u.invision.repository;

import com.u.invision.entity.EssayReview;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EssayReviewRepository extends JpaRepository<EssayReview, Long> {

	Optional<EssayReview> findByForm_Id(Long formId);
}
