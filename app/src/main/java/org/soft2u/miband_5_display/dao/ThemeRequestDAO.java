package org.soft2u.miband_5_display.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.soft2u.miband_5_display.entity.ThemeHistory;
import org.soft2u.miband_5_display.entity.ThemeRequest;

import java.util.List;

@Dao
public interface ThemeRequestDAO {
    @Query("SELECT * FROM tblThemeRequest ORDER BY date_add DESC")
    List<ThemeRequest> getAll();

    @Query("SELECT * FROM tblThemeRequest ORDER BY date_add DESC LIMIT 12 OFFSET :startfrom")
    List<ThemeRequest> getList(int startfrom);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ThemeRequest... themeRequests);

    @Delete
    void delete(ThemeRequest... themeRequests);

    @Query("DELETE FROM tblThemeRequest")
    void deleteAll();

//    @Query("DELETE from tblThemeHistory WHERE themeid = :themeId")
//    int delete(int themeId);
}
