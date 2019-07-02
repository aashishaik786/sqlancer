package postgres.ast;

import lama.IgnoreMeException;
import postgres.PostgresSchema.PostgresDataType;

public abstract class PostgresConstant extends PostgresExpression {

	public abstract String getTextRepresentation();

	public static class BooleanConstant extends PostgresConstant {

		private final boolean value;

		public BooleanConstant(boolean value) {
			this.value = value;
		}

		@Override
		public String getTextRepresentation() {
			return value ? "TRUE" : "FALSE";
		}

		@Override
		public PostgresDataType getExpressionType() {
			return PostgresDataType.BOOLEAN;
		}

		@Override
		public boolean asBoolean() {
			return value;
		}

		@Override
		public boolean isBoolean() {
			return true;
		}

		@Override
		public PostgresConstant isEquals(PostgresConstant rightVal) {
			if (rightVal.isNull()) {
				return PostgresConstant.createNullConstant();
			} else if (rightVal.isBoolean()) {
				return PostgresConstant.createBooleanConstant(value == rightVal.asBoolean());
			} else if (rightVal.isString()) {
				return PostgresConstant.createBooleanConstant(value == rightVal.cast(PostgresDataType.BOOLEAN).asBoolean());
			} else {
				throw new AssertionError(rightVal);
			}
		}

		@Override
		protected PostgresConstant isLessThan(PostgresConstant rightVal) {
			if (rightVal.isNull()) {
				return PostgresConstant.createNullConstant();
			} else if (rightVal.isString()) {
				return isLessThan(rightVal.cast(PostgresDataType.BOOLEAN));
			} else {
				assert rightVal.isBoolean();
				return PostgresConstant.createBooleanConstant((value ? 1 : 0) < (rightVal.asBoolean() ? 1 : 0));
			}
		}

		@Override
		public PostgresConstant cast(PostgresDataType type) {
			switch (type) {
			case BOOLEAN:
				return this;
			case INT:
				return PostgresConstant.createIntConstant((value) ? 1 : 0);
			case TEXT:
				return PostgresConstant.createTextConstant(value ? "true" : "false");
			default:
				throw new AssertionError();
			}
		}

	}

	public static class PostgresNullConstant extends PostgresConstant {

		@Override
		public String getTextRepresentation() {
			return "NULL";
		}

		@Override
		public PostgresDataType getExpressionType() {
			return null;
		}

		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public PostgresConstant isEquals(PostgresConstant rightVal) {
			return PostgresConstant.createNullConstant();
		}

		@Override
		protected PostgresConstant isLessThan(PostgresConstant rightVal) {
			return PostgresConstant.createNullConstant();
		}

		@Override
		public PostgresConstant cast(PostgresDataType type) {
			return PostgresConstant.createNullConstant();
		}

	}
	
	public static class StringConstant extends PostgresConstant {
		
		private final String value;
		
		public StringConstant(String value) {
			this.value = value;
		}

		@Override
		public String getTextRepresentation() {
			return String.format("'%s'", value.replace("'", "''"));
		}

		@Override
		public PostgresConstant isEquals(PostgresConstant rightVal) {
			if (rightVal.isNull()) {
				return PostgresConstant.createNullConstant();
			} else if (rightVal.isInt()) {
				return cast(PostgresDataType.INT).isEquals(rightVal.cast(PostgresDataType.INT));
			} else if (rightVal.isBoolean()) {
				return cast(PostgresDataType.BOOLEAN).isEquals(rightVal.cast(PostgresDataType.BOOLEAN));
			} else if (rightVal.isString()) {
				return PostgresConstant.createBooleanConstant(value.contentEquals(rightVal.asString()));
			} else {
				throw new AssertionError(rightVal);
			}
		}

		@Override
		protected PostgresConstant isLessThan(PostgresConstant rightVal) {
			if (rightVal.isNull()) {
				return PostgresConstant.createNullConstant();
			} else if (rightVal.isInt()) {
				return cast(PostgresDataType.INT).isLessThan(rightVal.cast(PostgresDataType.INT));
			} else if (rightVal.isBoolean()) {
				return cast(PostgresDataType.BOOLEAN).isLessThan(rightVal.cast(PostgresDataType.BOOLEAN));
			} else if (rightVal.isString()) {
				return PostgresConstant.createBooleanConstant(value.compareTo(rightVal.asString()) < 0);
			} else {
				throw new AssertionError(rightVal);
			}
		}

