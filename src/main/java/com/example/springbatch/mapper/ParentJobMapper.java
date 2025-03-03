package com.example.springbatch.mapper;

import com.example.springbatch.entity.ParentJob;
import com.example.springbatch.util.MapCollectionTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ParentJobMapper {

    @Insert("INSERT INTO parent_job (id, type, task, status, priority, context) " +
            "VALUES (NEXT VALUE FOR parent_job_seq, #{type}, #{task}, #{status}, #{priority}, " +
            "#{context, typeHandler=com.example.springbatch.util.MapCollectionTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer create(ParentJob parentJob);

    @Results(
            id = "parentJobResultMap",
            value = {
                    @Result(column = "id", property = "id", javaType = Long.class),
                    @Result(column = "type", property = "type", javaType = String.class),
                    @Result(column = "task", property = "task", javaType = String.class),
                    @Result(column = "status", property = "status", javaType = String.class),
                    @Result(column = "priority", property = "priority", javaType = Long.class),
                    @Result(column = "context", property = "context",
                            typeHandler = MapCollectionTypeHandler.class, javaType = Map.class)})
    @Select("SELECT * FROM parent_job WHERE status = 'CREATED' AND priority = #{priority}")
    List<ParentJob> findJobsToTrigger(@Param("priority") Long priority);

    @Update("UPDATE parent_job SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @ResultMap("parentJobResultMap")
    @Select("SELECT * FROM parent_job WHERE priority = #{priority}")
    List<ParentJob> findJobBasedOnPriority(@Param("priority") Long priority);
}