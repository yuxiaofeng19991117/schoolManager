package club.pinea.school.config;


/**
 * 费用类型
 * @author Administrator
 *
 */
public enum FeedsType {

	/**
	 * 书本费
	 */
	BOOK_FEEDS("书本费"),
	
	/**
	 * 学费
	 */
	STUDY_FEEDS("学费"),
	
	/**
	 * 其他费用
	 */
	OTHER_FEEDS("其他费用");
	
	private String name;
	
	FeedsType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
