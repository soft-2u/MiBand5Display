package org.soft2u.miband_5_display.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import org.soft2u.miband_5_display.entity.ThemeFavorite;
import org.soft2u.miband_5_display.entity.ThemeHistory;

import java.util.List;

@Dao
public interface ThemeFavoriteDAO {
    @Query("SELECT * FROM tblThemeFavorite ORDER BY date_add DESC")
    List<ThemeFavorite> getAll();

    @Query("SELECT * FROM tblThemeFavorite ORDER BY date_add DESC LIMIT 12 OFFSET :startfrom")
    List<ThemeFavorite> getList(int startfrom);

    @Insert
    void insertAll(ThemeFavorite... themeFavorites);

    @Query("DELETE from tblThemeFavorite WHERE themeid = :themeId")
    int delete(int themeId);
}
