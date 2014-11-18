package org.x.server.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gome.totem.sniper.util.FileUtil;

public class DExcel {
	
	private static final Logger logger = LoggerFactory.getLogger(DExcel.class);	
	private POIFSFileSystem fs;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HSSFRow row;
	private FileOutputStream fos = null;  
	public FileOutputStream getFos() {
		return fos;
	}

	private HSSFCell cell = null;
	private HSSFCellStyle cellType = null;  

	public HSSFSheet readSheet(String excelName, String sheetName){
		File file = FileUtil.createFile(excelName, true);
		try {
			this.setFos(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		}
		this.wb = new HSSFWorkbook();
		setCellType(this.wb.createCellStyle());
		this.cellType.setBorderTop(HSSFCellStyle.BORDER_THIN);  
		this.cellType.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
		this.cellType.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
		this.cellType.setBorderRight(HSSFCellStyle.BORDER_THIN);  
		this.cellType.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
		this.cellType.setFillForegroundColor(HSSFColor.GOLD.index);  
		this.cellType.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND); 
		
		this.sheet = this.wb.createSheet(sheetName);
		return this.sheet;
	}
	
	public void writeCell(int r, int c, String content){
		row = sheet.createRow(r);  
		if(c > Short.MAX_VALUE){
			logger.error("列号太大了！ 列号最大为 " + Short.MAX_VALUE);
			return;
		}
		setCell(row.createCell((short)c));
		this.cell.setCellStyle(cellType);
		this.cell.setCellValue(content);
	}
	
	/**
	 * 读取Excel表格表头的内容
	 * 
	 * @param InputStream
	 * @return String 表头内容的数组
	 */
	public String[] readExcelTitle(String excelName, int sheetPos) {
		InputStream is;
		try {
			is = new FileInputStream(excelName);
			fs = new POIFSFileSystem(is);
			wb = new HSSFWorkbook(fs);
		} catch (FileNotFoundException e1) {
			logger.error("{} is not found!", excelName);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		sheet = wb.getSheetAt(sheetPos);
		row = sheet.getRow(0);
		// 标题总列数
		int colNum = row.getPhysicalNumberOfCells();
		
		String[] title = new String[colNum];
		for (int i = 0; i < colNum; i++) {
			title[i] = getCellFormatValue(row.getCell((short) i));
		}
		return title;
	}

	/**
	 * 读取Excel数据内容
	 * 
	 * @param InputStream
	 * @return Map 包含单元格数据内容的Map对象
	 */
	public Map<Integer, String> readExcelContent(String excelName, int sheetPos) {
		InputStream is = null;
		try {
			is = new FileInputStream(excelName);
		} catch (FileNotFoundException e1) {
			logger.error("{} is not found!", excelName);
			return null;
		}
		Map<Integer, String> content = new HashMap<Integer, String>();
		String str = "";
		try {
			fs = new POIFSFileSystem(is);
			wb = new HSSFWorkbook(fs);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		sheet = wb.getSheetAt(sheetPos);
		// 得到总行数
		int rowNum = sheet.getLastRowNum();
		int colNum = row.getPhysicalNumberOfCells();
		
		for (int i = 0; i <= rowNum; i++) {
			row = sheet.getRow(i);
			int j = 0;
			while (j < colNum) {
				str += getCellFormatValue(row.getCell((short) j)).trim() + "	";
				j++;
			}
			content.put(i, str);
			str = "";
		}
		return content;
	}

	/**
	 * 根据HSSFCell类型设置数据
	 * 
	 * @param cell
	 * @return
	 */
	private String getCellFormatValue(HSSFCell cell) {
		String cellvalue = "";
		if (cell != null) {
			// 判断当前Cell的Type
			switch (cell.getCellType()) {
			// 如果当前Cell的Type为NUMERIC
			case HSSFCell.CELL_TYPE_NUMERIC:
			case HSSFCell.CELL_TYPE_FORMULA: {
				// 判断当前的cell是否为Date
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					cellvalue = sdf.format(date);
				}else {
					// 取得当前Cell的数值
					cellvalue = String.valueOf(cell.getNumericCellValue());
				}
				break;
			}
			// 如果当前Cell的Type为STRIN
			case HSSFCell.CELL_TYPE_STRING:
				// 取得当前的Cell字符串
				cellvalue = cell.getStringCellValue();
				break;
			// 默认的Cell值
			default:
				cellvalue = " ";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue;
	}
	
	public static void main(String[] args) {
		// 对读取Excel表格标题测试
		DExcel excelReader = new DExcel();
		String[] title = excelReader.readExcelTitle("/server/conf/2222.xlsx", 0);
		System.out.println("获得Excel表格的标题:");
		for (String s : title) {
			System.out.print(s + " ");
		}

		// 对读取Excel表格内容测试
		Map<Integer, String> map = excelReader.readExcelContent("/server/conf/2222.xlsx", 0);
		System.out.println("获得Excel表格的内容:");
		for (int i = 0;map != null && i < map.size(); i++) {
			System.out.println(map.get(i));
		}
	}

	public HSSFCellStyle getCellType() {
		return cellType;
	}

	public void setCellType(HSSFCellStyle cellType) {
		this.cellType = cellType;
	}

	public void setCell(HSSFCell cell) {
		this.cell = cell;
	}

	public void setFos(FileOutputStream fos) {
		this.fos = fos;
	}
}
