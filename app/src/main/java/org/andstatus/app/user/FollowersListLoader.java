/*
 * Copyright (c) 2016 yvolk (Yuri Volkov), http://yurivolkov.com
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

package org.andstatus.app.user;

import org.andstatus.app.account.MyAccount;
import org.andstatus.app.data.MyDatabase;
import org.andstatus.app.data.MyQuery;
import org.andstatus.app.util.MyLog;

/**
 * @author yvolk@yurivolkov.com
 */
public class FollowersListLoader extends UserListLoader {
    private long followedUserId;
    private final String userName;

    public FollowersListLoader(UserListType userListType, MyAccount ma, long centralItemId, boolean isListCombined) {
        super(userListType, ma, centralItemId, isListCombined);
        followedUserId = centralItemId;
        userName = MyQuery.userIdToWebfingerId(followedUserId);
    }

    @Override
    protected void loadInternal() {
        MyLog.v(this, "Followers of " + userName);
        populateItems();
    }

    protected String getSqlUserIds() {
        String sql = "SELECT " + MyDatabase.FollowingUser.USER_ID
                + " FROM " + MyDatabase.FollowingUser.TABLE_NAME
                + " WHERE " + MyDatabase.FollowingUser.FOLLOWED_USER_ID + "=" + followedUserId
                + " AND " + MyDatabase.FollowingUser.USER_FOLLOWED + "=1";
        return " IN (" + sql + ")";
    }

    @Override
    protected String getTitle() {
        return userName;
    }
}