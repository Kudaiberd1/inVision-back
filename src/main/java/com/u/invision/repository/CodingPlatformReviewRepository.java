package com.u.invision.repository;

import com.u.invision.entity.CodingPlatformReview;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodingPlatformReviewRepository extends JpaRepository<CodingPlatformReview, Long> {

	Optional<CodingPlatformReview> findByForm_IdAndPlatform(Long formId, String platform);
}

