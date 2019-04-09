package com.util;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.omg.CORBA.portable.InputStream;

public class DBUtil {
	final private static String driver="com.microsoft.sqlserver.jdbc.SQLServerDriver";
	final private static String url="jdbc:sqlserver://localhost:1433;DatabaseName=hotelDB";
	final private static String uid="sa";
	final private static String pwd="123456";
	private static Connection conn=null;
	private static Statement stmt=null;
	private static PreparedStatement pst=null;
	/*
	 * 打开数据库链接
	 */
	public static void openConn(){
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url,uid,pwd);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("数据库已连接");
	}
	/*
	 * 关闭数据库连接
	 */
	public static void closeConn(){
		try {
			if(conn!=null)
				conn.close();
			if(pst!=null)
				pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/*
	 * 获得连接对象
	 */
	public static Connection getConn(){
		try {
			if(conn==null|| conn.isClosed())
				openConn();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	/*
	 * 查询返回数据集对象
	 */
	public static ResultSet executeQuery(String sql){
		try {
			openConn();
			pst=conn.prepareStatement(sql);
			return pst.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/*
	 * 增删改返回执行成功记录数
	 */
	public static int excuteUpdate(String sql){
		try {
			openConn();
			pst=conn.prepareStatement(sql);
			return pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	/*
	 * 查询返回数据集对象
	 */
	public static ResultSet excuteQuery(String sql,Object[] o){
		try {
			openConn();
			pst=conn.prepareStatement(sql);
			for(int i=0;i<o.length;i++)
				pst.setObject(i+1, o[i]); 
			return pst.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/*
	 * 增删改返回执行成功记录数
	 */
	public static int excuteUpdate(String sql,Object[] o){
		try {
			openConn();
			pst=conn.prepareStatement(sql);
			for(int i=0;i<o.length;i++)
				pst.setObject(i+1, o[i]);
			return pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	/*
	 * 调用存储过程
	 */
	public static Object callIn(String procName,Object[] o){		
		try {
			openConn();
			procName="{call "+procName+"(";
			String link="";
			for(int i=0;i<o.length;i++){
				procName+=link+"?";
				link=",";
			}
			procName+=")}";
			CallableStatement cst=conn.prepareCall(procName);
			for(int i=0;i<o.length;i++)
				cst.setObject(i+1, o[i]);
			
			if(cst.execute())
				return cst.getResultSet();
			else
				return cst.getUpdateCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/*
	 * 调用存储过程，并有输出参数
	 * @procName ，存储过程名称：proc_Insert(?,?)
	 * @in ,输入参数集合
	 * @output,输出参数集合
	 * @type,输出参数类型集合
	 * */
	public static int executeOutputProcedure(String procName,Object[] in,Object[] output,int[] type){
		int result = 0;
		try {
			CallableStatement cstmt = conn.prepareCall("{call "+procName+"}");
			//设置存储过程的参数值
			int i=0;
			for(;i<in.length;i++){//设置输入参数
				cstmt.setObject(i+1, in[i]);
				//print(i+1);
			}
			int len = output.length+i;
			for(;i<len;i++){//设置输出参数
				cstmt.registerOutParameter(i+1,type[i-in.length]);
				//print(i+1);
			}
			cstmt.execute();
			//获取输出参数的值
			for(i=in.length;i<output.length+in.length;i++)
				output[i-in.length] = cstmt.getObject(i+1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}
