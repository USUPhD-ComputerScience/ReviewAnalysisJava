package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class PostgreSQLConnector {
	private Connection mConnect = null;
	private Statement mStatement = null;
	public String mDatabase = "";
	public static final String DBLOGIN = "postgres";
	public static final String DBPASSWORD = "phdcs2014";
	public static final String REVIEWDB = "reviewdb";
	public static final String APPID_TABLE = "appid"; // name, ID, gplay, amarket
	public static final String REVIEWS_TABLE = "reviews"; // text, title, appid,
	public static final String DESC_TABLE = "description"; // description, version, appid

	public PostgreSQLConnector(String user, String password, String database) {
		// TODO Auto-generated constructor stub
		try {

			String url = "jdbc:postgresql://localhost/" + database;

			mConnect = DriverManager.getConnection(url, user, password);
			// statements allow to issue SQL queries to the database
			mStatement = mConnect.createStatement();
			mDatabase = database;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// if you dont have any condition, just pass null into it
	public ResultSet select(String table, String fields[], String condition)
			throws SQLException {
		String field = fields[0];
		for (int i = 1; i < fields.length; i++) {
			field = field + "," + fields[i];
		}
		if (condition == null)
			return mStatement.executeQuery("select " + field + " from  "
					+ table);
		else
			return mStatement.executeQuery("select " + field + " from  "
					+ table + " WHERE " + condition);
	}

	public void update(String table, String updatefield, String condition) {
		// preparedStatements can use variables and are more efficient
		if (condition != null)
			try {
				mStatement.executeUpdate("update " + table + " set  "
						+ updatefield + " WHERE " + condition);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	// values: an array of values, must be in the same order as in the table
	// all values are String
	public int insert(String table, String values[]) throws SQLException {
		// preparedStatements can use variables and are more efficient
		String dumbValues = "";
		for (int i = 0; i < values.length; i++) {
			dumbValues += ",?";
		}
		PreparedStatement preparedStatement = mConnect
				.prepareStatement("insert into  " + table + " values (default"
						+ dumbValues + ")");
		// parameters start with 1

		for (int i = 0; i < values.length; i++) {
			try {
				int intValue = Integer.parseInt(values[i]);
				preparedStatement.setInt(i + 1, intValue);
			} catch (NumberFormatException e) {
				try {
					long longValue = Long.parseLong(values[i]);
					preparedStatement.setLong(i + 1, longValue);
				} catch (NumberFormatException ex) {
					preparedStatement.setString(i + 1, values[i]);
				}
			}
		}
		preparedStatement.executeUpdate();
		int id = -1;
		ResultSet rs = preparedStatement.getGeneratedKeys();
		if (rs.next()) {
			id = rs.getInt(1);
		}
		preparedStatement.close();
		return id; // only return the first
	}

	// you need to close all three to make sure
	public void close() {
		try {
			if (mStatement != null)
				mStatement.close();
			if (mConnect != null)
				mConnect.close();
		} catch (Exception e) {
			// don't throw now as it might leave following closables in
			// undefined state
		}
	}
}