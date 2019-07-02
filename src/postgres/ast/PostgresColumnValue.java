package postgres.ast;

import postgres.PostgresSchema.PostgresColumn;
import postgres.PostgresSchema.PostgresDataType;

public class PostgresColumnValue  extends PostgresExpression {
	
	private final PostgresColumn c;
	private final PostgresConstant expectedValue;
	

	public PostgresColumnValue(PostgresColumn c, PostgresConstant expectedValue) {
		this.c = c;
		this.expectedValue = expectedValue;
	}

	@Override
	public PostgresDataType getExpressionType() {
		return c.getColumnType();
	}

	@Override
	public PostgresConstant getExpectedValue() {
		return expectedValue;
	}
	
	public static PostgresColumnValue create(PostgresColumn c) {
		return new PostgresColumnValue(c, null);
	}
	
	public static PostgresColumnValue create(PostgresColumn c, PostgresConstant expected) {
		return new PostgresColumnValue(c, expected);
	}

	public PostgresColumn getColumn() {
		return c;
	}

}
