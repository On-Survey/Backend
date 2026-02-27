package OneQ.OnSurvey.global.push.domain.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(
    name = "push_property",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_push_property_name_code_key",
            columnNames = {"template_name", "template_set_code", "context_key"}
        )
    }
)
public class PushProperty extends BaseEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "template_set_code", nullable = false)
    private String templateSetCode;

    @Column(name = "context_key")
    private String contextKey;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    private String description;

    public void updateContext(String value, String description) {
        this.defaultValue = value;
        this.description = description;
    }
}
