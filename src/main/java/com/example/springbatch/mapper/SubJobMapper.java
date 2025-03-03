package com.example.springbatch.mapper;

import com.example.springbatch.entity.ParentJob;
import com.example.springbatch.entity.SubJob;
import com.example.springbatch.util.MapCollectionTypeHandler;
import com.fasterxml.jackson.annotation.JacksonInject;
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
public interface SubJobMapper {

    @Insert("INSERT INTO sub_job (id, parent_id, status, priority, context, is_dependent, dependency_id) " +
            "VALUES (NEXT VALUE FOR sub_job_seq, #{parentId}, #{status}, #{priority}, " +
            "#{context, typeHandler=com.example.springbatch.util.MapCollectionTypeHandler}, #{isDependent}, #{dependencyId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer create(SubJob subJob);

    @Results(
            id = "subJobResultMap",
            value = {
                    @Result(column = "id", property = "id", javaType = Long.class),
                    @Result(column = "parent_id", property = "parentId", javaType = Long.class),
                    @Result(column = "is_dependent", property = "isDependent", javaType = Boolean.class),
                    @Result(column = "status", property = "status", javaType = String.class),
                    @Result(column = "priority", property = "priority", javaType = Long.class),
                    @Result(column = "dependency_id", property = "dependencyId", javaType = Long.class),
                    @Result(column = "context", property = "context", typeHandler = MapCollectionTypeHandler.class, javaType = Map.class)
            })
    @Select("SELECT * FROM sub_job WHERE parent_id = #{parentId} AND status = 'CREATED' FETCH FIRST #{batchSize} ROWS ONLY")
    List<SubJob> findNextBatch(@Param("parentId") Long parentId, @Param("batchSize") int batchSize);

    @Update("UPDATE sub_job SET status = #{status} WHERE id = #{id}")
    Integer updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM sub_job WHERE parent_id = #{parentJobId} AND is_dependent = TRUE")
    boolean hasDependentJobs(@Param("parentJobId") Long parentJobId);

    @Select("SELECT * FROM sub_job WHERE parent_id = #{parentJobId}")
    @ResultMap("subJobResultMap")
    List<SubJob> getSubJobs(@Param("parentJobId") Long parentJobId);

    @Select("SELECT * FROM sub_job WHERE parent_id = #{parentJobId} AND is_dependent = TRUE")
    @ResultMap("subJobResultMap")
    List<SubJob> getDependentJobs(Long parentJobId);

    @Select("SELECT COUNT(*) FROM sub_job WHERE id = #{dependencyId} AND status = 'COMPLETED'")
    boolean isDependencyCompleted(@Param("dependencyId") Long dependencyId);

    @Select("SELECT * FROM sub_job WHERE id = #{subJobId}")
    @ResultMap("subJobResultMap")
    SubJob getSubJobById(Long subJobId);

    @Select("SELECT * FROM sub_job WHERE parent_id = #{parentJobId} " +
            "AND REGEXP_LIKE(context, '\"resourceType\"\\s*:\\s*\"' || #{resourceType} || '\"')")
    @ResultMap("subJobResultMap")
    SubJob findByResourceType(@Param("parentJobId") Long parentJobId, @Param("resourceType") String resourceType);
}