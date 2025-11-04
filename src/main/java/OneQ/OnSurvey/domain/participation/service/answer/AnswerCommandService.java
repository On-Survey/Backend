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

@Transactional
@RequiredArgsConstructor
public abstract class AnswerCommandService<E extends AbstractAnswer> implements AnswerCommand<E> {
    private final AnswerRepository<E> answerRepository;
    private final MemberFinder memberFinder;

    public List<E> insertAnswers(AnswerInsertDto insertDto) {
        Long memberId = getMemberIdFromUserKey();

        List<E> answerList = insertDto.getAnswerInfoList().stream()
            .map(answerInfo -> createAnswerFromDto(answerInfo, memberId))
            .toList();
        return answerRepository.saveAll(answerList);
    }

    public E insertAnswer(AnswerInsertDto.AnswerInfo answerInfo) {
        Long memberId = getMemberIdFromUserKey();

        E answer = createAnswerFromDto(answerInfo, memberId);
        return answerRepository.save(answer);
    }

    // 추후 memberFinder 컨트롤러 계층으로 이동
    private Long getMemberIdFromUserKey() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userKey = customUserDetails.getUserKey();

        return memberFinder.getMemberByUserKey(userKey).getId();
    }

    protected abstract E createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo, Long memberId);
}
