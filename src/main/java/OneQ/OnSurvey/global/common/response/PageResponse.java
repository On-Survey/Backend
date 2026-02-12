package OneQ.OnSurvey.global.common.response;

import OneQ.OnSurvey.global.common.response.result.ResponseState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PageResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private final boolean isSuccess = true;

    @Schema(description = "상태 코드", example = "1")
    private final int code;

    @Schema(description = "응답 메세지", example = "성공하였습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final List<T> result;

    @Schema(description = "현재 페이지 번호", example = "0")
    private final int pageNumber;

    @Schema(description = "페이지 크기", example = "10")
    private final int pageSize;

    @Schema(description = "전체 요소 개수", example = "205")
    private final long totalElements;

    @Schema(description = "전체 페이지 개수", example = "21")
    private final int totalPages;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private final boolean last;

    public static <T> PageResponse<T> of(int code, String message, Page<T> data) {
        return new PageResponse<>(
            code,
            message,
            data.getContent(),
            data.getNumber(),
            data.getSize(),
            data.getTotalElements(),
            data.getTotalPages(),
            data.isLast()
        );
    }

    public static <T> PageResponse<T> ok(Page<T> data) {
        return of(ResponseState.SUCCESS.getCode(), ResponseState.SUCCESS.getMessage(), data);
    }
}
