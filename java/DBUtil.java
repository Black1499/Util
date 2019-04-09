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
	 * �����ݿ�����
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
		System.out.println("���ݿ�������");
	}
	/*
	 * �ر����ݿ�����
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
	 * ������Ӷ���
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
	 * ��ѯ�������ݼ�����
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
	 * ��ɾ�ķ���ִ�гɹ���¼��
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
	 * ��ѯ�������ݼ�����
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
	 * ��ɾ�ķ���ִ�гɹ���¼��
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
	 * ���ô洢����
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
	 * ���ô洢���̣������������
	 * @procName ���洢�������ƣ�proc_Insert(?,?)
	 * @in ,�����������
	 * @output,�����������
	 * @type,����������ͼ���
	 * */
	public static int executeOutputProcedure(String procName,Object[] in,Object[] output,int[] type){
		int result = 0;
		try {
			CallableStatement cstmt = conn.prepareCall("{call "+procName+"}");
			//���ô洢���̵Ĳ���ֵ
			int i=0;
			for(;i<in.length;i++){//�����������
				cstmt.setObject(i+1, in[i]);
				//print(i+1);
			}
			int len = output.length+i;
			for(;i<len;i++){//�����������
				cstmt.registerOutParameter(i+1,type[i-in.length]);
				//print(i+1);
			}
			cstmt.execute();
			//��ȡ���������ֵ
			for(i=in.length;i<output.length+in.length;i++)
				output[i-in.length] = cstmt.getObject(i+1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}
