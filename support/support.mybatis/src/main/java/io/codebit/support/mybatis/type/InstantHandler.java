package io.codebit.support.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

/**
 * Map Java 8 Instant &lt;-&gt; java.sql.Timestamp
 */
@MappedTypes(Instant.class)
public class InstantHandler extends BaseTypeHandler<Instant>
{

	@Override
	public void
		setNonNullParameter(PreparedStatement ps, int i, Instant parameter, JdbcType jdbcType) throws SQLException
	{
		if (parameter == null)
		{
			ps.setTimestamp(i, null);
		} else
		{
			ps.setTimestamp(i, Timestamp.from(parameter));
		}
	}

	@Override
	public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		Timestamp ts = rs.getTimestamp(columnName);
		if (ts != null)
		{
			return ts.toInstant();
		}
		return null;
	}

	@Override
	public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		Timestamp ts = rs.getTimestamp(columnIndex);
		if (ts != null)
		{
			return ts.toInstant();
		}
		return null;
	}

	@Override
	public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		Timestamp ts = cs.getTimestamp(columnIndex);
		if (ts != null)
		{
			return ts.toInstant();
		}
		return null;
	}
}