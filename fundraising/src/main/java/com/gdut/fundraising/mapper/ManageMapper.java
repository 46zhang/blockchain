package com.gdut.fundraising.mapper;

import com.gdut.fundraising.dto.ReadListResult;
import com.gdut.fundraising.entities.OrderTblEntity;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.UserTblEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ManageMapper {

    @Select("select count(project_id) from project_tbl where project_state=#{state}")
    int projectCount(int state);

    @Select("select project_id, project_photo, project_people_nums, project_money_now, project_name from project_tbl where project_state=#{state} limit ${pageIndex * pageSize}, #{pageSize}")
    List<ReadListResult> readProjectList(int pageIndex, int pageSize, int state);

    @Select("select * from project_tbl where project_id=#{projectId}")
    ProjectTblEntity readProjectDetail(String projectId);

    @Select("select * from user_tbl where user_token=#{token}")
    UserTblEntity selectUserByToken(String token);

    @Select("select * from project_tbl where project_id=#{projectId}")
    ProjectTblEntity selectProjectById(String projectId);

    @Update("update project_tbl set project_state=#{nextState} where project_id=#{projectId} and project_state=#{nowState}")
    int SetProjectToNext(String projectId, int nowState, int nextState);


    @Select("select count(project_id) from project_tbl where project_id=#{projectId} and user_id=#{formUserId}")
    int selectProjectByIdAndUser(OrderTblEntity orderTblEntity);

    @Select("select count(user_id) from user_tbl where user_id=#{toUserId}")
    int selectUserById(OrderTblEntity orderTblEntity);

    @Insert("insert into order_tbl(form_user_id, to_user_id, order_operator, order_id, order_money, project_id, order_time, order_explain) values(#{formUserId}, #{toUserId}, #{orderOperator}, #{orderId}, #{orderMoney}, #{projectId}, #{orderTime}, #{orderExplain})")
    int expenditure(OrderTblEntity orderTblEntity);

}
