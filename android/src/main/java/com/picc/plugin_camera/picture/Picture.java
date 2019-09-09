package com.picc.plugin_camera.picture;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Picture {

    @Id
    public long id;

    public String taskId; //关联 task

    public String registerNo;//：RDZA……

    public String checkPhotoDescription;//:相片中文描述

    public String licenseNo;//：车牌号

    public String fileTypeCode;//：单证类型码(和原大唐一致)

    public String partName;//:配件名称

    public String partId;//：配件ID

    public String port;//：标的车: A  ,三者车: B1 ，B2 。。。。
    public String carId;//: 车辆ID 用于标识是哪一辆车

    public String comCode;//上传人机构码

    public String macAddr;//：上传机器mac地址

    public String manufacturer;//：制造厂商

    public String cameraType;//：相机型号

    public long photoTime;//: 影像拍摄时间

    public long photoSize;//: 影像尺寸

    public String propId;//与财产保护id

    public String photoType;//

    public String typeName;

    public String imageUri;

    public String thumbUri;

    public boolean update;


    public String scheduleType; //任务类型
}
