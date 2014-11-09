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

package org.andstatus.app.service;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;

import org.andstatus.app.R;
import org.andstatus.app.account.MySimpleAdapter;
import org.andstatus.app.context.MyContextHolder;
import org.andstatus.app.context.MyPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class QueueViewer extends ListActivity {
    private static final String KEY_QUEUE_TYPE = "queue_type";
    private static final String KEY_COMMAND_SUMMARY = "command_summary";
    private static final String KEY_RESULT_SUMMARY = "result_summary";
    private List<QueueData> mListData = null;

    private static class QueueData {
        QueueType queueType;
        CommandData commandData;
        
        static QueueData getNew(QueueType queueType, CommandData commandData) {
            QueueData queueData = new QueueData();
            queueData.queueType = queueType;
            queueData.commandData = commandData;
            return queueData;
        }

        public long getId() {
            return commandData.hashCode();
        }

        @Override
        public String toString() {
            return toSubject()
                    + " \n" + commandData.getResult().toSummary();
        }

        public String toSubject() {
            return queueType.getAcronym() + "; "
                    + commandData.toCommandSummary(MyContextHolder.get());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyServiceManager.setServiceUnavailable();
        MyServiceManager.stopService();
        super.onCreate(savedInstanceState);
        MyPreferences.loadTheme(this);
        setContentView(R.layout.queue);
        showList();
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.queue_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            return super.onContextItemSelected(item);
        }
        QueueData queueData = queueDataFromId(info.id);
        if (queueData == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.menuItemShare:
                share(queueData);
                return true;
            case R.id.menuItemResend:
                if (MyServiceManager.getServiceState() == MyServiceState.STOPPED) {
                    queueData.commandData.resetRetries();
                    queueData.commandData.setManuallyLaunched(true);
                    Queue<CommandData> mainCommandQueue = new PriorityBlockingQueue<CommandData>(100);
                    CommandData.loadQueue(this, mainCommandQueue, QueueType.CURRENT);
                    if (mainCommandQueue.offer(queueData.commandData)) {
                        CommandData.saveQueue(this, mainCommandQueue, QueueType.CURRENT);
                        showList();
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void share(QueueData queueData) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, queueData.toSubject());
        intent.putExtra(Intent.EXTRA_TEXT, queueData.toString());
        startActivity(Intent.createChooser(intent, getText(R.string.menu_item_share)));        
    }

    private QueueData queueDataFromId(long id) {
        if (mListData == null) {
            return null;
        }
        for (QueueData queueData : mListData) {
            if (queueData.getId() == id) {
                return queueData;
            }
        }
        return null;
    }
    
    private void showList() {
        mListData = newListData();
        setListAdapter(newListAdapter(mListData));
    }

    private List<QueueData> newListData() {
        List<QueueData> listData = new ArrayList<QueueData>();
        loadQueue(listData, QueueType.CURRENT);
        loadQueue(listData, QueueType.RETRY);
        loadQueue(listData, QueueType.ERROR);
        return listData;
    }

    private void loadQueue(List<QueueData> listData, QueueType queueType) {
        Queue<CommandData> queue = new PriorityBlockingQueue<CommandData>(100);
        CommandData.loadQueue(this, queue, queueType);
        for (CommandData commandData : queue) {
            listData.add(QueueData.getNew(queueType, commandData));
        }
    }

    private ListAdapter newListAdapter(List<QueueData> queueDataList) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (QueueData queueData : queueDataList) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(KEY_QUEUE_TYPE, queueData.queueType.getAcronym());
            map.put(KEY_COMMAND_SUMMARY, queueData.commandData.toCommandSummary(MyContextHolder.get()));
            map.put(KEY_RESULT_SUMMARY, queueData.commandData.getResult().toSummary());
            map.put(BaseColumns._ID, Long.toString(queueData.getId()));
            list.add(map);
        }
        if (list.isEmpty()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(KEY_QUEUE_TYPE, "-");
            map.put(KEY_COMMAND_SUMMARY, getText(R.string.empty_in_parenthesis).toString());
            map.put(KEY_RESULT_SUMMARY, "-");
            map.put(BaseColumns._ID, "0");
            list.add(map);
        }
        
        ListAdapter adapter = new MySimpleAdapter(this, 
                list, 
                R.layout.queue_item, 
                new String[] {KEY_QUEUE_TYPE, KEY_COMMAND_SUMMARY, KEY_RESULT_SUMMARY, BaseColumns._ID}, 
                new int[] {R.id.queue_type, R.id.command_summary, R.id.result_summary, R.id.id});
        return adapter;
    }
    
}
