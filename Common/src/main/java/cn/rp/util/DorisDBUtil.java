package cn.rp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

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


            Connection connection = getConnection("jdbc:mysql://192.168.88.101:9030/dorisTest", driverClassName, username, password);

            Statement statement = connection.createStatement();
            statement.execute(resultsql);
        } catch (SQLException e) {
            e.printStackTrace();        }
    }


    public static Connection getConnection(String address, String driverClassName, String username, String password) {
        try {
            Class.forName(driverClassName);
            Connection connection = DriverManager.getConnection(address, username, password);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}