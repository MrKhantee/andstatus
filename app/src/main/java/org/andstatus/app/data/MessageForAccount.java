/*
 * Copyright (C) 2014 yvolk (Yuri Volkov), http://yurivolkov.com
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

package org.andstatus.app.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import org.andstatus.app.account.MyAccount;
import org.andstatus.app.context.MyContextHolder;
import org.andstatus.app.database.DownloadTable;
import org.andstatus.app.database.FriendshipTable;
import org.andstatus.app.database.MsgOfUserTable;
import org.andstatus.app.database.MsgTable;
import org.andstatus.app.origin.Origin;
import org.andstatus.app.timeline.Timeline;
import org.andstatus.app.timeline.TimelineType;
import org.andstatus.app.util.I18n;
import org.andstatus.app.util.MyHtml;

/**
 * Helper class to find out a relation of a Message to MyAccount 
 * @author yvolk@yurivolkov.com
 */
public class MessageForAccount {
    public final long msgId;
    public final Origin origin;
    public DownloadStatus status = DownloadStatus.UNKNOWN;
    public String bodyTrimmed = "";
    public long authorId = 0;
    public long senderId = 0;
    private boolean isSenderMySucceededAccount = false;
    public long inReplyToUserId = 0;
    public long recipientId = 0;
    public boolean mayBePrivate = false;
    public String imageFilename = null;

    private final MyAccount myAccount;
    private final long userId;
    public boolean isSubscribed = false;
    public boolean isAuthor = false;
    public boolean isSender = false;
    public boolean isRecipient = false;
    public boolean favorited = false;
    public boolean reblogged = false;
    public boolean senderFollowed = false;
    public boolean authorFollowed = false;
    
    public MessageForAccount(long msgId, long originId, MyAccount myAccount) {
        this.msgId = msgId;
        this.origin = MyContextHolder.get().persistentOrigins().fromId(originId);
        this.myAccount = calculateMyAccount(origin, myAccount);
        this.userId = myAccount.getUserId();
        if (myAccount.isValid()) {
            getData();
        }
    }

    @NonNull
    private MyAccount calculateMyAccount(Origin origin, MyAccount maIn) {
        MyAccount ma = maIn;
        if (ma == null || !ma.getOrigin().equals(origin) || !ma.isValid()) {
            ma = MyContextHolder.get().persistentAccounts().getFirstSucceededForOrigin(origin);
        }
        if (ma == null) {
            ma = MyAccount.getEmpty();
        }
        return ma;
    }

    private void getData() {
        // Get a database row for the currently selected item
        Uri uri = MatchedUri.getTimelineItemUri(
                Timeline.getTimeline(TimelineType.MESSAGES_TO_ACT, myAccount, 0, null), msgId);
        Cursor cursor = null;
        try {
            cursor = MyContextHolder.get().context().getContentResolver().query(uri, new String[]{
                    BaseColumns._ID,
                    MsgTable.MSG_STATUS,
                    MsgTable.BODY, MsgTable.SENDER_ID,
                    MsgTable.AUTHOR_ID,
                    MsgTable.IN_REPLY_TO_USER_ID,
                    MsgTable.RECIPIENT_ID,
                    MsgOfUserTable.SUBSCRIBED,
                    MsgOfUserTable.FAVORITED,
                    MsgOfUserTable.REBLOGGED,
                    FriendshipTable.SENDER_FOLLOWED,
                    FriendshipTable.AUTHOR_FOLLOWED,
                    DownloadTable.IMAGE_FILE_NAME
            }, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                status = DownloadStatus.load(DbUtils.getLong(cursor, MsgTable.MSG_STATUS));
                authorId = DbUtils.getLong(cursor, MsgTable.AUTHOR_ID);
                senderId = DbUtils.getLong(cursor, MsgTable.SENDER_ID);
                isSenderMySucceededAccount = MyContextHolder.get().persistentAccounts().fromUserId(senderId).isValidAndSucceeded();
                recipientId = DbUtils.getLong(cursor, MsgTable.RECIPIENT_ID);
                imageFilename = DbUtils.getString(cursor, DownloadTable.IMAGE_FILE_NAME);
                bodyTrimmed = I18n.trimTextAt(MyHtml.fromHtml(
                        DbUtils.getString(cursor, MsgTable.BODY)), 80).toString();
                inReplyToUserId = DbUtils.getLong(cursor, MsgTable.IN_REPLY_TO_USER_ID);
                mayBePrivate = (recipientId != 0) || (inReplyToUserId != 0);
                
                isRecipient = (userId == recipientId) || (userId == inReplyToUserId);
                isSubscribed = DbUtils.getBoolean(cursor, MsgOfUserTable.SUBSCRIBED);
                favorited = DbUtils.getBoolean(cursor, MsgOfUserTable.FAVORITED);
                reblogged = DbUtils.getBoolean(cursor, MsgOfUserTable.REBLOGGED);
                senderFollowed = DbUtils.getBoolean(cursor, FriendshipTable.SENDER_FOLLOWED);
                authorFollowed = DbUtils.getBoolean(cursor, FriendshipTable.AUTHOR_FOLLOWED);
                isSender = (userId == senderId);
                isAuthor = (userId == authorId);
            }
        } finally {
            DbUtils.closeSilently(cursor);
        }
    }

    public MyAccount getMyAccount() {
        return myAccount;
    }
    
    public boolean isDirect() {
        return recipientId != 0;
    }

    public boolean isTiedToThisAccount() {
        return isRecipient || favorited || reblogged || isSender
                || senderFollowed || authorFollowed;
    }

    public boolean hasPrivateAccess() {
        return isRecipient || isSender;
    }

    public boolean isLoaded() {
        return status == DownloadStatus.LOADED;
    }

    public boolean isSenderMySucceededAccount() {
        return isSenderMySucceededAccount;
    }
}
