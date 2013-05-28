package com.facebook.android.fbreader;

import java.util.List;

import android.R;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.android.fbreader.dummy.DummyContent;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class StoryListFragment extends ListFragment {
	
	/*
	 * This file contains no additional code beyond
	 * that which is included in the master/detail
	 * template. This class will not be modified in
	 * the demo.
	 */

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private List<ParseObject> mStories;
    private Dialog mProgressDialog;
    
    public interface Callbacks {

        public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    public StoryListFragment() {
    }

    /*
     * This task makes a call to Parse to get the resource list
     * data.
     */
	private class RemoteDataTask extends AsyncTask<Void, Void, Void> {
		// Override this method to do custom remote calls
		protected Void doInBackground(Void... params) {
			// Gets the current list of todos in sorted order
			ParseQuery query = new ParseQuery("DummyContent");
			query.orderByAscending("_created_at");

			try {
				mStories = query.find();
			} catch (ParseException e) {

			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(getActivity(), "",
					"Loading...", true);
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {

			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			// Put the list of stories into the list view
			for (ParseObject story : mStories) {
				DummyContent.addItem(new DummyContent.DummyItem(
						story.getObjectId(), 
						story.get("title").toString(),
						story.get("content").toString(),
						story.get("url").toString(),
						story.get("pictureLink").toString()
						));
			}
	        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
	        		R.layout.simple_list_item_activated_1,
	        		R.id.text1,
	        		DummyContent.ITEMS));
			mProgressDialog.dismiss();
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kick off task to fetch data from Parse
        new RemoteDataTask().execute();
    }

    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState
                .containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
