package com.tfr.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GmallManageWebApplicationTests {


    @Test
    void contextLoads() throws IOException, MyException {
       String tracker =GmallManageWebApplication.class.getResource("/tracker.conf").getPath();
        ClientGlobal.init(tracker);
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer=trackerClient.getTrackerServer();
        StorageClient storageClient=new StorageClient(trackerServer,null);
        String orginalFilename="F:/img/you/9505c3bde78ba7b0048d6fbc6731fd0.jpg";
        String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);

        String url="http:192.168.35.128";
        for (String s : upload_file) {
            url +="/"+s;
            System.out.println(url);
        }
    }

}
