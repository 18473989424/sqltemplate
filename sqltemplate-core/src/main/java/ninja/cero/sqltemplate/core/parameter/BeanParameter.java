package ninja.cero.sqltemplate.core.parameter;

import ninja.cero.sqltemplate.core.util.BeanFields;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link org.springframework.jdbc.core.namedparam.SqlParameterSource} implementation that obtains parameter values
 * from public fields of a given value object.
 * Supports {@link java.time.LocalDateTime} and {@link java.time.LocalDate} of JSR-310
 */
public class BeanParameter extends AbstractSqlParameterSource {
    /** the value object for parameters */
    private Object entity;

    /** Map of the fields we provide mapping for */
    private Map<String, Field> mappedFields = new HashMap<>();

    /**
     * Create a new BeanParameter for the given value object.
     * @param entity the value object for parameters
     * @return a new BeanParameter
     */
    public static BeanParameter of(Object entity) {
        return new BeanParameter(entity);
    }

    /**
     * Create a new BeanParameter for the given value object.
     * @param entity the value object for parameters
     */
    protected BeanParameter(Object entity) {
        this.entity = entity;
        for (Field field : BeanFields.get(entity.getClass())) {
            mappedFields.put(field.getName(), field);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasValue(String paramName) {
        return mappedFields.containsKey(paramName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String paramName) {
        Field field = mappedFields.get(paramName);
        if (field == null) {
            return null;
        }

        Object value;
        try {
            value = field.get(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            return Date.valueOf((LocalDate) value);
        } else if (value instanceof LocalTime) {
            return Time.valueOf((LocalTime) value);
        } else if (value instanceof OffsetDateTime) {
            ZonedDateTime zonedDateTime = ((OffsetDateTime) value).atZoneSameInstant(ZoneId.systemDefault());
            return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        } else if (value instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = ((ZonedDateTime) value).withZoneSameInstant(ZoneId.systemDefault());
            return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        }

        return value;
    }
}
