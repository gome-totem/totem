<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page language="java" import="java.util.Map.Entry"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

ArrayList<ArrayList<String>> searchResult = (ArrayList<ArrayList<String>>)(request.getAttribute("searchResult"));
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'skuSearch.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">

  </head>
  
  <body>

    <div id = "main" align="center">
	   <form action="/cloud/business/SkuSearchServlet" method="post">
   	 <input type="text" name="sku_name" value='<%=request.getAttribute("searchSkuName")%>'>
       <input type="submit" value="搜索">
       <table border ="1" align = "center" style="border-collapse:collapse;">
 			 <tr align="center">
 			 <td>product ID</td>
          <td>sku ID</td>
          <td>sku name</td>
          <td>score</td>
          </tr>
            <%
            	for (ArrayList<String> entry : searchResult) {
            		String productID = entry.get(0);
            		String skuID = entry.get(1);
            		String skuName = entry.get(2);
            		double score = Double.parseDouble(entry.get(3));
            %>
				<tr align="center">
					<td><%=productID%></td>
					<td><%=skuID%></td>
					<td><%=skuName%></td>
					<td><%=score%></td>
				</tr>
				<%
					}
				%>
        </table>
    
    </form>
    </div>
</body>