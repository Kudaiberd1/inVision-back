package com.u.invision.dto.response.dashboard;

/**
 * {@code cvFullText} — LaTeX fragment (PDF text in {@code verbatim}) for text preview. {@code cvPdfUrl} — HTTPS URL
 * to the original PDF (embed, new tab, or download).
 */
public record CvReviewPanelResponse(String cvFullText, String cvPdfUrl, CvReviewDetailResponse cvReview) {}