		@Override
		public PostgresConstant cast(PostgresDataType type) {
			if (type == PostgresDataType.TEXT) {
				return this;
			}
			String s = value.trim();
			switch (type) {
			case BOOLEAN:
				try {
					return PostgresConstant.createBooleanConstant(Long.valueOf(s) != 0);
				} catch (NumberFormatException e) {
				}
				switch (s.toUpperCase()) {
				case "T":
				case "TR":
				case "TRU":
				case "TRUE":
				case "1":
				case "YES":
				case "YE":
				case "Y":
				case "ON":
					return PostgresConstant.createTrue();
				case "F":
				case "FA":
				case "FAL":
				case "FALS":
				case "FALSE":
				case "N":
				case "NO":
				case "OF":
				case "OFF":
				default:
					return PostgresConstant.createFalse();
				}
			case INT:
				try {
					return PostgresConstant.createIntConstant(Long.valueOf(s));
				} catch (NumberFormatException e) {
					return PostgresConstant.createIntConstant(-1);
				}
			case TEXT:
				return this;
			default:
				throw new AssertionError(this);
			}
		}

		@Override
		public PostgresDataType getExpressionType() {
			return PostgresDataType.TEXT;
		}
		
		@Override
		public boolean isString() {
			return true;
		}
		
		@Override
		public String asString() {
			return value;
		}
		
	}

	public static class IntConstant extends PostgresConstant {

		private long val;

		public IntConstant(long val) {
			this.val = val;
		}

		@Override
		public String getTextRepresentation() {
			return String.valueOf(val);
		}

		@Override
		public PostgresDataType getExpressionType() {
			return PostgresDataType.INT;
		}

		@Override
		public long asInt() {
			return val;
		}

		@Override
		public boolean isInt() {
			return true;
		}

		@Override
		public PostgresConstant isEquals(PostgresConstant rightVal) {
			if (rightVal.isNull()) {
				return PostgresConstant.createNullConstant();
			} else if (rightVal.isBoolean()) {
				return cast(PostgresDataType.BOOLEAN).isEquals(rightVal);
			} else if (rightVal.isInt()) {
				return PostgresConstant.createBooleanConstant(val == rightVal.asInt());
			}  else if (rightVal.isString()) {
				return PostgresConstant.createBooleanConstant(val == rightVal.cast(PostgresDataType.INT).asInt());
			} else {
				throw new AssertionError(rightVal);
			}
		}

		@Override
		protected PostgresConstant isLessThan(PostgresConstant rightVal) {
			if (rightVal.isNull()) {
				return PostgresConstant.createNullConstant();
			} else if (rightVal.isInt()) {
				return PostgresConstant.createBooleanConstant(val < rightVal.asInt());
			} else if (rightVal.isBoolean()) {
				throw new AssertionError(rightVal);
			} else if (rightVal.isString()) {
				return PostgresConstant.createBooleanConstant(val < rightVal.cast(PostgresDataType.INT).asInt());
			} else {
				throw new IgnoreMeException();
			}
			
		}

		@Override
		public PostgresConstant cast(PostgresDataType type) {
			switch (type) {
			case BOOLEAN:
				return PostgresConstant.createBooleanConstant(val != 0);
			case INT:
				return this;
			case TEXT:
				return PostgresConstant.createTextConstant(String.valueOf(val));
			default:
				throw new AssertionError(type);
			}
		}

	}

	public static PostgresConstant createNullConstant() {
		return new PostgresNullConstant();
	}

	public String asString() {
		throw new UnsupportedOperationException(this.toString());
	}

	public boolean isString() {
		return false;
	}

	public static PostgresConstant createIntConstant(long val) {
		return new IntConstant(val);
	}

	public static PostgresConstant createBooleanConstant(boolean val) {
		return new BooleanConstant(val);
	}

	@Override
	public PostgresConstant getExpectedValue() {
		return this;
	}

	public boolean isNull() {
		return false;
	}

	public boolean asBoolean() {
		throw new UnsupportedOperationException(this.toString());
	}

	public static PostgresConstant createFalse() {
		return createBooleanConstant(false);
	}

	public static PostgresConstant createTrue() {
		return createBooleanConstant(true);
	}

	public long asInt() {
		throw new UnsupportedOperationException(this.toString());
	}

	public boolean isBoolean() {
		return false;
	}

	public abstract PostgresConstant isEquals(PostgresConstant rightVal);

	public boolean isInt() {
		return false;
	}

	protected abstract PostgresConstant isLessThan(PostgresConstant rightVal);

	@Override
	public String toString() {
		return getTextRepresentation();
	}

	public abstract PostgresConstant cast(PostgresDataType type);

	public static PostgresConstant createTextConstant(String string) {
		return new StringConstant(string);
	}

}
