package com.picc.plugin_camera.picture.type;

public class CaseRootType extends CaseType {

    public static final int CASE_ROOT_LIMIT = 50;

    public static final String SURVEY_NAME = "查勘照片";
    public static final String LOSS_NAME = "定损照片";
    public static final String DOCUMENT_NAME = "单证收集";

    private CaseType mSurveyType;
    private LossType mLossType;
    private final CaseType mDocumentType;
    private String scheduleType;
    private String registerNo;
    private String taskId;

    public CaseRootType() {
        super("CaseRootType", "CaseRootType", CASE_ROOT_LIMIT);
        //查看环节
        mSurveyType = new CaseType("8", SURVEY_NAME, NO_LIMIT);
        //定损环节
        mLossType = new LossType("7", LOSS_NAME, NO_LIMIT);
        //单证收集环节
        mDocumentType = new CaseType("6", DOCUMENT_NAME, NO_LIMIT);
        addChild(mSurveyType);
        addChild(mLossType);
        addChild(mDocumentType);
    }

    public CaseType getSurveyType() {
        return mSurveyType;
    }

    public CaseType getLossType() {
        return mLossType;
    }

    public CaseType getDocumentType() {
        return mDocumentType;
    }

    public void setTask(String taskId, String registerNo, String scheduleType) {
        this.taskId = taskId;
        this.registerNo = registerNo;
        this.scheduleType = scheduleType;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public String getRegisterNo() {
        return registerNo;
    }

    public String getTaskId() {
        return taskId;
    }

    public int getCasePictureCount() {
        return 0;
    }
}
