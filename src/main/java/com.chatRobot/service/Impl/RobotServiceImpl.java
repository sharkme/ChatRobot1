package com.chatRobot.service.Impl;

import com.centit.fileserver.client.po.FileStoreInfo;
import com.centit.support.file.FileType;
import com.chatRobot.dao.IRobotDao;
import com.chatRobot.model.OneContent;
import com.chatRobot.model.Talk;
import com.chatRobot.service.IRobotService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("robotService")
public class RobotServiceImpl implements IRobotService {

    @Resource
    private IRobotDao robotDao;
    @Override
    public void learning(OneContent listenContent,OneContent backContent) {
        long idIn;
        long idOut;
        OneContent askContent=robotDao.selectListenContentByListenContent(listenContent.getWords());
        OneContent answerContent=robotDao.selectAnswerContentByAnswerContent(backContent.getWords());
        if(askContent==null){
            askContent=listenContent;
            askContent.setTimes(0);
            askContent.setCharatered(0);
//            askContent.setWords(listenContent.getWords());
            askContent.setCreateDate(new Date());
            robotDao.insertContent(askContent);
            idIn=askContent.getId();
        }else {
            //若已存在问题，则不再插入
            //滴滴滴，若需要查找一个问题有多少个答案，可查询talk表
//            askContent.setTimes(askContent.getTimes()+1);
//            robotDao.updateAskContent(askContent);
            idIn=askContent.getId();
        }
        if (answerContent==null){
            answerContent=backContent;
            answerContent.setTimes(0);
            answerContent.setCharatered(1);
//            answerContent.setWords(backContent.getWords());
            answerContent.setCreateDate(new Date());
            robotDao.insertContent(answerContent);
            idOut=answerContent.getId();
        }else {
            askContent.setTimes(askContent.getTimes()+1);
            robotDao.updateOneContentTimes(askContent);
            idOut=askContent.getId();
        }
//        String idqIn=robotDao.selectContentIDByContent(listenContent);
//        String idOut=robotDao.selectContentIDByContent(answerContent);
        Talk talk=new Talk();
        talk.setOutId(idOut);
        talk.setInId(idIn);
        robotDao.insertCorrespond(talk);

    }

    /**cread by shark
     * 获取到回答的语言内容
     * */
    @Override
    public List<OneContent> Answer(String listenContent) {
        OneContent askContent=robotDao.selectListenContentByListenContent(listenContent);
        List<OneContent> answerContents;
        if(askContent!=null){
            askContent.setTimes(askContent.getTimes()+1);//被询问次数
            robotDao.updateOneContentTimes(askContent);
            answerContents = robotDao.selectAnswerByListenContent(listenContent);
        }else {
            answerContents=new ArrayList<OneContent>();
            askContent=new OneContent();
            askContent.setWords("这个没学过");
            answerContents.add(askContent);
        }
        //滴滴滴，考虑将增删改查抽象出来
        return answerContents;
    }

    /**
     * 确认正确答案后的操作
     * */
    @Override
    public int makeSureTheAnswer(OneContent answerContent) {
        //滴滴滴，考虑将聊天关系表添加一个成功次数
        answerContent.setTimes(answerContent.getTimes()+1);
        return robotDao.updateOneContentTimes(answerContent);

    }
    /**
     * 获取所有的问题
     * */
    @Override
    public List<OneContent> getQuestions() {
        return robotDao.selectQuestions();
    }

    @Override
    public void saveFileMsg(String fileId, String fileMd5, long fileSize, String fileName) {
        FileStoreInfo fileStoreInfo=new FileStoreInfo();
        fileStoreInfo.setFileId(fileId);
        fileStoreInfo.setFileName(fileName);
        fileStoreInfo.setFileSize(fileSize);
        fileStoreInfo.setFileMd5(fileMd5);
        fileStoreInfo.setDownloadTimes(0L);
        fileStoreInfo.setCreateTime(new Date());
        try {
            fileStoreInfo.setFileType(FileType.getFileType(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        robotDao.saveFileMsg(fileStoreInfo);

    }

    @Override
    public FileStoreInfo getFileMsgById(String fileId) {
       return robotDao.getFileMsgById(fileId);
    }

}