package de.agiehl.watcher.bgg.usercollection;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Application {

	private static Logger log = Logger.getLogger(Application.class.getName());

	private static class User {
		public String userId;
		public String userName;
		public String urlUserName;
		public String realName;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		String baseUrl = "https://boardgamegeek.com/users/page/%d?country=Germany";

		int pageCount = 1;
		int lastPage = 10;
		List<User> userList = new ArrayList<>();

		for (; pageCount <= lastPage; pageCount++) {
			try {
				Document document = Jsoup.connect(format(baseUrl, pageCount)).userAgent("BGG User List Generator")
						.get();

				Element first = document.select("a[title='last page']").first();
				if (first != null) {
					String lastPageStr = first.text().replaceAll("^.|.$", "");
					lastPage = Integer.parseInt(lastPageStr);
				}

				Elements users = document.select("div[class*='avatarblock']");
				for (Element e : users) {
					User user = new User();
					user.userId = e.attr("data-userid");
					user.userName = e.attr("data-username");
					user.urlUserName = e.attr("data-urlusername");
					user.realName = e.child(0).text();
					userList.add(user);
				}

			} catch (Exception e) {
				log.log(Level.SEVERE, String.format("Error while watching: %d", pageCount), e);
			}

			Thread.sleep(1000);
		}

		String filename = args.length >= 1 ? args[0] : "";
		if (filename.isEmpty()) {
			filename = "userList.csv";
		}

		File file = new File(filename);
		FileWriter out = new FileWriter(file, UTF_8);
		CSVPrinter printer = new CSVPrinter(out,
				CSVFormat.DEFAULT.withHeader("userId", "username", "urlUserName", "realName"));

		userList.forEach(u -> {
			try {
				printer.printRecord(u.userId, u.userName, u.urlUserName, u.realName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		printer.close();

		System.out.println(String.format("%s wurde geschrieben", file.getAbsolutePath()));

	}

}
