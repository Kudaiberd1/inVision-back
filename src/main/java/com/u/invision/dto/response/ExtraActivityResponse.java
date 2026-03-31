package com.u.invision.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtraActivityResponse {
    private String codeforces;
    private String leetcode;
    private String github;
    private String linkedin;
}
