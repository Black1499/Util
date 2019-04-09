import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCUtil {

    public static String DRIVER = "com.mysql.jdbc.Driver";
    public static String URL = "jdbc:mysql://localhost:3306/schooldb";
    public static String USER_NAME = "admin";
    public static String PASSWORD = "123456";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private JDBCUtil() {

    }

    /**
     * Get connection
     * 
     * @return
     */
    public static Connection getconnnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Close connection
     * 
     * @param rs
     * @param st
     * @param con
     */
    public static void close(ResultSet rs, Statement st, Connection con) {
        try {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (st != null) {
                        st.close();
                    }
                } finally {
                    if (con != null)
                        con.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close connection
     * 
     * @param rs
     */
    public static void close(ResultSet rs) {
        Statement st = null;
        Connection con = null;
        try {
            try {
                if (rs != null) {
                    st = rs.getStatement();
                    rs.close();
                }
            } finally {
                try {
                    if (st != null) {
                        con = st.getConnection();
                        st.close();
                    }
                } finally {
                    if (con != null) {
                        con.close();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close connection
     * 
     * @param st
     * @param con
     */
    public static void close(Statement st, Connection con) {
        try {
            try {
                if (st != null) {
                    st.close();
                }
            } finally {
                if (con != null)
                    con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert/update/delete
     * 
     * @param sql
     * @param args
     * @return
     */
    public static int update(String sql, Object... args) {
        int result = 0;
        Connection con = getconnnection();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject((i + 1), args[i]);
                }
            }
            result = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps, con);
        }

        return result;
    }

    /**
     * query, because need to manually close the resource, so not recommended
     * for use it
     * 
     * @param sql
     * @param args
     * @return ResultSet
     */
    @Deprecated
    public static ResultSet query(String sql, Object... args) {
        ResultSet result = null;
        Connection con = getconnnection();
			ps = null;
        try {
            ps = con.prepareStatement(sql);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject((i + 1), args[i]);
                }
            }
            result = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Query a single record
     * 
     * @param sql
     * @param args
     * @return Map<String,Object>
     */
    public static Map<String, Object> queryForMap(String sql, Object... args) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> list = queryForList(sql, args);
        if (list.size() > 0) {
            result = list.get(0);
        }
        return result;
    }

    /**
     * Query a single record
     * 
     * @param sql
     * @param args
     * @return <T>
     */
    public static <T> T queryForObject(String sql, Class<T> clz, Object... args) {
        T result = null;
        List<T> list = queryForList(sql, clz, args);
        if (list.size() > 0) {
            result = list.get(0);
        }
        return result;
    }

    /**
     * Query a single record
     * 
     * @param sql
     * @param args
     * @return List<Map<String,Object>>
     */
    public static List<Map<String, Object>> queryForList(String sql, Object... args) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = getconnnection();
            ps = con.prepareStatement(sql);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject((i + 1), args[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    map.put(rsmd.getColumnLabel(i), rs.getObject(i));
                }
                result.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, con);
        }
        return result;
    }

    /**
     * Query a single record
     * 
     * @param sql
     * @param args
     * @return List<T>
     */
    public static <T> List<T> queryForList(String sql, Class<T> clz, Object... args) {
        List<T> result = new ArrayList<T>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getconnnection();
            ps = con.prepareStatement(sql);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject((i + 1), args[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                T obj = clz.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
					//获得当前列的列名
					Filed filed=clazz.getDeclaredField(columnName);
					//设置字段可以访问
					filed.setAccessible(true);
					//给字段赋值
					filed.set(obj,rs.getObject(columnName));
                    /*String methodName = "set" + columnName.substring(0, 1).toUpperCase()
                            + columnName.substring(1, columnName.length());
                    Method method[] = clz.getMethods();
                    for (Method meth : method) {
                        if (methodName.equals(meth.getName())) {
                            meth.invoke(obj, rs.getObject(i));
                        }
                    }*/
                }
                result.add(obj);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, con);
        }
        return result;
    }
	
	
	/**
     * 根据数据库的连接参数，获取指定表的基本信息：字段名、字段类型、字段注释
     *            表名
     * @return Map集合
     */
    public static List getTableInfo(String table) {
        List result = new ArrayList();

        Connection conn = null;
        DatabaseMetaData dbmd = null;

        try {
            conn = DriverManager.getConnection(URL, USER_NAME, PASSWORD);

            dbmd = conn.getMetaData();
            ResultSet resultSet = dbmd.getTables(null, "%", table, new String[] { "TABLE" });

            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                System.out.println(tableName);

                if (tableName.equals(table)) {
                    ResultSet rs = conn.getMetaData().getColumns(null, getSchema(conn), tableName.toUpperCase(), "%");

                    while (rs.next()) {
                        // System.out.println("字段名："+rs.getString("COLUMN_NAME")+"--字段注释："+rs.getString("REMARKS")+"--字段数据类型："+rs.getString("TYPE_NAME"));
                        Map map = new HashMap();
                        String colName = rs.getString("COLUMN_NAME");
                        map.put("code", colName);

                        String remarks = rs.getString("REMARKS");
                        if (remarks == null || remarks.equals("")) {
                            remarks = colName;
                        }
                        map.put("name", remarks);

                        String dbType = rs.getString("TYPE_NAME");
                        map.put("dbType", dbType);

                        map.put("valueType", changeDbType(dbType));
                        result.add(map);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static String changeDbType(String dbType) {
        dbType = dbType.toUpperCase();
        switch (dbType) {
        case "VARCHAR":
        case "VARCHAR2":
        case "CHAR":
            return "1";
        case "NUMBER":
        case "DECIMAL":
            return "4";
        case "INT":
        case "SMALLINT":
        case "INTEGER":
            return "2";
        case "BIGINT":
            return "6";
        case "DATETIME":
        case "TIMESTAMP":
        case "DATE":
            return "7";
        default:
            return "1";
        }
    }

    // 其他数据库不需要这个方法 oracle和db2需要
    private static String getSchema(Connection conn) throws Exception {
        String schema;
        schema = conn.getMetaData().getUserName();
        if ((schema == null) || (schema.length() == 0)) {
            throw new Exception("ORACLE数据库模式不允许为空");
        }
        return schema.toUpperCase().toString();
    }
}