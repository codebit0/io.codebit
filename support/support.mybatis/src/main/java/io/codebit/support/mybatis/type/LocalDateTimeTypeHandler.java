package io.codebit.support.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(LocalDateTime.class)
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime>
{

	@Override
	public
		void
		setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException
	{
		if (parameter == null)
		{
			ps.setTimestamp(i, null);
		} else
		{
			ps.setTimestamp(i, Timestamp.valueOf(parameter));
		}
	}

	@Override
	public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException
	{
		Timestamp ts = rs.getTimestamp(columnName);
		if (ts != null)
		{
			return LocalDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
		}
		return null;
	}

	@Override
	public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException
	{
		Timestamp ts = rs.getTimestamp(columnIndex);
		if (ts != null)
		{
			return LocalDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
		}
		return null;
	}

	@Override
	public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		Timestamp ts = cs.getTimestamp(columnIndex);
		if (ts != null)
		{
			Instant instant = Instant.ofEpochMilli(ts.getTime());
			return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//			ZoneOffset offset = OffsetTime.now().getOffset();
//			return LocalDateTime.ofInstant(ts.toInstant(), offset);
			//return LocalDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
		}
		return null;
	}
}
