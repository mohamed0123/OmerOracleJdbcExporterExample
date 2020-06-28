package omar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

class OmarExporter {
	public static void main(String args[]) {
		String filePath = null;
		String query = null;

		if (args.length == 0)
			// for test
			args = new String[] { "./tmp/test2.txt", " SELECT\r\n" + 
					"        *\r\n" + 
					"    FROM SYS.all_objects where object_type = 'FUNCTION' and owner = 'PDF_EXT'" };

		if (args.length == 2) {
			filePath = args[0];
			query = args[1];
		} else {
			new Exception("parameters must be file path and query");
		}

		Connection con = null;
		try {
			createDirIfNotExist(filePath);
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url = "jdbc:oracle:thin:@10.0.1.6:1521/xlp";
			con = DriverManager.getConnection(url, "pdf_ext", "pdf_ext");
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			String header = createHeaderRow(rs);
			Files.write(Paths.get(filePath), header.getBytes());
			long counter = 0;
			while (rs.next()) {
				Files.write(Paths.get(filePath), createRow(rs).getBytes(), StandardOpenOption.APPEND);
				System.out.println("current row >>> " + ++counter);
			}
			
			sendEmail(con,"<h1 style=\"color: #5e9ca0;\">Dears,</h1> <p>Report Executed Successfully</p> <h2 style=\"color: #2e6c80;\">Best Regards</h2> <h3 style=\"color: #2e6c80;\">  Omar Seliem </h3>");
			System.out.println("<<<<<<<<<<<<<<<<<<<< Done >>>>>>>>>>>>>>>>>>>>");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}

	}
	
	private static void sendEmail(Connection con, String body ) throws Exception {
		String query = "{call automation2.html_email('omar_seliem@siliconexpert.com', "
				+ "'mohamed_hamada@siliconexpert.com', 'OMER SELIEM REPORTS <Ba-SW@siliconexpert.com>'    , "
				+ "'OMER SELIEM REPORTS TITLE', "+(body.length() + 10 )+",?)}"; 
		CallableStatement statement = con.prepareCall(query);  
		statement.setString(1, body);  
		statement.execute();
		System.out.println("email sended");
	}

	private static String createRow(ResultSet rs) throws Exception {
		StringJoiner row = new StringJoiner("\t");
		for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
			row.add(cleanCell(rs.getString(i)));
		}
		return row.toString() + "\r\n";
	}

	private static String createHeaderRow(ResultSet rs) throws Exception {
		StringJoiner row = new StringJoiner("\t");
		for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
			row.add(cleanCell(rs.getMetaData().getColumnLabel(i)));
		}
		return row.toString() + "\r\n";
	}

	private static String cleanCell(String cell) {
		if (cell != null)
			return cell.replace("\n", " ").replace("\"", " ").replace("\r", " ").replace("\r\n", " ").replace("\t",
					" ");
		return " ";
	}

	private static String getCurrentFileDire() throws Exception {
		return new File(OmarExporter.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
	}

	private static void createDirIfNotExist(String filePath) throws Exception {
		if (filePath.contains("\\"))
			filePath = filePath.replaceAll("\\", "/");
		if (filePath.contains("/"))
			filePath = filePath.substring(0, filePath.lastIndexOf("/"));
		if (filePath.startsWith("./"))
			filePath = getCurrentFileDire();
		Files.createDirectories(Paths.get(filePath));
	}

}