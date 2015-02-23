package crawlers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.akdeniz.googleplaycrawler.GooglePlay.GetReviewsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.ReviewResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.GooglePlayAPI.REVIEW_SORT;

public class GooglePlayCrawler {
	private GooglePlayAPI service;

	public GooglePlayCrawler(String login, String password, String androidID) {
		this.service = new GooglePlayAPI(login, password, androidID);
		try {
			service.login();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getVersion(String appid) {
		String ver = "unknown";
		try {
			ver = service.details(appid).getDocV2().getDetails()
					.getAppDetails().getVersionString();
			TimeUnit.SECONDS.sleep(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ver;
	}

	public long getUploadDate(String appid) {
		String versionDate;
		long uploadDate = 0;
		try {
			versionDate = service.details(appid).getDocV2().getDetails()
					.getAppDetails().getUploadDate();
			SimpleDateFormat f = new SimpleDateFormat("MMM dd,yyyy");
			Date date = (Date) f.parse(versionDate);
			uploadDate = date.getTime();
			TimeUnit.SECONDS.sleep(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uploadDate;
	}

	public String getDescription(String appid) {
		StringBuilder desc = new StringBuilder();
		try {
			desc.append(service.details(appid).getDocV2().getDescriptionHtml()
					.replaceAll("&quot;", "").replaceAll("<br>", "\n"));
			desc.append("\n");
			desc.append(service.details(appid).getDocV2().getDetails()
					.getAppDetails().getRecentChangesHtml()
					.replaceAll("&quot;", "").replaceAll("<br>", "\n"));

			TimeUnit.SECONDS.sleep(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return desc.toString();
	}

	public String getNameAndPermision(String appid) {
		StringBuilder text = new StringBuilder();
		try {
			text.append(service.details(appid).getDocV2().getDetails()
					.getAppDetails().getTitle());
			TimeUnit.SECONDS.sleep(1);
			List<String> permisions = service.details(appid).getDocV2()
					.getDetails().getAppDetails().getPermissionList();
			for (String per : permisions)
				text.append(" " + per);
			TimeUnit.SECONDS.sleep(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text.toString();
	}

}
