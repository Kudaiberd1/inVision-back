package com.u.invision.dto.response.dashboard;

/**
 * {@code essayFullText} — LaTeX fragment (PDF text in {@code verbatim}). {@code essayPdfUrl} — HTTPS URL to the
 * original essay PDF.
 */
public record EssayReviewPanelResponse(String essayFullText, String essayPdfUrl, EssayReviewDetailResponse essayReview) {}
