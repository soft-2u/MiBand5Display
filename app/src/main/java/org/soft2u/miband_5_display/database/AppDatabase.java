package org.soft2u.miband_5_display.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import org.soft2u.miband_5_display.dao.ThemeByUserDAO;
import org.soft2u.miband_5_display.dao.ThemeFavoriteDAO;
import org.soft2u.miband_5_display.dao.ThemeHistoryDAO;
import org.soft2u.miband_5_display.dao.ThemeRequestDAO;
import org.soft2u.miband_5_display.dao.ThemeUserMakingDAO;
import org.soft2u.miband_5_display.entity.ThemeMadeByUser;
import org.soft2u.miband_5_display.entity.ThemeFavorite;
import org.soft2u.miband_5_display.entity.ThemeHistory;
import org.soft2u.miband_5_display.entity.ThemeRequest;
import org.soft2u.miband_5_display.entity.ThemeUserMaking;
import org.soft2u.miband_5_display.utils.DateConverter;

@Database(entities = {ThemeFavorite.class, ThemeHistory.class, ThemeRequest.class, ThemeUserMaking.class, ThemeMadeByUser.class}, version = 2)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "user-database")
                    // allow queries on the crop thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE tblThemeRequest (themeid INTEGER NOT NULL, date_add INTEGER, request_id TEXT, request_content TEXT, request_status_code INTEGER NOT NULL, PRIMARY KEY(themeid))");
            database.execSQL("CREATE TABLE tblThemeUserMaking (id INTEGER NOT NULL, base_theme_id INTEGER NOT NULL, theme_json TEXT, PRIMARY KEY(id))");
            database.execSQL("CREATE TABLE tblThemeMadeByUser (publish_id TEXT NOT NULL, date_add INTEGER, cover_url TEXT, public_status_code INTEGER NOT NULL, PRIMARY KEY(publish_id))");
        }
    };

//    public static void destroyInstance() {
//        INSTANCE = null;
//    }

    // ThemeFavoriteDAO is a class annotated with @Dao.
    public abstract ThemeFavoriteDAO ThemeFavoriteDAO();
    public abstract ThemeHistoryDAO ThemeHistoryDAO();
    public abstract ThemeRequestDAO ThemeRequestDAO();
    public abstract ThemeUserMakingDAO ThemeUserMakingDAO();
    public abstract ThemeByUserDAO ThemeByUserDAO();
}
