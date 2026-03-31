package com.u.invision.repository;

import com.u.invision.entity.Form;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FormRepository extends JpaRepository<Form, Long> {

    interface ExtraResponse{
        String getCodeforces();
        String getLeetcode();
        String getGithub();
        String getLinkedin();
    }

	@Query(
			"""
			select f from Form f
			where exists (select 1 from CVReview c where c.form = f)
			  and exists (select 1 from EssayReview e where e.form = f)
			order by f.createdAt desc
			""")
	List<Form> findAllForDashboard();

    @Query(
            """
			select f.codeforces as codeforces,
			       f.leetcode as leetcode,
			       f.github as github,
			       f.linkedin as linkedin
			from Form f
			where f.id = :formId
			"""
    )
	ExtraResponse getExtraForForm(@Param("formId") Long formId);

}
