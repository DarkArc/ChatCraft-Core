/**
 * This class is generated by jOOQ
 */
package com.nearce.chatcraft.db.schema;


import com.nearce.chatcraft.db.schema.tables.VerifiedUsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.0"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CcDb extends SchemaImpl {

	private static final long serialVersionUID = 228911732;

	/**
	 * The reference instance of <code>cc_db</code>
	 */
	public static final CcDb CC_DB = new CcDb();

	/**
	 * No further instances allowed
	 */
	private CcDb() {
		super("cc_db");
	}

	@Override
	public final List<Table<?>> getTables() {
		List result = new ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final List<Table<?>> getTables0() {
		return Arrays.<Table<?>>asList(
			VerifiedUsers.VERIFIED_USERS);
	}
}
