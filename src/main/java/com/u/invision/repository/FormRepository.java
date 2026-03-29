package com.u.invision.repository;

import com.u.invision.entity.Form;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FormRepository extends JpaRepository<Form, Long> {

	@Query(
			"""
			select f from Form f
			where exists (select 1 from CVReview c where c.form = f)
			  and exists (select 1 from EssayReview e where e.form = f)
			order by f.createdAt desc
			""")
	List<Form> findAllForDashboard();
}
