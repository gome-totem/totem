package org.z.global.object;

import org.z.global.dict.Global.Role;


public class UserRole {

	public long value = 0;

	public UserRole(long value) {
		this.value = value;
	}

	public UserRole() {
		this(0);
	}

	public boolean isTourist() {
		long v = 1 << Role.Tourist.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isMember() {
		long v = 1 << Role.Member.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isGuider() {
		long v = 1 << Role.Guider.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isLeader() {
		long v = 1 << Role.Leader.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isTravelAgent() {
		long v = 1 << Role.TravelAgent.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isLandlord() {
		long v = 1 << Role.Landlord.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isHotel() {
		long v = 1 << Role.Hotel.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isDriver() {
		long v = 1 << Role.Driver.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isAccounter() {
		if (isRoot())
			return true;
		long v = 1 << Role.Accounter.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isCarAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.CarAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isRoomAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.RoomAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isTripAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.TripAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isTopicAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.TopicAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isRequireAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.RequireAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isProductAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.ProductAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isPushAssistant() {
		if (isRoot())
			return true;
		long v = 1 << Role.PushAssistant.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isCustomerService() {
		if (isRoot())
			return true;
		long v = 1 << Role.CustomerService.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public boolean isRoot() {
		long v = 1 << Role.Root.ordinal();
		v = this.value & v;
		return v != 0;
	}

	public long set(Role role) {
		long v = role.ordinal();
		v = 1 << v;
		this.value = this.value | v;
		return this.value;
	}

	public long clear(Role role) {
		this.value = this.value & ~(1 << role.ordinal());
		return this.value;
	}

	public String toString() {
		return String.valueOf(value);
	}

	public static void main(String[] args) {
		UserRole record = new UserRole();
		System.out.println(record.set(Role.Accounter));
		System.out.println(record.set(Role.Root));
		System.out.println(record.set(Role.Member));
		System.out.println(record.clear(Role.Root));
		System.out.println(record.clear(Role.Accounter));
		System.out.println(record.clear(Role.Member));
	}
}
