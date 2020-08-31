// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/* Post Object:
Holds the information in the cards
  - Post ID
  - Organization Name
  - The date (month, day, year)
  - The start time (hour, minute)
  - The end time (hour, minute)
  - location (lat, lng)
  - Number of people it feeds
  - Type of food
  - Description
  - College Id
  - Rank
*/

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.sps.data.InputPattern;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;  
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;   
import java.util.regex.Matcher;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

public class Post implements Comparable<Post> {

  private String postId = "";
  private String organizationName = "";
  private int month = 0;
  private int day = 0;
  private int year = 0;
  private int startHour = 0;
  private int startMinute = 0;
  private int endHour = 0;
  private int endMinute = 0;
  private String location = "";
  private double lat = 0.0;
  private double lng = 0.0;
  private int numberOfPeopleItFeeds = 0;
  private String typeOfFood = "";
  private String description = "";
  private String collegeId = "";
  private int timeSort = 0;
  private int rank = 0;

  public boolean valid = true; // If false, the post shouldn't be saved - it might have malicious data.

  /* Constructor for Post testing */
  public Post(String organizationName, int month, int day, int startHour, 
              int startMinute, int endHour, int endMinute, String location,
              double lat, double lng, int numberOfPeopleItFeeds, String typeOfFood,
              String description, String collegeId) {
    this.organizationName = organizationName;
    this.month = month;
    this.day = day;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.endHour = endHour;
    this.endMinute = endMinute;
    this.location = location;
    this.lat = lat;
    this.lng = lng;
    this.numberOfPeopleItFeeds = numberOfPeopleItFeeds;
    this.typeOfFood = typeOfFood;
    this.description = description;
    this.collegeId = collegeId;
    this.rank = this.numberOfPeopleItFeeds + getDuration();
    timeSort = startHour * 60 + startMinute;
  }

  public Post() {}

