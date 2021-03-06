/* 
 * Copyright (c) 2014 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.app.net.social;

import android.content.Context;
import android.support.annotation.NonNull;

import org.andstatus.app.context.TestSuite;
import org.andstatus.app.data.DownloadStatus;
import org.andstatus.app.net.http.HttpReadResult;
import org.andstatus.app.net.social.Connection.ApiRoutineEnum;
import org.andstatus.app.util.TriState;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static org.andstatus.app.context.DemoData.demoData;
import static org.andstatus.app.util.RelativeTime.DATETIME_MILLIS_NEVER;
import static org.andstatus.app.util.RelativeTime.SOME_TIME_AGO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionGnuSocialTest {
    private ConnectionTwitterGnuSocialMock connection;

    public static AActivity getNoteWithAttachment(Context context) throws Exception {
        ConnectionGnuSocialTest test = new ConnectionGnuSocialTest();
        test.setUp();
        return test.privateGetNoteWithAttachment(true);
    }

    @Before
    public void setUp() throws Exception {
        TestSuite.initializeWithData(this);
        connection = new ConnectionTwitterGnuSocialMock();
    }

    @Test
    public void testGetPublicTimeline() throws IOException {
        connection.getHttpMock().addResponse(org.andstatus.app.tests.R.raw.quitter_home);

        String accountActorOid = demoData.gnusocialTestAccountActorOid;
        List<AActivity> timeline = connection.getTimeline(ApiRoutineEnum.PUBLIC_TIMELINE,
                new TimelinePosition("2656388"), TimelinePosition.EMPTY, 20, accountActorOid);
        assertNotNull("timeline returned", timeline);
        int size = 3;
        assertEquals("Number of items in the Timeline", size, timeline.size());

        int ind = 0;
        assertEquals("Posting note", AObjectType.NOTE, timeline.get(ind).getObjectType());
        AActivity activity = timeline.get(ind);
        assertEquals("Timeline position", "2663077", activity.getTimelinePosition().getPosition());
        assertEquals("Note Oid", "2663077", activity.getNote().oid);
        assertEquals("conversationOid", "2218650", activity.getNote().conversationOid);
        assertEquals("Favorited " + activity, TriState.TRUE, activity.getNote().getFavoritedBy(activity.accountActor));
        assertEquals("Oid", "116387", activity.getAuthor().oid);
        assertEquals("Username", "aru", activity.getAuthor().getUsername());
        assertEquals("WebFinger ID", "aru@status.vinilox.eu", activity.getAuthor().getWebFingerId());
        assertEquals("Display name", "aru", activity.getAuthor().getRealName());
        assertEquals("Description", "Manjaro user, student of physics and metalhead. Excuse my english ( ͡° ͜ʖ ͡°)", activity.getAuthor().getDescription());
        assertEquals("Location", "Spain", activity.getAuthor().location);
        assertEquals("Profile URL", "https://status.vinilox.eu/aru", activity.getAuthor().getProfileUrl());
        assertEquals("Homepage", "", activity.getAuthor().getHomepage());
        assertEquals("Avatar URL", "http://quitter.se/avatar/116387-48-20140609172839.png", activity.getAuthor().getAvatarUrl());
        assertEquals("Banner URL", "", activity.getAuthor().bannerUrl);
        assertEquals("Notes count", 523, activity.getAuthor().notesCount);
        assertEquals("Favorites count", 11, activity.getAuthor().favoritesCount);
        assertEquals("Following (friends) count", 23, activity.getAuthor().followingCount);
        assertEquals("Followers count", 21, activity.getAuthor().followersCount);
        assertEquals("Created at", connection.parseDate("Sun Feb 09 22:33:42 +0100 2014"), activity.getAuthor().getCreatedDate());
        assertEquals("Updated at", 0, activity.getAuthor().getUpdatedDate());

        ind++;
        activity = timeline.get(ind);
        assertEquals("Timeline position", "2664346", activity.getTimelinePosition().getPosition());
        assertEquals("Note Oid", "2664346", activity.getNote().oid);
        assertEquals("conversationOid", "2218650", activity.getNote().conversationOid);
        assertTrue("Does not have a recipient", activity.recipients().isEmpty());
        assertNotEquals("Is a reblog", ActivityType.ANNOUNCE,  activity.type);

        final AActivity inReplyTo = activity.getNote().getInReplyTo();
        assertTrue("Is a reply", inReplyTo.nonEmpty());
        assertEquals("Reply to the note id", "2663833", inReplyTo.getNote().oid);
        assertEquals("Reply to the note by actorOid", "114973", inReplyTo.getActor().oid);
        assertEquals("Updated date should be 0 for inReplyTo note", DATETIME_MILLIS_NEVER,
                inReplyTo.getNote().getUpdatedDate());
        assertEquals("Updated date should be 0 for inReplyTo activity", 0, inReplyTo.getUpdatedDate());

        assertEquals("Favorited " + activity, TriState.UNKNOWN, activity.getNote().getFavoritedBy(activity.accountActor));
        String startsWith = "@<span class=\"vcard\">";
        assertEquals("Body of this note starts with", startsWith, activity.getNote().getContent().substring(0, startsWith.length()));
        assertEquals("Username", "andstatus", activity.getAuthor().getUsername());
        assertEquals("Display name", "AndStatus@quitter.se", activity.getAuthor().getRealName());
        assertEquals("Banner URL", "https://quitter.se/file/3fd65c6088ea02dc3a5ded9798a865a8ff5425b13878da35ad894cd084d015fc.png", activity.getAuthor().bannerUrl);

        ind++;
        activity = timeline.get(ind);
        assertEquals("conversationOid", "2218650", activity.getNote().conversationOid);
        assertEquals("Note not private", TriState.UNKNOWN, activity.getNote().getPublic());
        assertEquals("Favorited " + activity, TriState.UNKNOWN, activity.getNote().getFavoritedBy(activity.accountActor));
        assertEquals("MyAccount", accountActorOid, activity.accountActor.oid);
        assertEquals("Actor", activity.getAuthor().oid, activity.getActor().oid);
        assertEquals("Oid", "114973", activity.getAuthor().oid);
        assertEquals("Username", "mmn", activity.getAuthor().getUsername());
        assertEquals("WebFinger ID", "mmn@social.umeahackerspace.se", activity.getAuthor().getWebFingerId());
        assertEquals("Display name", "mmn", activity.getAuthor().getRealName());
        assertEquals("Description", "", activity.getAuthor().getDescription());
        assertEquals("Location", "Umeå, Sweden", activity.getAuthor().location);
        assertEquals("Profile URL", "https://social.umeahackerspace.se/mmn", activity.getAuthor().getProfileUrl());
        assertEquals("Homepage", "http://blog.mmn-o.se/", activity.getAuthor().getHomepage());
        assertEquals("Avatar URL", "http://quitter.se/avatar/114973-48-20140702161520.jpeg", activity.getAuthor().getAvatarUrl());
        assertEquals("Banner URL", "", activity.getAuthor().bannerUrl);
        assertEquals("Notes count", 1889, activity.getAuthor().notesCount);
        assertEquals("Favorites count", 31, activity.getAuthor().favoritesCount);
        assertEquals("Following (friends) count", 17, activity.getAuthor().followingCount);
        assertEquals("Followers count", 31, activity.getAuthor().followersCount);
        assertEquals("Created at", connection.parseDate("Wed Aug 14 10:05:28 +0200 2013"), activity.getAuthor().getCreatedDate());
        assertEquals("Updated at", 0, activity.getAuthor().getUpdatedDate());
    }

    @Test
    public void testSearch() throws IOException {
        connection.getHttpMock().addResponse(org.andstatus.app.tests.R.raw.twitter_home_timeline);
        
        List<AActivity> timeline = connection.searchNotes(new TimelinePosition(""), TimelinePosition.EMPTY, 20,
                demoData.globalPublicNoteText);
        assertNotNull("timeline returned", timeline);
        int size = 4;
        assertEquals("Number of items in the Timeline", size, timeline.size());
    }

    @Test
    public void testPostWithMedia() throws IOException {
        connection.getHttpMock().addResponse(org.andstatus.app.tests.R.raw.quitter_note_with_attachment);
        AActivity activity = connection.updateNote("", "Test post note with media", "",
                Audience.EMPTY, "", demoData.localImageTestUri);
        assertEquals("Note returned",
                privateGetNoteWithAttachment(false).getNote(), activity.getNote());
    }

    @Test
    public void getNoteWithAttachment() throws IOException {
        privateGetNoteWithAttachment(true);
    }

    private AActivity privateGetNoteWithAttachment(boolean uniqueUid) throws IOException {
        final String NOTE_OID = "2215662";
        // Originally downloaded from https://quitter.se/api/statuses/show.json?id=2215662
        connection.getHttpMock().addResponse(org.andstatus.app.tests.R.raw.quitter_note_with_attachment);
        AActivity activity = connection.getNote(NOTE_OID);
        if (uniqueUid) {
            activity.setNote(activity.getNote().copy(activity.getNote().oid + "_" + demoData.testRunUid));
        }
        assertNotNull("note returned", activity);
        assertEquals("conversationOid", "1956322", activity.getNote().conversationOid);
        assertEquals("Author", "mcscx", activity.getAuthor().getUsername());
        assertEquals("null Homepage (url) should be treated as blank", "", activity.getAuthor().getHomepage());

        assertEquals("has attachment", 1, activity.getNote().attachments.size());
        Attachment attachment = Attachment.fromUri("https://quitter.se/file/mcscx-20131110T222250-427wlgn.png");
        assertEquals("attachment", attachment, activity.getNote().attachments.list.get(0));
        return activity;
    }

    @Test
    public void testReblog() throws IOException {
        final String NOTE_OID = "10341561";
        connection.getHttpMock().addResponse(org.andstatus.app.tests.R.raw.loadaverage_repost_response);
        AActivity activity = connection.announce(NOTE_OID);
        assertEquals(ActivityType.ANNOUNCE, activity.type);
        Note note = activity.getNote();
        assertEquals("Note oid" + note, NOTE_OID, note.oid);
        assertEquals("conversationOid", "9118253", note.conversationOid);
        assertEquals(1, connection.getHttpMock().getRequestsCounter());
        HttpReadResult result = connection.getHttpMock().getResults().get(0);
        assertTrue("URL doesn't contain note oid: " + result.getUrl(), result.getUrl().contains(NOTE_OID));
        assertEquals("Activity oid; " + activity, "10341833", activity.getTimelinePosition().getPosition());
        assertEquals("Actor; " + activity, "andstatus@loadaverage.org", activity.getActor().getWebFingerId());
        assertEquals("Author; " + activity, "igor@herds.eu", activity.getAuthor().getWebFingerId());
    }


    @Test
    public void testFavoritingActivity() {
        String contentOfFavoritedNote = "the favorited note";
        AActivity activity1 = getFavoritingActivity("10238",
                "somebody favorited something by anotheractor: " + contentOfFavoritedNote,
                contentOfFavoritedNote);
        AActivity activity2 = getFavoritingActivity("10239",
                "anotherman favorited something by anotheractor: " + contentOfFavoritedNote,
                contentOfFavoritedNote);
        assertEquals("Should have the same content", activity1.getNote().getContent(), activity2.getNote().getContent());

        final String content2 = "a status by somebody@somewhere.org";
        getFavoritingActivity("10240", "oneman favourited " + content2, content2);
    }

    @NonNull
    private AActivity getFavoritingActivity(String favoritingOid, String favoritingContent, String likedContent) {
        Actor accountActor = demoData.getAccountActorByOid(demoData.gnusocialTestAccountActorOid);
        String actorOid = favoritingOid + "1";
        Actor actor = Actor.fromOriginAndActorOid(accountActor.origin, actorOid);
        long favoritingUpdateDate = System.currentTimeMillis() - 1000000;
        AActivity activityIn = AActivity.newPartialNote(accountActor, actor, favoritingOid, favoritingUpdateDate,
                DownloadStatus.LOADED);

        activityIn.getNote().setContent(favoritingContent);

        AActivity activity = ConnectionTwitterGnuSocial.createLikeActivity(activityIn);
        assertEquals("Should become LIKE activity " + activityIn, ActivityType.LIKE , activity.type);
        final Note note = activity.getNote();
        assertEquals("Should strip favoriting prefix " + activityIn, likedContent, note.getContent());
        assertEquals("Note updatedDate should be 1 " + activity, SOME_TIME_AGO, note.getUpdatedDate());
        return activity;
    }

    @Test
    public void testFavoritingActivityInTimeline() throws IOException {
        connection.getHttpMock().addResponse(org.andstatus.app.tests.R.raw.loadaverage_favoriting_activity);

        String accountActorOid = demoData.gnusocialTestAccountActorOid;
        List<AActivity> timeline = connection.getTimeline(ApiRoutineEnum.SEARCH_NOTES,
                new TimelinePosition("2656388"), TimelinePosition.EMPTY, 20, accountActorOid);
        assertNotNull("timeline returned", timeline);
        assertEquals("Number of items in the Timeline", 2, timeline.size());

        int ind = 0;
        AActivity activity = timeline.get(ind);
        assertEquals("Posting a note " + activity, AObjectType.NOTE, activity.getObjectType());
        assertEquals("Should be UPDATE " + activity, ActivityType.UPDATE,  activity.type);
        assertEquals("Timeline position", "12940131", activity.getTimelinePosition().getPosition());
        assertEquals("Note Oid", "12940131", activity.getNote().oid);
        assertEquals("conversationOid", "10538185", activity.getNote().conversationOid);
        assertEquals("Favorited " + activity, TriState.UNKNOWN, activity.getNote().getFavoritedBy(activity.accountActor));
        assertEquals("Actor Oid", "379323", activity.getActor().oid);
        assertEquals("Actor Username", "colegota", activity.getActor().getUsername());
        assertEquals("Author Oid", "379323", activity.getAuthor().oid);
        assertEquals("Author Username", "colegota", activity.getAuthor().getUsername());
        final String contentPrefix = "@<a href=\"https://linuxinthenight.com/user/1\" class";
        assertTrue("Content " + activity, activity.getNote().getContent().startsWith(contentPrefix));
        // TODO: assertTrue("Should have a recipient", activity.recipients().nonEmpty());

        ind++;
        activity = timeline.get(ind);
        assertEquals("Should be LIKE " + activity, ActivityType.LIKE,  activity.type);
        assertEquals("Timeline position", "12942571", activity.getTimelinePosition().getPosition());
        assertEquals("Actor Oid", "347578", activity.getActor().oid);
        assertEquals("Actor Username", "fanta", activity.getActor().getUsername());
        assertEquals("Author Oid", "379323", activity.getAuthor().oid);
        assertEquals("Note Oid", "12940131", activity.getNote().oid);
        assertTrue("Should not have a recipient", activity.recipients().isEmpty());

        assertTrue("Content " + activity, activity.getNote().getContent().startsWith(contentPrefix));

        assertTrue("inReplyTo should be empty " + activity , activity.getNote().getInReplyTo().isEmpty());
        assertEquals("Updated date should be 1 for favorited note", SOME_TIME_AGO,
                activity.getNote().getUpdatedDate());
        assertEquals("Activity updated at " + TestSuite.utcTime(activity.getUpdatedDate()),
                TestSuite.utcTime(2018, Calendar.JUNE, 1, 17, 4, 57),
                TestSuite.utcTime(activity.getUpdatedDate()));
    }

}
