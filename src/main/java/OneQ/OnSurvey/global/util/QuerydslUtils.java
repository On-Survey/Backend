package OneQ.OnSurvey.global.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Pageable;

public abstract class QuerydslUtils {

    public static <T> OrderSpecifier<?>[] getSort(Pageable pageable, EntityPathBase<T> qClass) {
        return pageable.getSort().stream().map(order ->
                new OrderSpecifier(
                    Order.valueOf(order.getDirection().name()),
                    Expressions.path(Object.class, qClass, order.getProperty())
                )).toList()
            .toArray(new OrderSpecifier[0]);
    }

    public static StringTemplate convertLocalDateTimeIntoStringTemplate(LocalDateTime dateTime) {
        String dateFormat = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringTemplate formattedDate = Expressions.stringTemplate("datetime({0})", dateFormat);
    
        return formattedDate;
    }
}
