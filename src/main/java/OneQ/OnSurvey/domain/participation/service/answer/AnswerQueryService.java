package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class AnswerQueryService<E extends AbstractAnswer> implements AnswerQuery<E> {
    protected final AnswerRepository<E> answerRepository;
    private final MemberFinder memberFinder;

    public List<E> getAnswersByIdListAndMemberId(List<Long> idList, Long memberId) {
        return answerRepository.getAnswersByQuestionIdListAndMemberId(idList, memberId);
    }

    // 추후 memberFinder 컨트롤러 계층으로 이동
    protected Long getMemberIdFromUserKey() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userKey = customUserDetails.getUserKey();

        return memberFinder.getMemberByUserKey(userKey).getId();
    }

    protected abstract E createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo, Long memberId);
}
