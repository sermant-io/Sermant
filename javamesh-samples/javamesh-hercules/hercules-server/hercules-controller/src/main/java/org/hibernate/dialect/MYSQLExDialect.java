package org.hibernate.dialect;

import java.sql.Types;

public class MYSQLExDialect extends MySQL5Dialect {

	public MYSQLExDialect() {
		super();
		registerColumnType( Types.VARCHAR, 65535, "text" );

	}
}

