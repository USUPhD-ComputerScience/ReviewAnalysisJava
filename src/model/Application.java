package model;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.PostgreSQLConnector;

public class Application implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2497544910820359076L;
	private Map<String, Review> reviewMap;
	private Set<Long> updateDates;
	private int dbID;

	/**
	 * Add a review to this Application
	 * 
	 */
	public Review addReview(Review rev) {
		Review review = reviewMap.get(rev.getReviewId().intern());
		if (review == null)
			return reviewMap.put(rev.getReviewId().intern(), rev);
		return null;
	}

	public void writeSentenceToFile(PrintWriter fileWriter, long lastUpdate,
			PostgreSQLConnector db) {

		// String fields[] = { "title", "text" };
		for (Review review : reviewMap.values()) {
			if (review.getSentenceList().isEmpty())
				continue;
			try {
				review.writeSentenceToFile(fileWriter, lastUpdate);
				// if (review.getCreationTime() > lastUpdate) {
				// String condition = "appid=" + this.getDbID()
				// + " AND reviewid='" + review.getReviewId() + "'";
				// ResultSet results;
				// results = db.select(PostgreSQLConnector.REVIEWS_TABLE,
				// fields,
				// condition);
				// while (results.next()) {
				// String text = results.getString("text");
				// if (text.indexOf('\t') < 0) // Not from Android Market
				// text = results.getString("title") + "." + text;
				// fileWriter.println(text);
				// break;
				// }
				// }

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}
	}

	public boolean contains(String reviewID) {
		return reviewMap.containsKey(reviewID);
	}

	public int getDbID() {
		return dbID;
	}

	public Application(int dbID) {
		this.dbID = dbID;
		reviewMap = new HashMap<>();
		updateDates = new HashSet<>();
	}

	/**
	 * Add a new update date for this application
	 * 
	 * @param updateDate
	 *            - Date format of MMM dd,yyy
	 * @return the type Long version of the date, or throw ParseException in
	 *         case of wrong format.
	 */
	public long addAnUpdateDate(String updateDate) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat("MMM dd,yyyy");
		Date date;
		date = (Date) f.parse(updateDate);
		long update = date.getTime();
		updateDates.add(update);
		return update;
	}

	public List<Review> getReviews() {
		return new ArrayList<Review>(reviewMap.values());
	}
}
