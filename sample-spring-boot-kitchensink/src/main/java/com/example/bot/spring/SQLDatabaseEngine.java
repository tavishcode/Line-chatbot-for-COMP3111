package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {
	@Override
	String search(String text) throws Exception {
		//Write your code here
		PreparedStatement stmt = getConnection().prepareStatement(
				"SELECT * FROM messages WHERE ? LIKE concat('%',request,'%')");
		stmt.setString(1, text);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			String response = rs.getString("response");
			try {
				int hit = rs.getInt("hit");
				stmt = getConnection().prepareStatement("UPDATE messages SET hit = ? WHERE response = ?");
				stmt.setInt(1,++hit);
				stmt.setString(2,response);
				stmt.executeUpdate();
				return response + " " + String.valueOf(hit);
			}
			catch (Exception e){
				System.out.println(e);
				stmt = getConnection().prepareStatement("ALTER TABLE messages ADD hit int DEFAULT 0");
				stmt.executeUpdate();
				stmt = getConnection().prepareStatement("UPDATE messages SET hit = ? WHERE response = ?");
				int hit = 1;
				stmt.setInt(1, hit);
				stmt.setString(2, response);
				stmt.executeUpdate();
				return response + " " + String.valueOf(hit);
			}
		}
		return null;
	}
	
	
	private Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);
		
		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}

}
