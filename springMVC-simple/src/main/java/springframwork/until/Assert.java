/**
 * 
 */
package springframwork.until;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author 肖超
 * @date: 2018年12月13日
 */
public abstract class Assert {

	/**
	 *
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月13日
	 */
	public static void notBlank(String str, String message) throws Exception {
		if (str == null || str.trim().equals("")) {
			throw new Exception(message);
		}
	}

	/**
	 *
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月13日
	 */
	public static void notNull(Object str, String message) throws Exception {
		if (str == null) {
			throw new Exception(message);
		}
	}

	/**
	 *
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月13日
	 */
	public static void notEmpty(Collection con, String message) throws Exception {
		if (con.isEmpty()) {
			throw new Exception(message);
		}
	}

	/**
	 *
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月13日
	 */
	public static void notEmpty(Object[] obj, String message) throws Exception {
		if (obj.length < 1) {
			throw new Exception(message);
		}
	}

	/**
	 *
	 * @author: 肖超
	 * @throws Exception
	 * @date: 2018年12月13日
	 */
	public static void notEmpty(Map map, String message) throws Exception {
		if (map.isEmpty()) {
			throw new Exception(message);
		}
	}
}
