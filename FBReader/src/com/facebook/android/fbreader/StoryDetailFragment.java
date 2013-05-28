package com.facebook.android.fbreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.fbreader.dummy.DummyContent;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

/*
 * The StoryDetailFragment shows details on the selected
 * content, a button for sharing a story back to 
 * Facebook with a feed dialog, a button for sending a 
 * request to add new friends to the app, a button that 
 * gets a list of friends using Android devices and the 
 * music they've been listening to lately, and a button 
 * to publish an Open Graph action. Initially, these 
 * buttons do not do anything. 
 * 
 */

public class StoryDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    DummyContent.DummyItem mItem;
    
    private Button shareButton;
    private Button androidFriendsButton;
    private Button sendRequestsButton;
    private Button publishButton;
    private ProgressDialog progressDialog;
    
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
    	     if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
	} 
    
	private PendingAction pendingAction = PendingAction.NONE;
	
	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.android.fbreader:PendingAction";
	
	private enum PendingAction {
		NONE,
		READ_OG,
		PUBLISH
	}

    public StoryDetailFragment() {
    }

    // add UiLifecycleHelper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }
    

    // Wire up the buttons, which start out not doing anything
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story_detail, container, false);
        if (mItem != null) {
        	   shareButton = ((Button) rootView.findViewById(R.id.shareButton));
        	   shareButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (Session.getActiveSession().isOpened()) {
						publishFeedDialog(mItem);
					} else {
						Toast.makeText(getActivity(), 
								"You must log in to publish a story", 
								Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
        	  
        	   sendRequestsButton = ((Button) rootView.findViewById(R.id.sendRequestsButton));
       	   sendRequestsButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (Session.getActiveSession().isOpened()) {
						sendRequest(mItem);
					} else {
						Toast.makeText(getActivity(), 
								"You must log in to send a request", 
								Toast.LENGTH_SHORT)
								.show();
					}
					
				}
			});
        	   
        	   androidFriendsButton = ((Button) rootView.findViewById(R.id.androidFriendsButton));
        	   androidFriendsButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (Session.getActiveSession().isOpened()) {
						getMusic();
					} else {
						Toast.makeText(getActivity(), 
								"You must log in to get friend information", 
								Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
        	   
        	   publishButton = ((Button) rootView.findViewById(R.id.publishButton));
        	   publishButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (Session.getActiveSession().isOpened()) {
						publishOpenGraphStory(mItem);
					} else {
						Toast.makeText(getActivity(), 
								"You must log in to publish a story", 
								Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
        
        	   if (savedInstanceState != null) {
                   String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
                   pendingAction = PendingAction.valueOf(name);
               }
        	   
           ((TextView) rootView.findViewById(R.id.story_detail)).setText(mItem.title);
           ((TextView) rootView.findViewById(R.id.story_description)).setText(mItem.content);
        }
        
        return rootView;
    }
    
    // Take the currently selected item and populate a Feed
    // Dialog with its content to be posted to Facebook
    
    private void publishFeedDialog(DummyContent.DummyItem item) {
        Bundle params = new Bundle();
        
        // Add details about the clicked item to the
        // story params Bundle
        params.putString("name", item.title);
        params.putString("description", item.content);
        params.putString("link", item.url);
        params.putString("picture", item.pictureLink);

        WebDialog feedDialog = (
            new WebDialog.FeedDialogBuilder(getActivity(),
                Session.getActiveSession(),
                params))
            .setOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(Bundle values,
                    FacebookException error) {
                    if (error == null) {
                        // When the story is posted, echo the success
                        // and the post Id.
                        final String postId = values.getString("post_id");
                        if (postId != null) {
                            Toast.makeText(getActivity(),
                                "Posted story, id: "+postId,
                                Toast.LENGTH_SHORT).show();
                        } else {
                            // User clicked the Cancel button
                            Toast.makeText(getActivity()
                            .getApplicationContext(), 
                                "Publish cancelled", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else if (error instanceof 
                    FacebookOperationCanceledException) {
                        // User clicked the "x" button
                        Toast.makeText(getActivity().getApplicationContext(), 
                            "Publish cancelled", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        // Generic, ex: network error
                        Toast.makeText(getActivity().getApplicationContext(), 
                            "Error posting story", 
                            Toast.LENGTH_SHORT).show();
                    }
                }

            })
            .build();
        feedDialog.show();
    }
    
    // Very similar to the Feed Dialog, this uses the WebDialogBuilder
    // to build a Requests dialog. 
    private void sendRequest(DummyContent.DummyItem item) {
    	    Bundle params = new Bundle();
        params.putString("message", "Learn how to make your Android apps social");
        
        // Only show people who aren't already using the app 
        params.putString("filters", "app_non_users");
        
        // Track which page this request was generated from
        params.putString("data", item.content);

        WebDialog requestsDialog = (
            new WebDialog.RequestsDialogBuilder(getActivity(),
                Session.getActiveSession(),
                params))
                .setOnCompleteListener(new OnCompleteListener() {

                   // Add logging here to see when people choose
                	   // not to share 
                	   @Override
                    public void onComplete(Bundle values,
                        FacebookException error) {
                        if (error != null) {
                            if (error instanceof FacebookOperationCanceledException) {
                                Toast.makeText(getActivity().getApplicationContext(), 
                                    "Request cancelled", 
                                    Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), 
                                    "Network Error", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            final String requestId = values.getString("request");
                            if (requestId != null) {
                                Toast.makeText(getActivity().getApplicationContext(), 
                                    "Request sent",  
                                    Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), 
                                    "Request cancelled", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }   
                    }

                })
                .build();
        requestsDialog.show();
    	
    }
   
    // Asking for friends' music activity requires new
    // permissions. Ask for them in context!
    private void getMusic() {
    	   if (Session.getActiveSession().getPermissions()
  			  .contains("friends_actions.music")) {
    		   getAndroidFriends();
    	   } else {
    		   pendingAction = PendingAction.READ_OG;
    		   Session.NewPermissionsRequest newPermsRequest = 
    				   new Session.NewPermissionsRequest(
    					   getActivity(),
    					   Arrays.asList("friends_actions.music"));
    		   Session.getActiveSession()
    		          .requestNewReadPermissions(newPermsRequest);
    	   }
    }
    
    // Make an FQL call using the user and friend
    // tables to return a list of friends that only
    // includes people using at least one Android
    // device, alphabetized by name
   private void getAndroidFriends() {
    	      String fqlQuery = "SELECT uid, devices FROM user WHERE uid IN" + 
                  "(SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 100) " +
                  "AND \"Android\" IN devices " +
                  "ORDER BY name";
          Bundle params = new Bundle();
          
          // Put the query text into param "q"
          params.putString("q", fqlQuery);
          Session session = Session.getActiveSession();
          
          // Construct a request using the /fql graph path
          // end point and pass in parameter "q"
          Request request = new Request(session,
              "/fql",                         
              params,                         
              HttpMethod.GET,                 
              new Request.Callback(){         
                  public void onCompleted(Response response) {
                      if (response!=null) {
                    	     // if this call returned friends,
                    	     // parse the response for UIDs
                    	     ArrayList<String> androidIds = getIdsFromResponse(response);
                    	     requestFriendDataFromIds(androidIds);
                      }
                  }                  
          }); 
          request.executeAsync();
         
    }
    
    // Pull uids from response data. Quick and dirty,
    // a proper implementation should probably model
    // the response
    private ArrayList<String> getIdsFromResponse(Response response) {
    	     int uidStart;
    	     int uidEnd;
    	     Log.i("Response was", response.toString());
    	     String responseText = response.toString();
    	     ArrayList<String>androidIds = new ArrayList<String>();
    	     for (int i=0; i<responseText.length(); i++) {
    	    	      uidStart = responseText.indexOf("uid", i) + 5;
    	    	      uidEnd = responseText.indexOf("}", uidStart);
    	    	      String id = responseText.substring(uidStart, uidEnd);
    	    	      if (!androidIds.contains(id)) {
    	    	         androidIds.add(id);
    	    	      }
    	     }
    	     return androidIds;
    }
    
    // Get the latest song each friend using an Android
    // device listened to
    private void requestFriendDataFromIds(ArrayList<String> androidIds) {
    	   if (Session.getActiveSession().getPermissions()
  			  .contains("friends_actions.music")) {
    		   
    	     RequestBatch requestBatch = new RequestBatch();
 
         for (String id : androidIds) {
            Request musicRequest = Request.newGraphPathRequest(
            		Session.getActiveSession(), 
                id+"/music.listens", 
                new Request.Callback() {
                    
            	        public void onCompleted(Response response) {
            	        	  if (response != null) {
            	        	    Log.i("Story Detail Music", response.toString());
            	        	  }
            	        }
                });
            requestBatch.add(musicRequest);
         }
   
         requestBatch.executeAsync();	
    	  }
    }
    
    
   private void publishOpenGraphStory(DummyContent.DummyItem mItem){
	   
	   Session session = Session.getActiveSession();
	    if (session != null) {
		    // Check for publish permissions 
	        if (!session.getPermissions().contains("publish_actions")) {
	        		pendingAction = PendingAction.PUBLISH;
	        		Session.NewPermissionsRequest newPermissionsRequest = new Session 
	        				.NewPermissionsRequest(getActivity(), 
	        						Arrays.asList("publish_actions"));
	        		session.requestNewPublishPermissions(newPermissionsRequest);
	        		return;
		    }

		    // Show a progress dialog because the batch request could take a while.
	        progressDialog = ProgressDialog.show(getActivity(), "",
	                getActivity().getResources().getString(R.string.progress_dialog_text), true);

		    try {
				// Create a batch request, firstly to post a new object and
				// secondly to publish the action with the new object's id.
				RequestBatch requestBatch = new RequestBatch();

		      	// Set up JSON representing the resource
				JSONObject resource = new JSONObject();
	
				// Copy the resource attributes into the JSONObject
				resource.put("image", mItem.pictureLink);
				resource.put("title", mItem.title);			
				resource.put("url", 
						"https://stark-shelf-5591.herokuapp.com/resources/posted_resource");
				resource.put("description", mItem.content);

				// Set up object request parameters
				Bundle objectParams = new Bundle();
				objectParams.putString("object", resource.toString());
				// Set up the object request callback
			    Request.Callback objectCallback = new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						// Log any response error
						FacebookRequestError error = response.getError();
						if (error != null) {
							dismissProgressDialog();
							Log.i("Publishing OG Action", error.getErrorMessage());
						}
					}
			    };

			    // Create the request for object creation
				Request objectRequest = new Request(Session.getActiveSession(), 
						"me/objects/resourcebrowser:resource", objectParams, 
		                HttpMethod.POST, objectCallback);

				// Set the batch name so you can refer to the result
				// in the follow-on publish action request
				objectRequest.setBatchEntryName("objectCreate");

				// Add the request to the batch
				requestBatch.add(objectRequest);

				// Request: Publish action request
				// --------------------------------------------
				Bundle actionParams = new Bundle();
				// Refer to the "id" in the result from the previous batch request
				actionParams.putString("resource", "{result=objectCreate:$.id}");
				// Turn on the explicit share flag
				//actionParams.putString("fb:explicitly_shared", "true");

				// Set up the action request callback
				Request.Callback actionCallback = new Request.Callback() {

					@Override
					public void onCompleted(Response response) {
						dismissProgressDialog();
						FacebookRequestError error = response.getError();
						if (error != null) {
							Toast.makeText(getActivity()
								.getApplicationContext(),
								error.getErrorMessage(),
								Toast.LENGTH_LONG).show();
						} else {
							String actionId = null;
							try {
								JSONObject graphResponse = response
				                .getGraphObject()
				                .getInnerJSONObject();
								actionId = graphResponse.getString("id");
							} catch (JSONException e) {
								Log.i("Publishing OG Action",
										"JSON error "+ e.getMessage());
							}
							Toast.makeText(getActivity()
								.getApplicationContext(), 
								actionId,
								Toast.LENGTH_LONG).show();
						}
					}
				};

				// Create the publish action request
				Request actionRequest = new Request(Session.getActiveSession(),
						"me/resourcebrowser:browse", actionParams, HttpMethod.POST,
						actionCallback);

				// Add the request to the batch
				requestBatch.add(actionRequest);
				// Execute the batch request
				requestBatch.executeAsync();
			} catch (JSONException e) {
				Log.i("Publishing OG Action",
						"JSON error "+ e.getMessage());
				dismissProgressDialog();
			}
		}
	}

	/*
	 * Helper method to dismiss the progress dialog.
	 */
	private void dismissProgressDialog() {
		// Dismiss the progress dialog
		if (progressDialog != null) {
           progressDialog.dismiss();
           progressDialog = null;
       }
	}
  
   
   
   @SuppressWarnings("incomplete-switch")
   private void handlePendingAction() {
       PendingAction previouslyPendingAction = pendingAction;
       // These actions may re-set pendingAction if they are still pending, but we assume they
       // will succeed.
       pendingAction = PendingAction.NONE;

       switch (previouslyPendingAction) {
           case READ_OG:
               getAndroidFriends();
               break;
           case PUBLISH:
               publishOpenGraphStory(mItem);
               break;
       }
   }
   
    
    // add in UiLifecycleHelper calls to lifecycle methods

    @Override
    public void onResume() {
        super.onResume();
        
        // For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null &&
				(session.isOpened() || session.isClosed()) ) {
			onSessionStateChange(session, session.getState(), null);
		}

        uiHelper.onResume();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
        uiHelper.onSaveInstanceState(outState);
    }
  
}


