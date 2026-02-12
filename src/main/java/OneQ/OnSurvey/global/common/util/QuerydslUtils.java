package OneQ.OnSurvey.global.common.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class QuerydslUtils {

    public static <T> OrderSpecifier<?>[] getSort(Pageable pageable, EntityPathBase<T> qClass) {
        return pageable.getSort().stream().map(order ->
                new OrderSpecifier(
                    Order.valueOf(order.getDirection().name()),
                    Expressions.path(Object.class, qClass, order.getProperty())
                )).toList()
            .toArray(new OrderSpecifier[0]);
    }

    public static <T> OrderSpecifier<?>[] getSortPaidFirst(
            Pageable pageable,
            EntityPathBase<T> qClass,
            BooleanPath isFreePath
    ) {
        OrderSpecifier<?>[] baseSort = getSort(pageable, qClass);

        OrderSpecifier<?>[] result = new OrderSpecifier<?>[baseSort.length + 1];
        result[0] = isFreePath.asc();

        System.arraycopy(baseSort, 0, result, 1, baseSort.length);
        return result;
    }

    public static <T> Slice<T> createSlice(List<T> contents, Pageable pageable) {
        boolean hasNext = false;
        int size = pageable.getPageSize();

        if (contents.size() > size) {
            hasNext = true;
            contents = new ArrayList<>(contents.subList(0, size));
        }

        return new SliceImpl<>(contents, pageable, hasNext);
    }
}
