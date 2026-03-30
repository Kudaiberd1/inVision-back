package com.u.invision.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CvReviewPanelResponse {

    private String cvFullText;
    private String cvPdfUrl;
    private CvReviewDetailResponse cvReview;
}
