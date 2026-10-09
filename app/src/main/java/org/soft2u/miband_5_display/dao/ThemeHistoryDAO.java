package org.soft2u.miband_5_display.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.soft2u.miband_5_display.entity.ThemeHistory;

import java.util.List;

@Dao
public interface ThemeHistoryDAO {
    @Query("SELECT * FROM tblThemeHistory ORDER BY date_add DESC")
    List<ThemeHistory> getAll();

    @Query("SELECT * FROM tblThemeHistory ORDER BY date_add DESC LIMIT 12 OFFSET :startfrom")
    List<ThemeHistory> getList(int startfrom);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ThemeHistory... themeHistories);

    @Delete
    void delete(ThemeHistory... themeHistories);

    @Query("DELETE FROM tblThemeHistory")
    void deleteAll();

//    @Query("DELETE from tblThemeHistory WHERE themeid = :themeId")
//    int delete(int themeId);
}
