package OneQ.OnSurvey.domain.survey.repository.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static OneQ.OnSurvey.domain.survey.entity.QFormRequest.formRequest;

@Repository
@RequiredArgsConstructor
public class FormRequestRepositoryImpl implements FormRequestRepository {

    private final FormRequestJpaRepository formRequestJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public FormRequest save(FormRequest request) {
        return formRequestJpaRepository.save(request);
    }

    @Override
    public Optional<FormRequest> findById(Long id) {
        return formRequestJpaRepository.findById(id);
    }

    @Override
    public List<FormRequest> findAllUnregistered() {
        return formRequestJpaRepository.findByIsRegisteredFalse();
    }

    @Override
    public Page<FormRequest> findAllWithFilters(String email, Boolean isRegistered, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (email != null && !email.isEmpty()) {
            builder.and(formRequest.requesterEmail.containsIgnoreCase(email));
        }
        if (isRegistered != null) {
            builder.and(formRequest.isRegistered.eq(isRegistered));
        }

        List<FormRequest> results = jpaQueryFactory.selectFrom(formRequest)
            .where(builder)
            .orderBy(formRequest.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
            .select(formRequest.count())
            .from(formRequest)
            .where(builder);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }
}
