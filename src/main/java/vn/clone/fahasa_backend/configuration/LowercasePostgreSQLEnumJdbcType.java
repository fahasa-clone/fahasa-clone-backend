package vn.clone.fahasa_backend.configuration;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;

public class LowercasePostgreSQLEnumJdbcType extends PostgreSQLEnumJdbcType {
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
}
