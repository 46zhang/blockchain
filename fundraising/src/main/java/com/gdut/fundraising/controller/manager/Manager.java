package com.gdut.fundraising.controller.manager;

import com.gdut.fundraising.entities.OrderTblEntity;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.service.ManageService;
import com.gdut.fundraising.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ControllerAdvice
@RestController
@RequestMapping("/manage")
public class Manager {

    @Autowired
    ManageService manageService;

    @PostMapping("/**")
    public Map notFound() throws BaseException {
        throw new BaseException(404,"Not Found!");
    }

    @GetMapping("/readProjectList")
    public Map readProjectList(@RequestHeader("AUTHORIZATION") String token, @RequestParam("pageIndex") int pageIndex, @RequestParam("pageSize") int pageSize, @RequestParam int state){
        return JsonResult.success(manageService.readProjectList(token.substring(7), pageIndex , pageSize, state)).result();
    }

    @GetMapping("/readProjectDetail")
    public Map readProjectDetail(@RequestHeader("AUTHORIZATION") String token, @RequestParam("projectId") String projectId){
        return JsonResult.success(manageService.readProjectDetail(token.substring(7), projectId)).result();
    }

    @PostMapping("/setProjectState")
    public Map setProjectState(@RequestHeader("AUTHORIZATION") String token, @RequestBody Map<String, Object> param) throws BaseException {
        try {
            return JsonResult.success(manageService.setProjectState(token.substring(7),
                    (Integer)param.get("nowState"), (Integer)param.get("nextState"),
                    (String) param.get("projectId"))).result();
        }
        catch (BaseException e){
            throw e;
        }
        catch (Exception e){
            throw new BaseException(400, "字段格式错误！");
        }

    }

    @PostMapping("/expenditure")
    public Map expenditure(@RequestHeader("AUTHORIZATION") String token, @RequestBody OrderTblEntity orderTblEntity){
        return JsonResult.success(manageService.expenditure(token.substring(7), orderTblEntity)).result();
    }


}
