package cn.rp.util;

import java.sql.*;
import java.util.Map;
import java.util.Set;

import static java.lang.Class.forName;

/**
 * @author Rp
 * @date 2026.6.10 07:09
 **/
public class DorisDBUtil {

    public static void saveData(String tableName, Map<String, Object> data, Set<String> intFieldSet, String driverClassName, String username, String password) {
        try {

            String resultsql = "insert into";
            resultsql += tableName + "(";
            String valuesql = "(";

            Set<Map.Entry<String, Object>> sets = data.entrySet();

            for (Map.Entry<String, Object> entry : sets) {
                String fieldName = entry.getKey();
                Object valueString = entry.getValue();
                resultsql += fieldName + ",";
                if (intFieldSet.contains(fieldName)) {
                    valuesql += valueString + ",";
                } else {
                    valuesql += "'" + valueString + "',";
                }
            }
            resultsql = resultsql.substring(0, resultsql.length() - 1) + ")";
            valuesql = valuesql.substring(0, valuesql.length() - 1) + ")";
            resultsql = resultsql +" values "+valuesql;
            System.out.println("dorisDB 插入sql = " + resultsql);


            Connection connection = getConnection("jdbc:mysql://192.168.88.101:9030/hadoop_db", driverClassName, username, password);

            Statement statement = connection.createStatement();
            statement.execute(resultsql);
        } catch (SQLException e) {
            e.printStackTrace();        }
    }


    public static Connection getConnection(String address, String driverClassName, String username, String password) {
        try {
            forName(driverClassName);
            Connection connection = DriverManager.getConnection(address, username, password);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public static Connection getConnection(String address){
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String username = "hadoop";
        String password = "hadoop";
        return getConnection(address, driverClassName, username, password);
    }

    public static ResultSet quereBySql(String sql) throws SQLException {
        String address = "jdbc:mysql://192.168.88.101:9030/hadoop_db";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String username = "hadoop";
        String password = "hadoop";
        return quereBySql(address, driverClassName, username, password, sql);
    }

    public static ResultSet quereBySql(String address, String driverClassName, String username, String password, String sql) throws SQLException {
        Connection connection = getConnection(address, driverClassName, username, password);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        return resultSet;

    }
}