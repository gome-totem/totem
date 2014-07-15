package org.z.core.module;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.z.common.tree.RadixTreeImpl;
import org.z.core.common.Context;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.dict.Global.UserType;
import org.z.global.environment.Const;
import org.z.global.object.SecurityObject;
import org.z.global.object.UserRole;
import org.z.global.util.ImageUtil;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeType;
import org.z.store.mongdb.DBFileSystem;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ModuleImage implements ServiceIntf {
	protected DBFileSystem shotFS = null;
	protected DBFileSystem pictureFS = null;
	protected RadixTreeImpl<String> countries = null;
	protected RadixTreeImpl<String> areas = null;
	protected Logger logger = LoggerFactory.getLogger(ModuleImage.class);

	@Override
	public boolean init(boolean isReload) {
		shotFS = new DBFileSystem("image", "shots");
		pictureFS = new DBFileSystem("image", "pictures");
		return true;
	}

	@Override
	public void afterCreate(Object[] params) {
	}

	@Override
	public void start(boolean isReload) {
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public String getId() {
		return "image";
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		return null;
	}

	protected void trieCountryDict() {
		countries = new RadixTreeImpl<String>();
		areas = new RadixTreeImpl<String>();
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select country from country_dict where area=''", 0, 0);
		for (int i = 0; i < rows.length; i++) {
			countries.insert(rows[i][0], rows[i][0]);
		}
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select area from country_dict where area!=''", 0, 0);
		for (int i = 0; i < rows.length; i++) {
			areas.insert(rows[i][0], rows[i][0]);
		}
	}

	protected String extractPrice(String value) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			if (Character.isDigit(value.charAt(i))) {
				buffer.append(value.charAt(i));
			} else if (buffer.length() > 0) {
				break;
			}
		}
		if (buffer.length() == 0) {
			return "30";
		}
		return buffer.toString();
	}

	protected boolean isSmallPicture(long userId, String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			return true;
		}
		try {
			BufferedImage sImage = ImageUtil.readImage(file);
			if (sImage.getWidth() < 160 && sImage.getHeight() < 160) {
				FileUtils.deleteDirectory(file);
				return true;
			}
		} catch (Exception e) {
			return true;
		}
		return false;
	}

