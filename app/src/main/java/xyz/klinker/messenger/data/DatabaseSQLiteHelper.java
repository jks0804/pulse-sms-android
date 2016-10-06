/*
 * Copyright (C) 2016 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import xyz.klinker.messenger.data.model.Blacklist;
import xyz.klinker.messenger.data.model.Contact;
import xyz.klinker.messenger.data.model.Conversation;
import xyz.klinker.messenger.data.model.Draft;
import xyz.klinker.messenger.data.model.Message;
import xyz.klinker.messenger.data.model.ScheduledMessage;
import xyz.klinker.messenger.encryption.EncryptionUtils;

/**
 * Handles creating and updating databases.
 */
public class DatabaseSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "messenger.db";
    private static final int DATABASE_VERSION = 5;

    private DatabaseTable[] tables = {
            new Contact(),
            new Conversation(),
            new Message(),
            new Draft(),
            new ScheduledMessage(),
            new Blacklist()
    };

    /**
     * Construct a new database helper.
     *
     * @param context the current application context.
     */
    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (DatabaseTable table : tables) {
            db.execSQL(table.getCreateStatement());

            for (String index : table.getIndexStatements()) {
                db.execSQL(index);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // upgrade the database depending on version changes
        // for example, if old version was 1, then we need to execute all changes from 2 through the
        // newest version

        if (oldVersion < 2) {
             try {
                 db.execSQL("ALTER TABLE conversation ADD COLUMN archive integer not null DEFAULT 0");
             } catch(Exception e) { }
        }

        if (oldVersion < 3) {
             try {
                 db.execSQL(new Contact().getCreateStatement());
             } catch(Exception e) { }
        }

        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE conversation ADD COLUMN private_notifications integer not null DEFAULT 0");
            } catch(Exception e) { }
        }

        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE conversation ADD COLUMN led_color integer not null DEFAULT " + Color.WHITE); // white default
            } catch(Exception e) { }
        }
    }

    public void onDrop(SQLiteDatabase db) {
        for (DatabaseTable table : tables) {
            db.execSQL("drop table if exists " + table.getTableName());
        }
    }

    public interface DatabaseTable {

        String getCreateStatement();

        String getTableName();

        String[] getIndexStatements();

        void fillFromCursor(Cursor cursor);

        void encrypt(EncryptionUtils utils);

        void decrypt(EncryptionUtils utils);

    }

}
