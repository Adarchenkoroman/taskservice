package com.example.tasklist.repository;


import com.example.tasklist.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;


public interface TaskRepository extends JpaRepository<Task , Long> {



    @Query(value = """
            select * from tasks t
            join users_tasks ut on ut.task_id = t.id
            where ut.user_id = :userId""" , nativeQuery = true)
    List<Task> findAllByUserId(@Param("userId") Long userId);






}
