package org.x.server.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ISearchManage {
	public void beansTalkMonitor(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException;
	public void msgSend(String massage);
	public void appsDoc(HttpServletRequest req, HttpServletResponse resp) throws IOException ;
	public void promoProductFullWeight(HttpServletRequest req, HttpServletResponse resp) throws IOException ;
	public void promoProductFull(HttpServletRequest req, HttpServletResponse resp) throws IOException ;
	public void promoSingle(HttpServletRequest req, HttpServletResponse resp);
}
