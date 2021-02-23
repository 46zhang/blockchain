package com.gdut.fundraising.mapper;

import com.gdut.fundraising.dto.LoginResult;
import com.gdut.fundraising.dto.ReadDonationResult;
import com.gdut.fundraising.dto.ReadExpenditureResult;
import com.gdut.fundraising.dto.ReadListResult;
import com.gdut.fundraising.entities.GiftTblEntity;
import com.gdut.fundraising.entities.ProjectTblEntity;
import com.gdut.fundraising.entities.UserTblEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapper {

    @Select("select count(user_phone) from user_tbl where user_phone=#{phone}")
    int selectUserByPhone(String phone);

    @Insert("insert into user_tbl(user_id, user_phone, user_password, user_name, user_address, user_bank, user_token) values(#{userId}, #{userPhone}, #{userPassword}, #{userName}, #{userAddress}, #{userBank}, #{userToken})")
    void register(UserTblEntity userTblEntity);

    @Select("select * from user_tbl where user_phone=#{userPhone} and user_password=#{userPassword}")
    LoginResult login(UserTblEntity userTblEntity);

    @Update("update user_tbl set user_token=#{userToken} where user_phone=#{userPhone} and user_password=#{userPassword}")
    int updateToken(UserTblEntity userTblEntity);

    @Select("select * from user_tbl where user_token=#{token}")
    UserTblEntity selectUserByToken(String token);

    @Insert("insert into project_tbl set user_id=#{userId}, project_id=#{projectId}, project_start_time=#{projectStartTime}, project_finish_time=#{projectFinishTime}, project_state=0, project_name=#{projectName}, project_people_nums=0, project_money_target=-1, project_money_now=0, project_photo=#{projectPhoto}, project_explain=#{projectExplain}")
    void launch(ProjectTblEntity projectTblEntity);

    @Update("update project_tbl set project_photo=#{add} where project_id=#{projectId} and user_id=#{userId}")
    int updatePhoto(String userId, String projectId, String add);

    @Select("select count(project_id) from project_tbl where project_state>=2 and project_state<6")
    int projectCount();

    @Select("select project_id, project_photo, project_people_nums, project_money_now, project_name from project_tbl where project_state>=2 and project_state<6 limit ${pageIndex * pageSize}, #{pageSize}")
    List<ReadListResult> readProjectList(int pageIndex, int pageSize);

    @Select("select * from project_tbl where project_id=#{projectId} and project_state>=2 and project_state<6")
    ProjectTblEntity readProjectDetail(String projectId);

    @Update("update project_tbl set project_money_now=project_money_now+#{money}, project_people_nums=project_people_nums+1 where project_id=#{projectId} and project_state=2")
    int contributionUpdateProject(double money, String projectId);

    @Update("insert into gift_tbl(user_id, gift_id, gift_money, project_id, gift_time) values(#{userId}, #{giftId}, #{giftMoney}, #{projectId}, #{giftTime})")
    int contributionUpdateGiftTbl(GiftTblEntity giftTblEntity);

    @Select("select * from gift_tbl left join user_tbl on gift_tbl.user_id=user_tbl.user_id where project_id=#{projectId}")
    List<ReadDonationResult> readDonation(String projectId);

    @Select("select form_user_id, to_user_id, order_operator, order_id, order_money, project_id, order_time, order_explain, user_name to_user_name from order_tbl left join user_tbl on order_tbl.to_user_id=user_tbl.user_id where project_id=#{projectId}")
    List<ReadExpenditureResult> readExpenditureResult(String projectId);

}