  /* Fill in the important Post details from the POST request. */
  public void requestToPost(HttpServletRequest request) {
    String orgNameUnparsed = request.getParameter("organizationName");
    String monthUnparsed = request.getParameter("month");
    String dayUnparsed = request.getParameter("day");
    String startHourUnparsed = request.getParameter("startHour");
    String startMinuteUnparsed = request.getParameter("startMinute");
    String endHourUnparsed = request.getParameter("endHour");
    String endMinuteUnparsed = request.getParameter("endMinute");
    String locationUnparsed = request.getParameter("location");
    String latUnparsed = request.getParameter("lat");
    String lngUnparsed = request.getParameter("lng");
    String numberOfPeopleItFeedsUnparsed = request.getParameter("numberOfPeopleItFeeds");
    String typeOfFoodUnparsed = request.getParameter("typeOfFood");
    String descriptionUnparsed = request.getParameter("description");
    String collegeIdUnparsed = request.getParameter("collegeId");

    // Perform input validation before we attempt to parse the inputs, as things like integers
    // might be invalid and would cause parseInt() to throw an exception.
    if (!InputPattern.TEXT.matcher(orgNameUnparsed).matches() || orgNameUnparsed.length() > 75 ||
        !InputPattern.POSITIVE_INTEGER.matcher(monthUnparsed).matches() ||
        !InputPattern.POSITIVE_INTEGER.matcher(dayUnparsed).matches() ||
        !InputPattern.POSITIVE_INTEGER.matcher(startHourUnparsed).matches() ||
        !InputPattern.POSITIVE_INTEGER.matcher(startMinuteUnparsed).matches() ||
        !InputPattern.POSITIVE_INTEGER.matcher(endHourUnparsed).matches() ||
        !InputPattern.POSITIVE_INTEGER.matcher(endMinuteUnparsed).matches() ||
        !InputPattern.TEXT.matcher(locationUnparsed).matches() || locationUnparsed.length() > 100 ||
        !InputPattern.DOUBLE.matcher(latUnparsed).matches() ||
        !InputPattern.DOUBLE.matcher(lngUnparsed).matches() ||
        !InputPattern.POSITIVE_INTEGER.matcher(numberOfPeopleItFeedsUnparsed).matches() ||
        !InputPattern.TEXT.matcher(typeOfFoodUnparsed).matches() || typeOfFoodUnparsed.length() > 25 ||
        !InputPattern.TEXT.matcher(descriptionUnparsed).matches() || descriptionUnparsed.length() > 500 ||
        descriptionUnparsed.length() < 15 ||
        !InputPattern.TEXT.matcher(collegeIdUnparsed).matches() || collegeIdUnparsed.length() > 6) {
      valid = false;
      return;
    }

    // After validating that what we have are indeed numbers/valid strings, parse and assign.
    organizationName = orgNameUnparsed;
    month = Integer.parseInt(monthUnparsed) - 1; // Months are indexed at 0.
    day = Integer.parseInt(dayUnparsed);
    startHour = Integer.parseInt(startHourUnparsed);
    startMinute = Integer.parseInt(startMinuteUnparsed);
    endHour = Integer.parseInt(endHourUnparsed);
    endMinute = Integer.parseInt(endMinuteUnparsed);
    location = locationUnparsed;
    lat = Double.parseDouble(latUnparsed);
    lng = Double.parseDouble(lngUnparsed);
    numberOfPeopleItFeeds = Integer.parseInt(numberOfPeopleItFeedsUnparsed);
    typeOfFood = typeOfFoodUnparsed;
    description = descriptionUnparsed;
    rank = numberOfPeopleItFeeds + getDuration();
    collegeId = collegeIdUnparsed;

    // Adjust the start and end hour based on whether the hour is AM or PM.
    startHour = startHour % 12;
    String startAMorPM = request.getParameter("startAMorPM");
    if (startAMorPM.equals("pm")) {
      startHour += 12;
    }
    endHour = endHour % 12;
    String endAMorPM = request.getParameter("endAMorPM");
    if (endAMorPM.equals("pm")) {
      endHour += 12;
    }     

    // Check that values are within expected ranges.
    if (month < 0 || month > 11 ||
      day < 0 || day > 31 ||
      startHour < 0 || startHour > 23 ||
      startMinute < 0 || startMinute > 59 ||
      endHour < 0 || endHour > 23 ||
      endMinute < 0 || endMinute > 59 ||
      numberOfPeopleItFeeds < 0 ||
      startHour > endHour ||
      (startHour == endHour && startMinute > endMinute)) {
      valid = false;
      return;
    }

    // Translate the start time into minutes to allow for sorting.
    timeSort = startHour * 60 + startMinute;

    // Set the year. Right now time zone is set to "America/Los_Angeles".
    Calendar nowTime = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
    year = nowTime.get(Calendar.YEAR);

  }

