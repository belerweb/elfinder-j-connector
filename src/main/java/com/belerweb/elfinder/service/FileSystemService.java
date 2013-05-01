package com.belerweb.elfinder.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.StringUtils;
import org.hsqldb.Server;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.belerweb.elfinder.bean.Target;

@Service
public class FileSystemService implements InitializingBean {

  private static final String CONFIG = "elfinder.root";
  private static final Boolean TRUE = new Boolean(true);

  private File rootDir;
  private QueryRunner runner;
  private Connection conn;

  public Map<String, Object> getCwd(Target target) throws SQLException {
    return runner.query(conn, "SELECT * FROM VOLUME_" + target.getVolume() + " WHERE HASH = ?",
        new MapHandler(), target.getHash());
  }

  public List<Map<String, Object>> getFiles(Target target, Map<String, Object> cwd, Boolean tree)
      throws SQLException {
    List<Object> parents = new ArrayList<Object>();
    parents.add(cwd.get("ID"));
    if (TRUE.equals(tree)) {
      List<Map<String, Object>> pIds =
          runner.query(conn, "SELECT ID FROM VOLUME_" + target.getVolume()
              + " WHERE LFT<? AND RGT>?", new MapListHandler(), cwd.get("LFT"), cwd.get("RGT"));
      for (Map<String, Object> map : pIds) {
        parents.add(map.get("ID"));
      }
      parents.add(0);
    }
    return runner.query(conn, "SELECT * FROM VOLUME_" + target.getVolume() + " WHERE PID IN("
        + StringUtils.join(parents, ",") + ") ORDER BY LFT ASC", new MapListHandler());
  }

  public void add(Target target, Map<String, Object> cwd, Map<String, Object> obj)
      throws SQLException {
    Integer rgt = (Integer) cwd.get("RGT");
    Object[] param = new Object[20];
    param[0] = null;// ID
    param[1] = cwd.get("ID");// PID
    param[2] = rgt;// LFT
    param[3] = rgt + 1;// RGT
    param[4] = obj.get("name");// NAME
    param[5] = obj.get("path");// PATH
    param[6] = obj.get("size");// SIZE
    param[7] = obj.get("read");// READ
    param[8] = obj.get("write");// WRITE
    param[9] = obj.get("locked");// LOCKED
    param[10] = obj.get("dirs");// DIRS
    param[11] = obj.get("mime");// MIME
    param[12] = obj.get("tmb");// TMB
    param[13] = obj.get("dim");// DIM
    param[14] = obj.get("alias");// ALIAS
    param[15] = obj.get("thash");// THASH
    param[16] = null;// VOLUMEID
    param[17] = obj.get("hash");// HASH
    param[18] = obj.get("phash");// PHASH
    param[19] = obj.get("ts");// TS
    runner.update(conn, "UPDATE VOLUME_" + target.getVolume()
        + " SET LFT=LFT+2,RGT=RGT+2 WHERE LFT>?", rgt);
    runner.update(conn, "UPDATE VOLUME_" + target.getVolume()
        + " SET RGT=RGT+2 WHERE LFT<=? AND RGT>=?", cwd.get("LFT"), rgt);
    runner.update(conn, "INSERT INTO VOLUME_" + target.getVolume()
        + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", param);
  }

  public void delete(String volume, Map<String, Object> cwd) throws SQLException {
    Integer lft = (Integer) cwd.get("LFT");
    Integer rgt = (Integer) cwd.get("RGT");
    List<Map<String, Object>> items =
        runner.query(conn, "SELECT * FROM VOLUME_" + volume + " WHERE LFT>=? AND RGT<=?",
            new MapListHandler(), lft, rgt);
    runner.update(conn, "DELETE FROM VOLUME_" + volume + " WHERE LFT>=? AND RGT<=?", lft, rgt);
    Integer step = rgt - lft + 1;
    runner.update(conn, "UPDATE VOLUME_" + volume + " SET RGT=RGT-? WHERE LFT<? AND RGT>?", step,
        lft, rgt);
    runner.update(conn, "UPDATE VOLUME_" + volume + " SET LFT=LFT-?, RGT=RGT-? WHERE LFT>?", step,
        step, rgt);
    for (Map<String, Object> item : items) {
      if (item.get("PATH") != null) {
        // TODO delete files
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String dir = System.getProperty(CONFIG, System.getenv(CONFIG));
    Assert.notNull(dir, "Please config elfinder.root in system properties or envionment variables");
    rootDir = new File(dir);
    Assert.isTrue(rootDir.isDirectory() && rootDir.canWrite(), dir
        + " should be a writeable directory");
    initDatabase();
  }

  private void initDatabase() throws ClassNotFoundException, SQLException {
    Server embedDb = new Server();
    embedDb.setAddress("localhost");
    embedDb.setDatabaseName(0, "main");
    embedDb.setDatabasePath(0, new File(rootDir, "files").getAbsolutePath());
    embedDb.start();

    DbUtils.loadDriver("org.hsqldb.jdbcDriver");
    runner = new QueryRunner();
    conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/main", "sa", "");
    ResultSet set = conn.getMetaData().getTables(null, null, "VOLUME", null);
    if (!set.next()) {
      runner.update(conn,
          "CREATE TABLE VOLUME(ID SMALLINT NOT NULL PRIMARY KEY,NAME VARCHAR(16) NOT NULL)");
      runner.update(conn, "INSERT INTO VOLUME VALUES (0, '/')");
      runner.update(conn, getCreateVolumeSQL(0));
      runner.update(conn, "ALTER TABLE VOLUME_0 ALTER COLUMN ID RESTART WITH 1");
      String rootDirSql =
          "INSERT INTO VOLUME_0(PID, LFT, RGT, NAME, SIZE,"
              + " READ, WRITE, LOCKED, MIME, VOLUMEID, HASH, TS)"
              + " VALUES (0, 1, 2, '/', 0, 1, 1, 0, 'directory', '0_', '0_1', "
              + System.currentTimeMillis() + ")";
      runner.update(conn, rootDirSql);
    }
  }

  private String getCreateVolumeSQL(int id) {
    return "CREATE TABLE VOLUME_" + id + " (" + "ID INTEGER NOT NULL PRIMARY KEY IDENTITY,"
        + "PID INTEGER NOT NULL," + "LFT INTEGER NOT NULL," + "RGT INTEGER NOT NULL,"
        + "NAME VARCHAR(256) NOT NULL," + "PATH VARCHAR(256)," + "SIZE BIGINT NOT NULL,"
        + "READ TINYINT NOT NULL," + "WRITE TINYINT NOT NULL," + "LOCKED TINYINT NOT NULL,"
        + "DIRS TINYINT," + "MIME VARCHAR(256) NOT NULL," + "TMB VARCHAR(256),"
        + "DIM VARCHAR(16)," + "ALIAS VARCHAR(256)," + "THASH VARCHAR(256),"
        + "VOLUMEID VARCHAR(4)," + "HASH VARCHAR(256) NOT NULL," + "PHASH VARCHAR(256),"
        + "TS BIGINT NOT NULL" + ")";
  }

}
