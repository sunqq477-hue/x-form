package cn.xiaofuge.x.infrastructure.dao;

import cn.xiaofuge.x.infrastructure.dao.po.FormDefinitionPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IFormDefinitionDao {
    @Insert("INSERT INTO x_form_definition (share_code, title, description, status, fields_json) "
            + "VALUES (#{shareCode}, #{title}, #{description}, #{status}, #{fieldsJson})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FormDefinitionPO form);

    @Select("SELECT f.id, f.share_code AS shareCode, f.title, f.description, f.status, "
            + "f.fields_json AS fieldsJson, f.created_at AS createdAt, "
            + "(SELECT COUNT(*) FROM x_form_submission s WHERE s.form_id = f.id) AS submissionCount "
            + "FROM x_form_definition f ORDER BY f.created_at DESC LIMIT 200")
    List<FormDefinitionPO> findAll();

    @Select("SELECT f.id, f.share_code AS shareCode, f.title, f.description, f.status, "
            + "f.fields_json AS fieldsJson, f.created_at AS createdAt, "
            + "(SELECT COUNT(*) FROM x_form_submission s WHERE s.form_id = f.id) AS submissionCount "
            + "FROM x_form_definition f WHERE f.id = #{id}")
    FormDefinitionPO findById(@Param("id") Long id);

    @Select("SELECT f.id, f.share_code AS shareCode, f.title, f.description, f.status, "
            + "f.fields_json AS fieldsJson, f.created_at AS createdAt, "
            + "(SELECT COUNT(*) FROM x_form_submission s WHERE s.form_id = f.id) AS submissionCount "
            + "FROM x_form_definition f WHERE f.share_code = #{shareCode} AND f.status = 'PUBLISHED'")
    FormDefinitionPO findPublishedByShareCode(@Param("shareCode") String shareCode);
}
