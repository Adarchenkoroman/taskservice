package com.example.tasklist.repository.impl;

import com.example.tasklist.domain.exceptiom.ResourceMappingException;
import com.example.tasklist.domain.user.Role;
import com.example.tasklist.domain.user.User;
import com.example.tasklist.repository.DataSourceConfig;
import com.example.tasklist.repository.UserRepository;
import com.example.tasklist.repository.mappers.UserRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


//@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final DataSourceConfig dataSourceConfig;


    private final String FIND_BY_ID = """
            select u.id as user_id,
                   u.name as user_name,
                   u.username as user_username,
                   u.password as user_password,
                   ur.role as user_role_role,
                   t.id as task_id,
                   t.title as task_title,
                   t.description as task_description,
                   t.expiration_date as task_expiration_date,
                   t.status as task_status
                
            from users u
                left join users_roles  ur on u_id = ur.user_id
                left join users_tasks  ut on u_id = ut.user_id
                left join tasks t on ut.task_id = t.id
               
            where u.id = ?""";


    private final String FIND_BY_USERNAME = """
            select u.id as user_id,
                   u.name as user_name,
                   u.username as user_username,
                   u.password as user_password,
                   ur.role as user_role,
                   t.id as task_id,
                   t.title as task_title,
                   t.description as task_description,
                   t.expiration_date as task_expiration_date,
                   t.status as task_status
                 
            from users u
                left join users_roles  ur on u_id = ur.user_id
                left join users_tasks  ut on u_id = ut.user_id
                left join tasks t on ut.task_id = t.id
               
            where u.username = ?""";


    private final String UPDATE = """
            update users
            set name = ?,
                username = ?,
                password = ?,
            where id = ?""";


    private final String CREATE = """
            insert into users (name , username , password)
            values(? , ? , ?)""";



    private final String INSERT_USER_ROLE = """
            insert into users_roles (user_id , role)
            values(? , ?)""";


    private final String IS_TASK_OWNER = """
            select exists (
              select 1
              from users_tasks
              where user_id = ?
              and task_id = ?
            )""";


    private final String DELETE = """
            delete from users
            where id = ?""";






    @Override
    public Optional<User> findById(Long id) {

        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_BY_ID ,
                    ResultSet.TYPE_SCROLL_INSENSITIVE ,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setLong(1 , id);
            try(ResultSet rs = statement.executeQuery()){
                return Optional.ofNullable(UserRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new ResourceMappingException("Exception while finding user by id");
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {

        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME ,
                    ResultSet.TYPE_SCROLL_INSENSITIVE ,
                    ResultSet.CONCUR_READ_ONLY);
            statement.setString(1 , username);
            try(ResultSet rs = statement.executeQuery()){
                return Optional.ofNullable(UserRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new ResourceMappingException("Exception while finding user by username");
        }
    }

    @Override
    public void update(User user) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(UPDATE);
            statement.setString(1 , user.getName());
            statement.setString(2 , user.getUsername());
            statement.setString(3 , user.getPassword());
            statement.setLong(4 , user.getId());
        } catch (SQLException e) {
            throw new ResourceMappingException("Exception while updating user");
        }

    }

    @Override
    public void create(User user) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(CREATE , PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1 , user.getName());
            statement.setString(2 , user.getUsername());
            statement.setString(3 , user.getPassword());
            statement.executeUpdate();


            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                user.setId(rs.getLong(1));

            }
        }
          catch (SQLException e) {
            throw new ResourceMappingException("Exception while creating user");
        }

    }

    @Override
    public void insertUserRole(Long userId, Role role) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(INSERT_USER_ROLE);
            statement.setLong(1 , userId);
            statement.setString(2 , role.name());

            statement.executeUpdate();

        }
        catch (SQLException e) {
            throw new ResourceMappingException("Exception while inserting user role");
        }

    }

    @Override
    public boolean isTaskOwner(Long userId, Long taskId) {


        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(IS_TASK_OWNER);
            statement.setLong(1 , userId);
            statement.setLong(2 , taskId);
            try(ResultSet rs = statement.executeQuery()){
                rs.next();
                return rs.getBoolean(1);
            }


        }
        catch (SQLException e) {
            throw new ResourceMappingException("Exception while cheking if user is task owner");
        }
    }

    @Override
    public void delete(Long id) {
        try {
            Connection connection = dataSourceConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(DELETE);
            statement.setLong(1 , id);


            statement.executeUpdate();

        }
        catch (SQLException e) {
            throw new ResourceMappingException("Exception while deleting user ");
        }

    }
}
