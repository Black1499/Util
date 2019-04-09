package com.vo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

public class GenEntityMysql {


private String packageOutPath = "com.entity";// ָ��ʵ���������ڰ���·��
private String packageOutName = "com.entity";// ָ��ʵ���������ڰ�������
private String authorName = "";// ��������
private String tablename = null;// ����
private static String dbname = "schooldb";// ����
private String[] colnames; // ��������
private String[] colTypes; // ������������
private int[] colSizes; // ������С����
private boolean f_util = false; // �Ƿ���Ҫ�����java.util.*
private boolean f_sql = false; // �Ƿ���Ҫ�����java.sql.*
// com.mysql.jdbc.Driver jdbc:mysql://localhost:3306/easybuy root 123456
// ���ݿ�����
private static final String URL = "jdbc:mysql://localhost:3306/" + dbname;
private static final String NAME = "admin";
private static final String PASS = "123456";
private static final String DRIVER = "com.mysql.jdbc.Driver";


/*
* ���캯��
*/
public GenEntityMysql() {
		// ��������
		Connection con = null;
		// ��Ҫ����ʵ����ı�
		String sql = "";
		PreparedStatement pStemt = null;
		ResultSetMetaData rsmd = null;
		// �����
		ResultSet rs = null;
		try {
			try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			con = DriverManager.getConnection(URL, NAME, PASS);
			// ѭ����ȡ��ǰ���е����б���
			String str = "SELECT table_name FROM information_schema.tables WHERE table_schema='" + dbname
					+ "' AND table_type='base table'";
			pStemt = con.prepareStatement(str);
			rs = pStemt.executeQuery();
			while (rs.next()) {
				tablename = rs.getString("table_name");
				sql = "select * from " + tablename;
				pStemt = con.prepareStatement(sql);
				rsmd = pStemt.getMetaData();
				int size = rsmd.getColumnCount(); // ͳ����
				colnames = new String[size];
				colTypes = new String[size];
				colSizes = new int[size];
				for (int i = 0; i < size; i++) {
					colnames[i] = rsmd.getColumnName(i + 1);
					colTypes[i] = rsmd.getColumnTypeName(i + 1);

					if (colTypes[i].equalsIgnoreCase("datetime")) {
						f_util = true;
					}
					if (colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")) {
						f_sql = true;
					}
					colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
				}

				String content = parse(colnames, colTypes, colSizes);

				try {
					File directory = new File("");
					// System.out.println("����·����"+directory.getAbsolutePath());
					// System.out.println("���·����"+directory.getCanonicalPath());
					String path = this.getClass().getResource("").getPath();
					String outputPath = directory.getAbsolutePath() + "/src/" + this.packageOutPath.replace(".", "/")
							+ "/" + initcap(tablename) + ".java";
					System.out.println(outputPath);
					FileWriter fw = new FileWriter(outputPath);
					PrintWriter pw = new PrintWriter(fw);
					pw.println(content);
					pw.flush();
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null && !rs.isClosed()) {
					rs.close();
				}
				if (pStemt != null && !pStemt.isClosed()) {
					pStemt.close();
				}
				if (con != null && !con.isClosed()) {
					con.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���ܣ�����ʵ�����������
	 * 
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes, int[] colSizes) {
		StringBuffer sb = new StringBuffer();
		sb.append("package " + this.packageOutName + ";\r\n");
		// �ж��Ƿ��빤�߰�
		if (f_util) {
			sb.append("import java.util.Date;\r\n");
		}
		if (f_sql) {
			sb.append("import java.sql.*;\r\n");
		}
		sb.append("\r\n");
		// ע�Ͳ���
		sb.append("   /**\r\n");
		sb.append("    * " + tablename + " ʵ����\r\n");
		sb.append("    * " + new Date() + " " + this.authorName + "\r\n");
		sb.append("    */ \r\n");
		// ʵ�岿��
		sb.append("\r\n\r\npublic class " + initcap(tablename) + "{\r\n");
		processAllAttrs(sb);// ����
		processAllMethod(sb);// get set����
		sb.append("}\r\n");
		// System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * ���ܣ�������������
	 * 
	 * @param sb
	 */
	private void processAllAttrs(StringBuffer sb) {
		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + colnames[i] + ";\r\n");
		}

	}

	/**
	 * ���ܣ��������з���
	 * 
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {

		for (int i = 0; i < colnames.length; i++) {
			sb.append("\tpublic void set" + initcap(colnames[i]) + "(" + sqlType2JavaType(colTypes[i]) + " "
					+ colnames[i] + "){\r\n");
			sb.append("\tthis." + colnames[i] + "=" + colnames[i] + ";\r\n");
			sb.append("\t}\r\n");
			sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initcap(colnames[i]) + "(){\r\n");
			sb.append("\t\treturn " + colnames[i] + ";\r\n");
			sb.append("\t}\r\n");
		}

	}

	/**
	 * ���ܣ��������ַ���������ĸ�ĳɴ�д
	 * 
	 * @param str
	 * @return
	 */
	private String initcap(String str) {

		char[] ch = str.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}

		return new String(ch);
	}

	/**
	 * ���ܣ�����е���������
	 * 
	 * @param sqlType
	 * @return
	 */
	private String sqlType2JavaType(String sqlType) {

		if (sqlType.equalsIgnoreCase("bit")) {
			return "boolean";
		} else if (sqlType.equalsIgnoreCase("tinyint")) {
			return "byte";
		} else if (sqlType.equalsIgnoreCase("smallint")) {
			return "short";
		} else if (sqlType.equalsIgnoreCase("int")) {
			return "int";
		} else if (sqlType.equalsIgnoreCase("bigint")) {
			return "long";
		} else if (sqlType.equalsIgnoreCase("float")) {
			return "float";
		} else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
				|| sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money")
				|| sqlType.equalsIgnoreCase("smallmoney")) {
			return "double";
		} else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
				|| sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
				|| sqlType.equalsIgnoreCase("text")) {
			return "String";
		} else if (sqlType.equalsIgnoreCase("datetime")) {
			return "Date";
		} else if (sqlType.equalsIgnoreCase("image")) {
			return "Blod";
		} else if (sqlType.equalsIgnoreCase("date")) {
			return "Date";
		}

		return null;
	}

	/**
	 * ���� TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		new GenEntityMysql();

	}

}