package com.facebook.android.fbreader.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyContent {
	
	/*
	 * This file defines the content for the
	 * items in the ListView. It adds a few
	 * extra fields to the standard template's
	 * DummyItem, and adds static content that
	 * we can share back to Facebook
	 */

    public static class DummyItem {

        public String id;
        public String title;
        public String content;
        public String url;
        public String pictureLink;
        

        public DummyItem(String id, String title, 
        		String content, String url, 
        		String pictureLink) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.url = url;
            this.pictureLink = pictureLink;
        }

        @Override
        public String toString() {
            return title;
        }
        
    }

    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    public static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
}