  /* Translate the entities from the Datastore query to Post objects and return in an array. */
  public static ArrayList<Post> queryToPosts(PreparedQuery queryResult, DatastoreService datastore) {
    ArrayList<Post> currentPosts = new ArrayList<Post>();

    // Create a calendar based off the current time zone.
    // TODO: Update Time Zone based off student and university location, instead of "America/Los_Angeles".

    Calendar nowTime = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));

    for (Entity entity: queryResult.asIterable()) {

      // Create a calendar based off the post timing.
      Calendar postTime = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
      int postMonth = Integer.parseInt(entity.getProperty("month").toString());
      int postDay = Integer.parseInt(entity.getProperty("day").toString());
      int postYear = Integer.parseInt(entity.getProperty("year").toString());
      int postHour = Integer.parseInt(entity.getProperty("endHour").toString());
      int postMinute = Integer.parseInt(entity.getProperty("endMinute").toString());
      postTime.set(postYear, postMonth, postDay, postHour, postMinute);
      
      // If the post time is before the current time, delete from Datastore.
      if (postTime.before(nowTime)) {
        datastore.delete(entity.getKey());
      }
      // Only add the post to result if it is on the same day.
      else if (postYear == nowTime.get(Calendar.YEAR) && postMonth == nowTime.get(Calendar.MONTH) && postDay == nowTime.get(Calendar.DATE)) {
        Post newPost = new Post();
        newPost.entityToPost(entity);
        currentPosts.add(newPost);
      } 
    }
    return currentPosts;
  }

  /* Translate the fields of an entity from Datastore to a Post object. */
  public void entityToPost(Entity entity) {
    organizationName = (String) entity.getProperty("organizationName");
    month = Integer.parseInt(entity.getProperty("month").toString());
    day = Integer.parseInt(entity.getProperty("day").toString());
    year = Integer.parseInt(entity.getProperty("year").toString());
    startHour = Integer.parseInt(entity.getProperty("startHour").toString());
    startMinute = Integer.parseInt(entity.getProperty("startMinute").toString());
    endHour = Integer.parseInt(entity.getProperty("endHour").toString());
    endMinute = Integer.parseInt(entity.getProperty("endMinute").toString());
    location = (String) entity.getProperty("location");
    lat = Double.parseDouble(entity.getProperty("lat").toString());
    lng = Double.parseDouble(entity.getProperty("lng").toString());
    numberOfPeopleItFeeds = Integer.parseInt(entity.getProperty("numberOfPeopleItFeeds").toString());
    typeOfFood = (String) entity.getProperty("typeOfFood");
    description = (String) entity.getProperty("description");
    timeSort = Integer.parseInt(entity.getProperty("timeSort").toString());
    collegeId = (String) entity.getProperty("collegeId");
    postId = entity.getKey().toString();
    rank = Integer.parseInt(entity.getProperty("rank").toString());
  }

  /* Creates a new entity with the college ID and the Post information. Sets all the properties. */
  public Entity postToEntity(String entityKind) {
    Entity newPost = new Entity(entityKind);

    newPost.setProperty("organizationName", organizationName);

    newPost.setProperty("month", month);
    newPost.setProperty("day", day);
    newPost.setProperty("year", year);

    newPost.setProperty("startHour", startHour);
    newPost.setProperty("startMinute", startMinute);
    newPost.setProperty("endHour", endHour);
    newPost.setProperty("endMinute", endMinute);

    newPost.setProperty("location", location);
    newPost.setProperty("lat", lat);
    newPost.setProperty("lng", lng);

    newPost.setProperty("typeOfFood", typeOfFood);
    newPost.setProperty("numberOfPeopleItFeeds", numberOfPeopleItFeeds);
    newPost.setProperty("description", description);
    newPost.setProperty("rank", rank);

    newPost.setProperty("timeSort", timeSort);
    newPost.setProperty("collegeId", collegeId);

    return newPost;
  }

  /* Set sorting by the rank of event. */
  @Override     
  public int compareTo(Post post) {          
    return (this.getRank() > post.getRank() ? 1 : 
      (this.getRank() == post.getRank() ? 0 : -1));     
  }     

  /* Get duration of an event in minutes.*/
  public int getDuration() {
    LocalTime start = LocalTime.of(startHour, startMinute, 0);
    LocalTime end = LocalTime.of(endHour, endMinute, 0);
    int duration = (int) ChronoUnit.MINUTES.between(start, end);  

    return duration;
  }

  /* Class Getters. */
  public String getOrganizationName() {
    return organizationName;
  }

  public int getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }

  public int getStartHour() {
    return startHour;
  }

  public int getStartMinute() {
    return startMinute;
  }

  public int getEndHour() {
    return endHour;
  }

  public int getEndMinute() {
    return endMinute;
  }

  public String getLocation() {
    return location;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public int getNumberOfPeopleItFeeds() {
    return numberOfPeopleItFeeds;
  }

  public String getTypeOfFood() {
    return typeOfFood;
  }

  public String getDescription() {
    return description;
  }

  public String getCollegeId() {
    return collegeId;
  }

  public int getRank() {
    return rank;
  }
}
