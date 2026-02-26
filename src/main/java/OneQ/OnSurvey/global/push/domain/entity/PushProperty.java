package OneQ.OnSurvey.global.push.domain.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(
    name = "push_property",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_push_property_code_key",
            columnNames = {"template_set_code", "key"}
        ),
        @UniqueConstraint(
            name = "unique_push_property_name",
            columnNames = {"template_name"}
        ),
    }
)
@IdClass(PushProperty.class)
public class PushProperty extends BaseEntity {

    @Column(name = "template_name")
    private String templateName;

    @Id
    @Column(name = "template_set_code")
    private String templateSetCode;

    @Id
    private String key;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    private String description;
}
