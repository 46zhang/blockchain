package com.gdut.fundraising.util;

import com.alibaba.fastjson.JSON;
import com.gdut.fundraising.blockchain.Block;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

public class FileUtilsTest {

    @Test
    public void testCreateDir() {
        String rootPath = FileUtils.getRootFilePath();
        String pathName = rootPath + "\\10000";

        Assert.assertTrue(pathName.equals(FileUtils.buildPath(rootPath, "10000")));
        boolean res = FileUtils.createDir(pathName);
        Assert.assertTrue(res);
        pathName = rootPath + "\\20000";
        res = FileUtils.createDir(pathName);
        Assert.assertTrue(res);
    }

    @Test
    public void testCreateFile() {
        String rootPath = FileUtils.getRootFilePath();
        String pathName = rootPath + "\\10000" + "\\1.json";
        boolean res = FileUtils.createFile(pathName);
        Assert.assertTrue(res);
        pathName = rootPath + "\\10000" + "\\2.json";
        res = FileUtils.createFile(pathName);
        Assert.assertTrue(res);
    }

    @Test
    public void testWrite() throws IOException {
        Block block = new Block();
        block.setHash("1342342425");
        block.setHeight(10L);
        block.setMerkleRootHash("dsgsdg23twggergsfg");
        block.setTime(new Date());
        block.setPreBlockHash("f32fhoiwhgo8h324ghewpjg[w4kt[");
        block.setVersion("1.0.0");

        String s = JSON.toJSONString(block);

        String rootPath = FileUtils.getRootFilePath();
        String pathName = rootPath + "\\10000" + "\\1.json";

        System.out.println(rootPath);
        System.out.println(pathName);
        boolean res = FileUtils.write(pathName, s);

        Assert.assertTrue(res);

        String data = FileUtils.read(pathName);

        Block block1 = (Block) JSON.parseObject(data, Block.class);
        Assert.assertTrue(block.getHash().equals(block1.getHash()));
        Assert.assertEquals(block.getHeight(), block1.getHeight());
        Assert.assertTrue(block.getPreBlockHash().equals(block1.getPreBlockHash()));
    }

    @Test
    public void testGetFileInDir() {
        String port = "10000";
        //根据不同系统构建文件路径
        String pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port);

        File[] files= FileUtils.getAllFileInDir(pathName);

        String[] fileNames=FileUtils.getAllFileNameInDir(pathName);
        for (String file:fileNames){
            System.out.println(FileUtils.read(FileUtils.buildPath(pathName,file)));
        }
     }


}