package OneQ.OnSurvey.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileImageUpdateRequest(
        @Schema(description = "프로필 이미지 URL (null 가능)", example = "https://kr.object.ncloudstorage.com/onsurvey/public/member/2025/11/13/20251113_1_12a4652111cb4ec2ad6ad99c7c0fea1c.jpg")
        String profileUrl
) {}
