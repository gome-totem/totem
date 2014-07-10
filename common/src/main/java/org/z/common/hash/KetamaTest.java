package org.z.common.hash;

import java.util.ArrayList;

import org.z.global.interfaces.KetamaNodeIntf;


class TableInfo {
	public String serverIP = null;

	public TableInfo(String serverIP) {
		this.serverIP = serverIP;
	}

}

public class KetamaTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<TableInfo> list = new ArrayList<TableInfo>();
		for (int i = 0; i < 11; i++) {
			list.add(new TableInfo("192.168.1." + i));
		}
		KetamaLocator<TableInfo> locator = new KetamaLocator<TableInfo>(list, new KetamaNodeIntf<TableInfo>() {

			@Override
			public String getKeyForNode(TableInfo t, int repetition) {
				return t.serverIP + "-" + repetition;
			}

			@Override
			public int getNodeRepetitions() {
				return 10;
			}

		}, HashAlgorithm.KETAMA_HASH);
		for (int i = 0; i < 100; i++) {
			TableInfo info = locator.getPrimary(String.valueOf(i));
			System.out.println(i + "====>" + info.serverIP);
		}

	}
}
