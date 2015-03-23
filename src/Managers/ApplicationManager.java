package Managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import model.Application;
import model.Review;
import model.Review.ReviewBuilder;
import util.PostgreSQLConnector;
import word2vec.WordVec;

public class ApplicationManager implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6981176569155372512L;
	private static ApplicationManager instance = null;
	private Set<Application> appSet;
	private long lastUpdate;
	private long currentUpdate;
	public static final String FILENAME = main.main.DATA_DIRECTORY
			+ "applicationsData" + ".ser";
	private int totalReviewCount;

	public static final WordVec word2vec = new WordVec();
	public static synchronized ApplicationManager getInstance() {
		if (instance == null) {
			File fcheckExist = new File(FILENAME);
			if (fcheckExist.exists() && !fcheckExist.isDirectory()) {
				System.err.println(">>Read Application Manager from file.");
				FileInputStream fin;
				try {
					fin = new FileInputStream(FILENAME);
					ObjectInputStream oos = new ObjectInputStream(fin);
					instance = (ApplicationManager) oos.readObject();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					instance = new ApplicationManager();
				}
			} else
				instance = new ApplicationManager();
		}
		return instance;
	}

	private ApplicationManager() {
		appSet = new HashSet<>();
		lastUpdate = 0;
		currentUpdate = 0;
		totalReviewCount = 0;
	}

	/**
	 * Create new instance of Application and add them to the list
	 * 
	 * @param appID
	 *            - the String contains the appID on Google Play.
	 * @param dbID
	 *            - the String contains the dbID on our database.
	 * @return False if this appID is already in the list, True if it isn't and
	 *         is successfully added
	 * 
	 */
	public boolean addNewApp(String appID, int dbID) {
		return appSet.add(new Application(appID, dbID));
	}

	public void writeSentenceToFile(PrintWriter fileWriter,PostgreSQLConnector db ) {
		for (Application app : appSet) {
			app.writeSentenceToFile(fileWriter, lastUpdate,db);
		}
	}

	public int fillAppsWithReviews() {
		PostgreSQLConnector db = null;
		int count = 0;
		long startTime = System.nanoTime();
		lastUpdate = currentUpdate;
		try {
			long thisUpdate = currentUpdate;
			db = new PostgreSQLConnector(PostgreSQLConnector.DBLOGIN,
					PostgreSQLConnector.DBPASSWORD,
					PostgreSQLConnector.REVIEWDB);
			String fields[] = { "title", "text", "rating", "creationtime",
					"documentversion", "reviewid", "device" };
			for (Application app : appSet) {
				String condition = "appid=" + app.getDbID()
						+ " AND creationtime>" + currentUpdate;
				ResultSet results;
				results = db.select(PostgreSQLConnector.REVIEWS_TABLE, fields,
						condition);
				while (results.next()) {
					try {
						String reviewID = results.getString("reviewid");
						if (app.contains(reviewID))
							continue;
						Review.ReviewBuilder reviewBuilder = new Review.ReviewBuilder();
						long creationTime = results.getLong("creationtime");
						if (creationTime <= currentUpdate)
							continue;
						String text = results.getString("text");
						if (text.indexOf('\t') < 0) // Not from Android Market
							text = results.getString("title") + "." + text;

						reviewBuilder.text(text);
						reviewBuilder.reviewId(reviewID);
						reviewBuilder.deviceName(results.getString("device"));
						reviewBuilder.documentVersion(results
								.getString("documentversion"));
						reviewBuilder.rating(results.getInt("rating"));
						reviewBuilder.creationTime(creationTime);
						reviewBuilder.application(app);
						app.addReview(reviewBuilder.createReview());
						if (thisUpdate < creationTime)
							thisUpdate = creationTime;
						count++;
						if (count % 10000 == 0) {

							long stopTime = System.nanoTime();
							long duration = stopTime - startTime;
							startTime = stopTime;
							System.out.println("Reviews processed: " + count
									+ ", time passed since last message: "
									+ (duration / 1000000) + " milliseconds");
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			if (thisUpdate > currentUpdate)
				currentUpdate = thisUpdate;

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		totalReviewCount += count;
		return count;
	}

	public int reviewsNumber() {
		return totalReviewCount;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public int applicationsNumber() {
		return appSet.size();
	}

	public void setLastUpdate(long time) {
		lastUpdate = time;
	}

	public long getCurrentUpdateTime() {
		return currentUpdate;
	}

	public long getLastUpdateTime() {
		return lastUpdate;
	}

	public Set<Application> getAppSet() {
		return appSet;
	}
}