//	protected String[] uploadPicture(long userId, String fileName, String title, String tag) {
//		File file = new File(fileName);
//		if (!file.exists()) {
//			return null;
//		}
//		String[] names = fileName.split("/");
//		String name = null;
//		if (names.length >= 3) {
//			name = names[names.length - 3] + "-" + names[names.length - 2] + "-" + file.getName();
//		} else if (names.length >= 2) {
//			name = names[names.length - 2] + "-" + file.getName();
//		} else {
//			name = file.getName();
//		}
//		String tableName = "i" + userId;
//		String server = ServiceFactory.user().userRecordByCache(userId).server;
//		BasicDBObject one = (BasicDBObject) DataCollection.findOne(server, "pictures", tableName, new BasicDBObject().append("fileName", name));
//		if (one != null) {
//			return new String[] { one.getString("shot"), one.getString("picture") };
//		}
//		ArrayList<MagickImage> destroies = new ArrayList<MagickImage>();
//		try {
//			MagickImage sImage = ImageUtil.readMagickImage(fileName, destroies);
//			Dimension dim = sImage.getDimension();
//			if (dim.width < 360 && dim.height < 360) {
//				return null;
//			}
//			MagickImage shotImage = ImageUtil.resize(ImageType.Snapshot, sImage, destroies);
//			sImage = ImageUtil.resize(ImageType.Picture, sImage, destroies);
//			dim = sImage.getDimension();
//			BasicDBObject meta = new BasicDBObject().append("userId", userId).append("lastModified", System.currentTimeMillis());
//			meta.append("width", dim.width);
//			meta.append("height", dim.height);
//			String pictureId = pictureFS.saveFile(ImageUtil.readBytes(sImage), userId + "~" + name, "jpg", meta);
//			meta.append("pictureId", pictureId);
//			String shotId = shotFS.saveFile(ImageUtil.readBytes(shotImage), userId + "~" + name, "jpg", meta);
//			BasicDBObject oRecord = new BasicDBObject();
//			oRecord.append("fileName", name);
//			oRecord.append("title", title).append("tag", tag).append("state", RecordState.New.ordinal());
//			oRecord.append("contentType", "jpg");
//			oRecord.append("shot", shotId).append("picture", pictureId);
//			oRecord.append("source", "upload");
//			DataCollection.insert(server, "pictures", tableName, oRecord);
//			return new String[] { shotId, pictureId };
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		} finally {
//			ImageUtil.free(destroies);
//		}
//	}

	protected int hashUserId() {
		int result = RandomUtils.nextInt(301);
		while (result >= 301 || result < 20) {
			result = RandomUtils.nextInt(301);
		}
		return result;
	}

	public BasicDBObject createUser(String email, String mobile, String qq, String msn, String name, String city) {
		String key = email;
		String sql = "select user_id,user_name,email,user_type,qq,msn from user where email=?";
		if (StringUtil.isEmpty(key) && (!StringUtil.isEmpty(qq))) {
			key = qq + "@qq.com";
		}
		if (StringUtil.isEmpty(key) && (!StringUtil.isEmpty(mobile))) {
			key = mobile;
			sql = "select user_id,user_name,email,user_type,qq,msn from user where mobile=?";
		}
		if (StringUtil.isEmpty(key)) {
			return null;
		}
		name = name.replaceAll("⊙", "");
		String password = StringUtil.createPassword(6);
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Do, mobile),
				SecurityObject.create(SecurityType.Name, SecurityMode.Do, qq), SecurityObject.create(SecurityType.Name, SecurityMode.Do, name),
				SecurityObject.create(SecurityType.UserPassword, SecurityMode.Do, password), SecurityObject.create(SecurityType.Name, SecurityMode.Do, msn),
				SecurityObject.create(SecurityType.Email, SecurityMode.Do, email));
		mobile = values[0];
		qq = values[1];
		password = values[3];
		msn = values[4];
		email = values[5];
		if (values[5].length() == 0) {
			key = mobile;
		} else {
			key = values[5];
		}
		BasicDBObject oUser = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(key) }, 0, 0);
		if (oUser == null) {
			UserRole role = new UserRole();
			BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, mobile);
			sql = "insert into user(user_name,password,email,role,user_type,mobile,qq,msn,sex,city,server)values(?,?,?,b?,?,?,?,?,?,?,?)";
			int sex = 1;
			if (name.indexOf("女士") >= 0 || name.indexOf("小姐") >= 0) {
				sex = 0;
			}
			long userId = DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
					new String[] { name, password, email, role.toString(), String.valueOf(UserType.Create.ordinal()), mobile, qq, msn, String.valueOf(sex),
							city, rec.getString("id") });
			oUser = new BasicDBObject().append("user_id", userId);
			oUser.append("user_name", name);
			oUser.append("user_type", UserType.Create.ordinal());
		} else {
			if (StringUtil.isEmpty(oUser.getString("qq")) && !StringUtil.isEmpty(qq)) {
				sql = "update user set qq=? where user_id=?";
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { qq, oUser.getString("user_id") });
			}
			if (StringUtil.isEmpty(oUser.getString("msn")) && !StringUtil.isEmpty(msn)) {
				sql = "update user set msn=? where user_id=?";
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { msn, oUser.getString("user_id") });
			}
		}
		oUser.append("contact_person", values[2]);
		oUser.append("mobile", mobile);
		oUser.append("qq", qq);
		oUser.append("msn", msn);
		return oUser;
	}

	protected BasicDBObject readUser(long userId) {
		String sql = "select mobile,qq,msn from user where user_id=?";
		BasicDBObject oUser = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(userId) }, 0, 0);
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Undo, oUser.getString("mobile")),
				SecurityObject.create(SecurityType.Name, SecurityMode.Do, oUser.getString("qq")),
				SecurityObject.create(SecurityType.Name, SecurityMode.Do, oUser.getString("msn")));
		oUser.append("mobile", values[0]);
		oUser.append("qq", values[1]);
		oUser.append("msn", values[2]);
		return oUser;
	}

	protected void resetUserCount(long userId) {
		String sql = "update user set service_count=0 where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(userId) });
	}

	protected BasicDBObject findPhotoByUrl(String url, BasicDBList photos) {
		for (int i = 0; i < photos.size(); i++) {
			BasicDBObject oPhoto = (BasicDBObject) photos.get(i);
			if (oPhoto.getString("url").equalsIgnoreCase(url)) {
				return oPhoto;
			}
		}
		return null;
	}

	protected String[] findCountryArea(String content) {
		if (countries == null) {
			trieCountryDict();
		}
		String country = "";
		String area = "";
		int index = 0;
		while (index < content.length()) {
			if (!StringUtil.isEmpty(country) && !StringUtil.isEmpty(area)) {
				break;
			}
			String key = String.valueOf(content.charAt(index));
			ArrayList<String> matches = null;
			int mode = 1;
			if (StringUtil.isEmpty(area)) {
				matches = areas.searchPrefix(key, 100);
			}
			if (matches.size() == 0 && StringUtil.isEmpty(country)) {
				matches = countries.searchPrefix(key, 100);
				mode = 0;
			}
			boolean flag = false;
			for (int i = 0; matches.size() > 0 && i < matches.size(); i++) {
				String match = matches.get(i);
				if (index + match.length() > content.length())
					continue;
				String matchSource = content.substring(index, index + match.length());
				if (matchSource.equalsIgnoreCase(match)) {
					if (mode == 0) {
						country = matchSource;
					} else {
						area = matchSource;
						String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select country from country_dict where area=?",
								new String[] { area }, 0, 0);
						country = rows[0][0];
					}
					flag = true;
					index += match.length();
				}
			}
			matches.clear();
			if (flag == true)
				continue;
			index++;
		}
		if (StringUtil.isEmpty(country) && StringUtil.isEmpty(area)) {
			return null;
		}
		return new String[] { country, area };
	}

	protected String trimString(Node node) {
		String content = node.getTextContent();
		return content.replaceAll("地区", "").replaceAll("旅游", "").replaceAll("攻略", "");
	}

	protected void deleteSmallImages() {
		int offset = 0;
		int size = 10;
		String sql = "select url,content,user_id from mafengwo_spider where bizName='trip' and user_id!=0";
		String[][] rows = DataSet.query("x2", Const.defaultMysqlDB, sql, offset, size);
		while (rows != null && rows.length > 0) {
			for (int i = 0; i < rows.length; i++) {
				String url = rows[i][0];
				String content = rows[i][1];
				BasicDBObject oTrip = (BasicDBObject) JSON.parse(content);
				BasicDBList photos = (BasicDBList) oTrip.get("photos");
				long userId = Long.parseLong(rows[i][2]);
				System.out.print("Url=" + url);
				int t = 0;
				while (t < photos.size()) {
					BasicDBObject oPhoto = (BasicDBObject) photos.get(t);
					String fileName = oPhoto.getString("fileName").replaceAll("e:", "/server/backup");
					if (isSmallPicture(userId, fileName)) {
						System.out.println(fileName);
						photos.remove(t);
						continue;
					}
					t++;
				}
				DataSet.update("x2", Const.defaultMysqlDB, "update mafengwo_spider set upload=? where url=?", new String[] { photos.toString(), url });
				System.out.println("&userId=" + userId + "&Count=" + photos.size());
			}
			offset += size;
			rows = DataSet.query("x2", Const.defaultMysqlDB, sql, offset, size);
		}

	}

	protected void removeAllImages() {
		for (int i = 20; i <= 300; i++) {
			removeAllImages(i);
		}
	}

	protected void removeAllImages(long userId) {
		String q = "^" + userId + "~.*";
		pictureFS.removeAllFile(q);
		shotFS.removeAllFile(q);
		String server = ServiceFactory.user().userRecordByCache(userId).server;
		DataCollection.remove(server, "pictures", "i" + userId, new BasicDBObject());
		System.out.println("UserId=" + userId);
	}

}
