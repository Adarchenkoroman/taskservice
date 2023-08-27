package com.example.tasklist.repository.impl;


import com.example.tasklist.domain.exceptiom.ResourceMappingException;
import com.example.tasklist.domain.task.Task;
import com.example.tasklist.repository.DataSourceConfig;
import com.example.tasklist.repository.TaskRepository;
import com.example.tasklist.repository.mappers.TaskRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;


//@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final DataSourceConfig dataSourceConfig;

    private final String FIND_BY_ID = """
    select t_id as task_id,
    t_title as task_title,
    t_description as task_description,
    t_expiration_date as task_expiration_date,
    t_status as task_status
    from tasks t
    where id = ?""";

    private final String FIND_ALL_BY_USER_ID = """
       select t_id as task_id,
       t_title as task_title,
       t_description as task_description,
       t_expiration_date as task_expiration_date,
       t_status as task_status
       from tasks t
      join users_tasks ut on t_id = ut.task_id
      where ut.user_id = ?""";

    private final String ASSIGN = """
            insert into users_tasks(task_id , user_id)
            values (? , ?)""";

    private final String UPDATE = """
            update tasks
            set title = ?,
                description = ?,
                expiration_date = ?,
                status = ?
            where id = ?""";

    private final String CREATE = """
            insert into tasks (title , description , expiration_date , status)
            values (? , ? , ? , ?)""";


    private final String DELETE = """
            delete from tasks
            where id = ?""";





    @Override
    public Optional<Task> findById(Long id) {
        try{
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);
            statement.setLong(1 , id);
            try(ResultSet rs = statement.executeQuery()) {
               return Optional.ofNullable(TaskRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new ResourceMappingException("Error finding user by id");
        }
    }

    @Override
    public List<Task> findAllByUserId(Long userId) {
        try{
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_ALL_BY_USER_ID);
            statement.setLong(1 , userId);
            try(ResultSet rs = statement.executeQuery()) {
                return TaskRowMapper.mapRows(rs);
            }
        } catch (SQLException e) {
            throw new ResourceMappingException("Error finding all user by id");
        }
    }

    @Override
    public void assignToUserById(Long taskId, Long userId) {
        try{
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(ASSIGN);
            statement.setLong(1 , taskId);
            statement.setLong(2 , userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ResourceMappingException("Error assigning to user");
        }
    }

    @Override
    public void update(Task task) {
        try{
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE);
            statement.setString(1 , task.getTitle());
            if (task.getDescription() == null){
                statement.setNull(2 , Types.VARCHAR);
            } else {
                statement.setString(2 , task.getDescription());
            }



            if (task.getExpirationDate() == null){
                statement.setNull(3 , Types.TIMESTAMP);
            } else {
                statement.setTimestamp(3 , Timestamp.valueOf(task.getExpirationDate()));
            }

            statement.setString(4 , task.getStatus().name());
            statement.setLong(5 , task.getId());


            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ResourceMappingException("Error updating task");
        }
    }

    @Override
    public void create(Task task) {
        try{
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(CREATE , PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1 , task.getTitle());
            if (task.getDescription() == null){
                statement.setNull(2 , Types.VARCHAR);
            } else {
                statement.setString(2 , task.getDescription());
            }



            if (task.getExpirationDate() == null){
                statement.setNull(3 , Types.TIMESTAMP);
            } else {
                statement.setTimestamp(3 , Timestamp.valueOf(task.getExpirationDate()));
            }

            statement.setString(4 , task.getStatus().name());



            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                task.setId(rs.getLong(1));

            }
        } catch (SQLException e) {
            throw new ResourceMappingException("Error creating task");
        }

    }

    @Override
    public void delete(Long id) {
        try{
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(DELETE);
            statement.setLong(1 , id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ResourceMappingException("Error deleting task");
        }


    }
}
