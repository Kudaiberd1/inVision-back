package com.u.invision.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.u.invision.dto.interview.InterviewSessionResponse;
import com.u.invision.dto.interview.InterviewSessionResponse.InterviewDetail;
import com.u.invision.dto.interview.InterviewSessionResponse.InterviewScoring;
import com.u.invision.entity.InterviewResult;
import com.u.invision.repository.InterviewResultRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewResultPersistenceService {

	private final InterviewResultRepository repository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public InterviewResultPersistenceService(InterviewResultRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void saveFinalIfCompleted(InterviewSessionResponse body) {
		if (!Boolean.TRUE.equals(body.interviewCompleted) || body.sessionId == null || body.sessionId.isBlank()) {
			return;
		}

		InterviewResult entity = repository.findBySessionId(body.sessionId).orElseGet(InterviewResult::new);
		entity.setSessionId(body.sessionId);
		entity.setInterviewCompleted(true);
		entity.setQuestionsAsked(body.questionsAsked);
		entity.setMaxQuestions(body.maxQuestions);

		InterviewScoring sc = body.scoring;
		if (sc == null && body.interview != null) {
			sc = body.interview.scoring;
		}
		if (sc != null) {
			entity.setLeadershipScore(sc.leadershipScore);
			entity.setProactivenessScore(sc.proactivenessScore);
			entity.setEnergyScore(sc.energyScore);
			entity.setChatbotScore(sc.chatbotScore);
		} else if (body.interview != null) {
			entity.setLeadershipScore(body.interview.leadershipScore);
			entity.setProactivenessScore(body.interview.proactivenessScore);
			entity.setEnergyScore(body.interview.energyScore);
			entity.setChatbotScore(body.interview.chatbotScore);
		}

		String jury = body.jurySessionSummary;
		if ((jury == null || jury.isBlank()) && body.interview != null) {
			jury = body.interview.jurySessionSummary;
		}
		entity.setJurySessionSummary(jury);

		InterviewDetail inv = body.interview;
		if (inv != null) {
			entity.setCandidateId(inv.candidateId);
			entity.setCandidateStage(inv.candidateStage);
		}

		try {
			entity.setResponseJson(objectMapper.writeValueAsString(body));
		} catch (JsonProcessingException e) {
			entity.setResponseJson("{\"error\":\"failed to serialize response\"}");
		}

		entity.setCompletedAt(Instant.now());
		repository.save(entity);
	}
}
