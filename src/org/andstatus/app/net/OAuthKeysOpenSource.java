/**
 * Copyright (C) 2012 yvolk (Yuri Volkov), http://yurivolkov.com
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
package org.andstatus.app.net;

/**
 * Keys of the "AndStatus-OpenSource" application.
 * @author yvolk
 */
public class OAuthKeysOpenSource implements OAuthKeysStrategy {
    @Override
    public String getConsumerKey(long originId) {
        if(originId == 1) {  // Twitter
            return "XPHj81OgjphGlN6Jb55Kmg";
        } else {
            return "";
        }
    }
    @Override
    public String getConsumerSecret(long originId) {
        if(originId == 1) {  // Twitter
            return "o2E5AYoDQhZf9qT7ctHLGihpq2ibc5bC4iFAOHURxw";
        } else {
            return "";
        }
    }
}