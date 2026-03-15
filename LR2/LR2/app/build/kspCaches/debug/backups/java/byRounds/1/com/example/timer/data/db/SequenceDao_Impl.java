package com.example.timer.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.timer.data.model.Sequence;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SequenceDao_Impl implements SequenceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Sequence> __insertionAdapterOfSequence;

  private final EntityDeletionOrUpdateAdapter<Sequence> __deletionAdapterOfSequence;

  private final EntityDeletionOrUpdateAdapter<Sequence> __updateAdapterOfSequence;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public SequenceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSequence = new EntityInsertionAdapter<Sequence>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sequences` (`id`,`name`,`color`,`warmupDuration`,`workDuration`,`restDuration`,`cooldownDuration`,`cycles`,`restBetweenCycles`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Sequence entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getColor());
        statement.bindLong(4, entity.getWarmupDuration());
        statement.bindLong(5, entity.getWorkDuration());
        statement.bindLong(6, entity.getRestDuration());
        statement.bindLong(7, entity.getCooldownDuration());
        statement.bindLong(8, entity.getCycles());
        statement.bindLong(9, entity.getRestBetweenCycles());
      }
    };
    this.__deletionAdapterOfSequence = new EntityDeletionOrUpdateAdapter<Sequence>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sequences` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Sequence entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSequence = new EntityDeletionOrUpdateAdapter<Sequence>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sequences` SET `id` = ?,`name` = ?,`color` = ?,`warmupDuration` = ?,`workDuration` = ?,`restDuration` = ?,`cooldownDuration` = ?,`cycles` = ?,`restBetweenCycles` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Sequence entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getColor());
        statement.bindLong(4, entity.getWarmupDuration());
        statement.bindLong(5, entity.getWorkDuration());
        statement.bindLong(6, entity.getRestDuration());
        statement.bindLong(7, entity.getCooldownDuration());
        statement.bindLong(8, entity.getCycles());
        statement.bindLong(9, entity.getRestBetweenCycles());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sequences";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Sequence sequence, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSequence.insertAndReturnId(sequence);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Sequence sequence, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSequence.handle(sequence);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Sequence sequence, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSequence.handle(sequence);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Sequence>> getAll() {
    final String _sql = "SELECT * FROM sequences ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"sequences"}, false, new Callable<List<Sequence>>() {
      @Override
      @Nullable
      public List<Sequence> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfWarmupDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "warmupDuration");
          final int _cursorIndexOfWorkDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "workDuration");
          final int _cursorIndexOfRestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "restDuration");
          final int _cursorIndexOfCooldownDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldownDuration");
          final int _cursorIndexOfCycles = CursorUtil.getColumnIndexOrThrow(_cursor, "cycles");
          final int _cursorIndexOfRestBetweenCycles = CursorUtil.getColumnIndexOrThrow(_cursor, "restBetweenCycles");
          final List<Sequence> _result = new ArrayList<Sequence>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Sequence _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpColor;
            _tmpColor = _cursor.getInt(_cursorIndexOfColor);
            final int _tmpWarmupDuration;
            _tmpWarmupDuration = _cursor.getInt(_cursorIndexOfWarmupDuration);
            final int _tmpWorkDuration;
            _tmpWorkDuration = _cursor.getInt(_cursorIndexOfWorkDuration);
            final int _tmpRestDuration;
            _tmpRestDuration = _cursor.getInt(_cursorIndexOfRestDuration);
            final int _tmpCooldownDuration;
            _tmpCooldownDuration = _cursor.getInt(_cursorIndexOfCooldownDuration);
            final int _tmpCycles;
            _tmpCycles = _cursor.getInt(_cursorIndexOfCycles);
            final int _tmpRestBetweenCycles;
            _tmpRestBetweenCycles = _cursor.getInt(_cursorIndexOfRestBetweenCycles);
            _item = new Sequence(_tmpId,_tmpName,_tmpColor,_tmpWarmupDuration,_tmpWorkDuration,_tmpRestDuration,_tmpCooldownDuration,_tmpCycles,_tmpRestBetweenCycles);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super Sequence> $completion) {
    final String _sql = "SELECT * FROM sequences WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Sequence>() {
      @Override
      @Nullable
      public Sequence call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfWarmupDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "warmupDuration");
          final int _cursorIndexOfWorkDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "workDuration");
          final int _cursorIndexOfRestDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "restDuration");
          final int _cursorIndexOfCooldownDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldownDuration");
          final int _cursorIndexOfCycles = CursorUtil.getColumnIndexOrThrow(_cursor, "cycles");
          final int _cursorIndexOfRestBetweenCycles = CursorUtil.getColumnIndexOrThrow(_cursor, "restBetweenCycles");
          final Sequence _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpColor;
            _tmpColor = _cursor.getInt(_cursorIndexOfColor);
            final int _tmpWarmupDuration;
            _tmpWarmupDuration = _cursor.getInt(_cursorIndexOfWarmupDuration);
            final int _tmpWorkDuration;
            _tmpWorkDuration = _cursor.getInt(_cursorIndexOfWorkDuration);
            final int _tmpRestDuration;
            _tmpRestDuration = _cursor.getInt(_cursorIndexOfRestDuration);
            final int _tmpCooldownDuration;
            _tmpCooldownDuration = _cursor.getInt(_cursorIndexOfCooldownDuration);
            final int _tmpCycles;
            _tmpCycles = _cursor.getInt(_cursorIndexOfCycles);
            final int _tmpRestBetweenCycles;
            _tmpRestBetweenCycles = _cursor.getInt(_cursorIndexOfRestBetweenCycles);
            _result = new Sequence(_tmpId,_tmpName,_tmpColor,_tmpWarmupDuration,_tmpWorkDuration,_tmpRestDuration,_tmpCooldownDuration,_tmpCycles,_tmpRestBetweenCycles);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
