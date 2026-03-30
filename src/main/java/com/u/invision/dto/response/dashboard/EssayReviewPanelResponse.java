package com.u.invision.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EssayReviewPanelResponse {

    private String essayFullText;
    private String essayPdfUrl;
    private EssayReviewDetailResponse essayReview;
}
