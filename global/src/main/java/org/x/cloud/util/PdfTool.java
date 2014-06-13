package org.x.cloud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.x.cloud.dict.Global;

import com.mongodb.BasicDBObject;

public class PdfTool {

	public static BasicDBObject createFor(String url, String pdfFileName) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String execFile = Global.PluginPath + "wkhtmltox/bin/wkhtmltopdf";
		File f = new File(execFile);
		if (f.exists() == false) {
			return oResult.append("message", execFile + ",not exist.");
		}
		pdfFileName=Global.PdfPath + pdfFileName;
		StringBuilder cmd = new StringBuilder();
		cmd.append(execFile);
		cmd.append(" -s Letter ");
		cmd.append(" -B 10 ");
		cmd.append(" -L 10 ");
		cmd.append(" -R 10 ");
		cmd.append(" -T 10 ");
		cmd.append(" -l ");
		// cmd.append(" --disable-external-links ");
		// cmd.append(" --disable-internal-links ");
		cmd.append(" --disable-javascript ");
		cmd.append(" --disable-plugins ");
		cmd.append(" --disable-forms ");
		cmd.append(" --load-error-handling ignore ");
		cmd.append(url + " ");
		cmd.append(pdfFileName);
		StringBuffer output = new StringBuffer();
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			p.waitFor();
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			int exit = p.exitValue();
			if (exit == 0) {
				output.setLength(0);
				output.append("ok");
			}
			p.destroy();
			oResult.append("fileName", pdfFileName);
			return oResult.append("xeach", true).append("message", output.toString());
		} catch (Exception e) {
			oResult.append("message", e.getMessage());
		}
		return oResult;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String pdfFileName = "order.pdf";
		System.out.println(PdfTool.createFor("http://www.yiqihi.com/order/5&pdf=true&cookieId=9ea55b2f-4ccf-45c9-8fea-c4782aedc0aa", pdfFileName));
		System.out.println("done");
	}

}
