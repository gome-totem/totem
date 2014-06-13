package org.x.cloud.ip;

public class IPAddressRange {
	/** IP地址段开始 */
	private Long ipStartAddress;
	/** IP地址段结束 */
    private Long ipEndAddress;
    /** IP地址对应城市编号 */
    private String cityCode;
    
	public Long getIpStartAddress() {
		return ipStartAddress;
	}
	public void setIpStartAddress(Long ipStartAddress) {
		this.ipStartAddress = ipStartAddress;
	}
	public Long getIpEndAddress() {
		return ipEndAddress;
	}
	public void setIpEndAddress(Long ipEndAddress) {
		this.ipEndAddress = ipEndAddress;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	
	@Override
	public String toString() {
		return getIpStartAddress() + "-" + getIpEndAddress() + "\t\t" + getCityCode();
	}
	
	
    
}
