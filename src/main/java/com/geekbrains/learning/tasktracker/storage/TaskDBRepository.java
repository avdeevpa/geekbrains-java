package com.geekbrains.learning.tasktracker.storage;
/**
 * To hide private info created class Shadow:
 *
 * public class Shadow {
 *     static final String HOST = "host:1521:sid";
 *     static final String USER = "user";
 *     static final String PASS = "pass";
 *     static final String BASE = "scott";
 *     static final String TAB_PREFIX = "java_";
 * }
 *
 * .gitignore added
 */
import com.geekbrains.learning.tasktracker.exceptions.TTStorageException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDBRepository implements TaskInterface{
    private Connection connection;

    public TaskDBRepository() {
        try {
            connect();
            if(!isTableExists()){
                createTableEx();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public TaskDBRepository(boolean flushDb) {
        this();
        if (flushDb) {
            try {
                flushDB();
                createTableEx();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.OracleDriver");
        connection = DriverManager.getConnection("jdbc:oracle:thin:@"+Shadow.HOST +"?characterEncoding=win1251" , Shadow.USER, Shadow.PASS);
    }

    private boolean isTableExists() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT count(*) FROM all_tables WHERE owner=upper('" + Shadow.BASE + "') and table_name = upper('" +Shadow.TAB_PREFIX + "tasks')");
        if(rs.next() && rs.getInt(1) == 1) {
            return true;
        }
        return false;
    }

    private void flushDB() throws SQLException {
        connection.createStatement().execute(
                "DROP TABLE " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks "
        );
    }

    private void createTableEx() throws SQLException {
        connection.createStatement().execute(
            "CREATE TABLE " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks (\n" +
                "  id number,\n" +
                "  caption varchar2(200),\n" +
                "  owner varchar2(20),\n" +
                "  assigned varchar2(20),\n" +
                "  description varchar2(20),\n" +
                "  status varchar2(20)\n" +
            ")"
        );
    }

    @Override
    public Task addEdtTask(Task task) throws TTStorageException {
        PreparedStatement ps;
        ResultSet rs = null;
        try {
            if (task.getId() == null) {
                ps = connection.prepareCall(
                        "select nvl(max(id), 0)+1 from " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks \n"
                );
                rs = ps.executeQuery();
                rs.next();
                task.setId(rs.getLong(1));

                ps = connection.prepareCall(
                        "INSERT INTO " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks VALUES (?, ?, ?, ?, ?, ?)"
                );
                ps.setLong(1, task.getId());
                ps.setString(2, task.getCaption());
                ps.setString(3, task.getOwner());
                ps.setString(4, task.getAssigned());
                ps.setString(5, task.getDescription());
                ps.setString(6, task.getStatus().toString());
                ps.execute();
            } else {
                ps = connection.prepareCall(
                        "UPDATE " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks SET \n" +
                            "  caption = ?, \n" +
                            "  owner = ?, \n" +
                            "  assigned = ?, \n" +
                            "  description = ?, \n" +
                            "  status = ? \n" +
                            "  WHERE id = ? "
                );
                ps.setString(1, task.getCaption());
                ps.setString(2, task.getOwner());
                ps.setString(3, task.getAssigned());
                ps.setString(4, task.getDescription());
                ps.setString(5, task.getStatus().toString());
                ps.setLong(6, task.getId());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new TTStorageException("Unable to insert/update");
        }
        return task;
    }

    @Override
    public Task getTask(Long id) throws TTStorageException {
        ResultSet rs = null;
        PreparedStatement ps;
        Task task = null;
        try {
            ps = connection.prepareCall("SELECT id, caption, owner, assigned, description, status FROM " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks WHERE id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if(rs.next()) {
                task = new Task(rs.getString("caption"), rs.getString("owner"), rs.getString("description"));
                task.setId(rs.getLong("id"));
                task.setStatus(Task.Status.valueOf(rs.getString("status")));
                task.assign(rs.getString("assigned"));
            }
        } catch (SQLException e) {
            throw new TTStorageException("Error in select by id");
        }
        return task;
    }

    @Override
    public List<Task> getTasks() {
        ResultSet rs = null;
        PreparedStatement ps;
        List<Task> taskList = new ArrayList<>();
        try {
            ps = connection.prepareCall("SELECT id, caption, owner, assigned, description, status FROM " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks");
            rs = ps.executeQuery();
            while (rs.next()) {
                Task task = null;
                task = new Task(rs.getString("caption"), rs.getString("owner"), rs.getString("description"));
                task.setId(rs.getLong("id"));
                task.setStatus(Task.Status.valueOf(rs.getString("status")));
                task.assign(rs.getString("assigned"));
                taskList.add(task);
            }
        } catch (SQLException | TTStorageException e) {
            e.printStackTrace();
        }
        return taskList;
    }

    @Override
    public void deleteTask(Long id) throws TTStorageException {
        PreparedStatement ps;
        try {
            ps = connection.prepareCall("DELETE FROM " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks WHERE id = ?");
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new TTStorageException("Deletion error");
        }
    }

    @Override
    public void deleteTask(String caption) throws TTStorageException {
        PreparedStatement ps;
        try {
            ps = connection.prepareCall("DELETE FROM " + Shadow.BASE + "." + Shadow.TAB_PREFIX + "tasks WHERE caption = ?");
            ps.setString(1, caption);
            ps.execute();
        } catch (SQLException e) {
            throw new TTStorageException("Deletion error");
        }
    }
}