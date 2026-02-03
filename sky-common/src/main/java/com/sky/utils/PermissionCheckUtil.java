package com.sky.utils;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;

public class PermissionCheckUtil {
    //检验当前操作是否由管理员进行

    public static boolean checkPermission(){
        Long empId = BaseContext.getCurrentId();
        if(empId == StatusConstant.ADMIN_ID){
            return true;
        }
        return false;
    }


}
