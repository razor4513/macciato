package de.telekom.cldii.data.mail.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.util.Log;

import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.store.LocalStore.LocalMessage;

import de.telekom.cldii.data.mail.IFullMail;

public class FullMailWrapper extends CompactMailWrapper implements IFullMail {
	
	private static final String TAG = "FullMailWrapper";

	private LocalMessage localMessage;
	
	public FullMailWrapper(LocalMessage message) {
		super(message);
		this.localMessage = message;
	}

	@Override
	public String getText() {
		try {
			String textForDisplay = localMessage.getTextForDisplay();
			if(textForDisplay != null) {
				try {
					Document doc = Jsoup.parse(textForDisplay);
					doc.select("img").remove();
					doc.select("style").remove();
					textForDisplay = doc.toString();
				} catch (Exception e) {
					// if there is an undocumented exception in Jsoup we return the value unchanged
				}
				return textForDisplay;
			} else {
				return "";
			}
		} catch (MessagingException e) {
			Log.e(TAG, "Failed to get text for displaying", e);
			return "";
		}
	}

}
