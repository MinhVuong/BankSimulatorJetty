package vn._123pay.bank;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import vn._123pay.Result;
import vn._123pay.ResultCode;
import vn._123pay.configuration.Config;
import vn._123pay.data.dbms.relational.OracleClient;
import vn._123pay.data.dbms.relational.RDbClient;
import vn._123pay.data.dbms.relational.RDbClientManager;
import vn._123pay.data.dbms.relational.RDbClientName;
import vn._123pay.data.json.JSONArray;
import vn._123pay.util.ExceptionUtil;

/**
 *
 * @author vutt3
 */
public class Banks {

    public static Result<JSONArray> get(String merchantCode, StringBuilder logBuilder) {

        RDbClientName dbClientName = RDbClientName.Oracle;
        RDbClient dbClient = RDbClientManager.Instance.get(dbClientName);
        if (dbClient == null) {
            logBuilder.append(dbClientName.toString())
                    .append("RDbClient=null;");

            return new Result<>(ResultCode.SystemError, null);
        }

        String sp = "{call SP_MI_GET_BANK_BY_MERCHANT(?,?,?,?)}";
        try (Connection conn = dbClient.getConnection()) {
            try (CallableStatement cs = conn.prepareCall(sp)) {
                cs.setString(1, merchantCode);
                cs.setString(2, "desktop_web_1");
                cs.registerOutParameter(3, Types.NUMERIC); //oStatus
                cs.registerOutParameter(4, -10); // Cursor

                cs.executeUpdate();

                int dbRetCode = cs.getInt(3);
                if (dbRetCode != 1) {
                    logBuilder.append("SP_MI_GET_BANK_BY_MERCHANT return ")
                            .append(Integer.toString(dbRetCode))
                            .append(';');

                    if (dbRetCode == -10) {
                        return new Result<>(ResultCode.NotFound);
                    }

                    return new Result<>(ResultCode.SystemError);
                }

                try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                    if (!rs.next()) {
                        return new Result<>(ResultCode.Success,
                                JSONArray.fromJSONString("[]"),
                                null
                        );
                    }

                    StringBuilder sb = new StringBuilder("[");
                    do {
                        sb.append("{\"code\":\"")
                                .append(rs.getString("sub_bank"))
                                .append("\",\"name\":\"")
                                .append(rs.getString("description"))
                                .append("\",\"status\":")
                                .append(rs.getInt("status"))
                                .append("},");
                    } while (rs.next());

                    sb.deleteCharAt(sb.length() - 1);
                    sb.append(']');

                    return new Result<>(ResultCode.Success,
                            JSONArray.fromJSONString(sb.toString()),
                            null
                    );
                }
            }

        } catch (SQLException ex) {
            logBuilder.append("SP_MI_GET_BANK_BY_MERCHANT")
                    .append(ExceptionUtil.Instance.getAsOneLineString(ex))
                    .append(';');

            return new Result<>(ResultCode.SystemError);
        }
    }

    public static void main(String... args) {
        try {
            RDbClient oracle = new OracleClient(Config.get("order-oracle-url"),
                    Config.get("order-oracle-uId"),
                    Config.get("order-oracle-uPwd"),
                    null,
                    null);

            RDbClientManager.Instance.addClient(RDbClientName.Oracle, oracle);
            StringBuilder logBuilder = new StringBuilder();
            Banks b = new Banks();
            System.out.println(b.get("123ALO", logBuilder).getData());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
