package cn.xiaofuge.x.infrastructure.dao;

import cn.xiaofuge.x.infrastructure.dao.po.FormSubmissionPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IFormSubmissionDao {
    @Insert("INSERT INTO x_form_submission (form_id, answers_json) VALUES (#{formId}, #{answersJson})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FormSubmissionPO submission);

    @Select("SELECT id, form_id AS formId, answers_json AS answersJson, submitted_at AS submittedAt "
            + "FROM x_form_submission WHERE form_id = #{formId} ORDER BY submitted_at DESC LIMIT 500")
    List<FormSubmissionPO> findByFormId(@Param("formId") Long formId);
}
