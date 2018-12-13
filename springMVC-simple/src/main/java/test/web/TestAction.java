/**
 * 
 */
package test.web;

import springframwork.annotation.XiaoAutowired;
import springframwork.annotation.XiaoController;
import springframwork.annotation.XiaoRequestMapping;
import test.server.TestServer;

/**
 *
 * @author 肖超
 * @date: 2018年12月12日
 */
@XiaoController
@XiaoRequestMapping("/xx")
public class TestAction {
	@XiaoAutowired
	private TestServer server;

	@XiaoRequestMapping("/cc")
	public String xx() {
		return server.xc();
	}
	@XiaoRequestMapping("/cc1")
	public String xxd(int a) {
		System.out.println(a);
		return server.xc();
	}
}
