package model;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Application implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2497544910820359076L;
	private Map<String, Review> reviewMap;
	private Set<Long> updateDates;
	private String appID;
	private int dbID;
	
	@Override
	public int hashCode() {
		return appID.hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (this == arg0)
			return true;
		if (!(arg0 instanceof Application))
			return false;
		Application obj = (Application) arg0;
		if (this.appID.equals(obj.appID))
			return true;
		return false;
	}

	public String getAppID() {
		return appID;
	}

	/**
	 * Add a review to this Application
	 * 
	 */
	public Review addReview(Review rev) {
		Review review = reviewMap.get(rev.getReviewId());
		if (review == null)
			return reviewMap.put(rev.getReviewId(), rev);
		return null;
	}

	public void writeSentenceToFile(PrintWriter fileWriter, long lastUpdate) {
		for(Review review: reviewMap.values()){
			review.writeSentenceToFile(fileWriter, lastUpdate);
		}
	}

	public boolean contains(String reviewID) {
		return reviewMap.containsKey(reviewID);
	}

	public int getDbID() {
		return dbID;
	}

	public Application(String appID, int dbID) {
		this.appID = appID;
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
	
	public List<Review> getReviews(){
		return new ArrayList<Review>(reviewMap.values());
	}
}
