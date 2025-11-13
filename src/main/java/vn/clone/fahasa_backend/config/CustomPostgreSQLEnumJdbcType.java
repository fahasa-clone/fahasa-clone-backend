package vn.clone.fahasa_backend.config;

import java.sql.*;

import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;

public class CustomPostgreSQLEnumJdbcType extends PostgreSQLEnumJdbcType {
    @Override
    public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
        return new BasicBinder<>(javaType, this) {
            @Override
            protected void doBindNull(PreparedStatement st, int index, WrapperOptions options) throws SQLException {
                st.setNull(index, Types.OTHER);
            }

            @Override
            protected void doBindNull(CallableStatement st, String name, WrapperOptions options) throws SQLException {
                st.setNull(name, Types.OTHER);
            }

            @Override
            protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
                    throws SQLException {
                st.setObject(index, ((Enum<?>) value).name().toLowerCase(), Types.OTHER);
            }

            @Override
            protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
                    throws SQLException {
                st.setObject(name, ((Enum<?>) value).name().toLowerCase(), Types.OTHER);
            }
        };

    }

    @Override
    public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
        return new BasicExtractor<>(javaType, this) {
            @Override
            protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
                String result = (String) rs.getObject(paramIndex);
                if (result == null) {
                    return null;
                }
                return getJavaType().wrap(result.toUpperCase(), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                String result = (String) statement.getObject(index);
                return getJavaType().wrap(result.toUpperCase(), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                String result = (String) statement.getObject(name);
                return getJavaType().wrap(result.toUpperCase(), options);
            }
        };

    }
}
